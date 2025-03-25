package com.shop.service;

import com.shop.dto.OrderDto;
import com.shop.dto.OrderHistDto;
import com.shop.dto.OrderItemDto;
import com.shop.entity.*;
import com.shop.repository.ItemImgRepository;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final ItemImgRepository itemImgRepository;
    private final PortOneService portOneService;// 포트원 환불 서비스 추가

    // 주문 생성
    public Long order(List<OrderDto> orderDtos, String email) {
        log.debug("Processing order for email: {}", email);
        log.debug("Processing OrderDtos:{}", orderDtos); //입력된 주문 데이터를 확인
        Member member = memberRepository.findByEmail(email);

        if (member == null) {
            log.error("Member not found for email: {}", email);
            throw new EntityNotFoundException("Member not found");
        }

        log.debug("Member found: {}", member);


        List<OrderItem> orderItemList = new ArrayList<>();
        String payNo = orderDtos.get(0).getImpUid(); // 동일 결제 ID
        log.debug("Payment No (payNo): {}", payNo);


        for (OrderDto orderDto : orderDtos) {
            log.debug("Processing OrderDto: {}", orderDto); // 각 항목 확인

            Item item = itemRepository.findById(orderDto.getItemId())
                    .orElseThrow(() -> {
                        log.error("Item not found with ID: {}", orderDto.getItemId());
                        return new EntityNotFoundException("Item not found");
                    });

            log.debug("Item found: {}", item);

            OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount(), payNo);
            log.debug("Created OrderItem: {}", orderItem);
            orderItemList.add(orderItem);
        }

        Order order = Order.createOrder(member, orderItemList);
        order.setPayNo(payNo);

        log.debug("Created Order: {}", order);

        orderRepository.save(order);
        log.info("Order saved successfully with ID: {}", order.getId());

        return order.getId();
    }


    @Transactional(readOnly = true)
    public Page<OrderHistDto> getOrderList(String email, Pageable pageable) {
        log.debug("Fetching order list for email: {}", email);
        log.debug("Pageable details: {}", pageable);

        List<Order> orders = orderRepository.findOrders(email, pageable); // 주문 리스트
        log.debug("Fetched Orders: {}", orders);

        Long totalCount = orderRepository.countOrder(email); // 총 주문 수
        log.debug("Total Orders Count: {}", totalCount);


        List<OrderHistDto> orderHistDtos = new ArrayList<>(); // 주문 히스토리 리스트
        for (Order order : orders) {
            log.debug("Processing Order: {}", order);

            OrderHistDto orderHistDto = new OrderHistDto(order);
            List<OrderItem> orderItems = order.getOrderItems();

            for (OrderItem orderItem : orderItems) {
                log.debug("Processing OrderItem: {}", orderItem);

                ItemImg itemImg = itemImgRepository.findByItemIdAndRepImgYn(orderItem.getItem().getId(), "Y");
                log.debug("Fetched ItemImg: {}", itemImg);

                OrderItemDto orderItemDto = new OrderItemDto(orderItem, itemImg.getImgUrl());
                orderHistDto.addOrderItemDto(orderItemDto);
            }
            orderHistDtos.add(orderHistDto);
        }
        log.info("Order history fetched successfully for email: {}", email);
        return new PageImpl<>(orderHistDtos, pageable, totalCount);
    }

    @Transactional(readOnly = true)
    public boolean validateOrder(Long orderId, String email){
        log.debug("Validating order with ID: {} for email: {}", orderId, email);

        Member curMember = memberRepository.findByEmail(email);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with ID: {}", orderId);
                    return new EntityNotFoundException("Order not found");
                });

        Member savedMember = order.getMember();

        log.debug("Current Member: {}", curMember);
        log.debug("Saved Member: {}", savedMember);


        boolean isValid = StringUtils.equals(curMember.getEmail(), savedMember.getEmail());
        log.debug("Validation result: {}", isValid);

        return isValid;
    }

    // 주문 취소 및 환불
    public void cancelOrder(Long orderId) {
        log.debug("Cancelling order with ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with ID: {}", orderId);
                    return new EntityNotFoundException("Order not found");
                });

        log.debug("Order found: {}", order);

        String payNo = order.getPayNo();
        log.debug("Payment No (payNo): {}", payNo);

        if (payNo != null) {
            String reason = "사용자 요청으로 인한 환불";
            log.info("Processing refund for payNo: {}", payNo);
            portOneService.cancelPayment(payNo, reason); // 환불 처리
        }

        order.cancelOrder();
        log.info("Order cancelled successfully with ID: {}", orderId);
    }

    public Long orders(List<OrderDto> orderDtoList, String email) {
        log.debug("Processing orders for email: {}", email);
        log.debug("Received OrderDtos: {}", orderDtoList);

        Member member = memberRepository.findByEmail(email);
        List<OrderItem> orderItemList = new ArrayList<>();

        for (OrderDto orderDto : orderDtoList) {
            log.debug("Processing OrderDto: {}", orderDto);

            Item item = itemRepository.findById(orderDto.getItemId())
                    .orElseThrow(() -> {
                        log.error("Item not found with ID: {}", orderDto.getItemId());
                        return new EntityNotFoundException("Item not found");
                    });

            OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount(), orderDto.getImpUid());
            log.debug("Created OrderItem: {}", orderItem);

            orderItemList.add(orderItem);
        }

        Order order = Order.createOrder(member, orderItemList);
        orderRepository.save(order);

        log.info("Orders saved successfully with ID: {}", order.getId());
        return order.getId();
    }

    public String getPayNoByOrderId(Long orderId) {
        log.debug("Fetching payment number for order ID: {}", orderId);
        String payNo = orderRepository.findPayNoByOrderId(orderId);
        log.debug("Fetched payNo: {}", payNo);
        return payNo;
    }
}
