package com.shop.controller;


import com.shop.dto.OrderDto;
import com.shop.dto.OrderHistDto;
import com.shop.service.OrderService;
import com.shop.service.PortOneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PortOneService portOneService; // 포트원 환불 서비스 주입
    @PostMapping(value = "/order")
    public @ResponseBody ResponseEntity order(@RequestBody @Valid List<OrderDto> orderDtos, BindingResult bindingResult, Principal principal) {
        log.debug("Received OrderDtos: {}, orderDtos"); //요청 데이터 출력
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                log.error("Validation error on field: {}", fieldError.getField());
                sb.append(fieldError.getDefaultMessage());
            }
            return new ResponseEntity<>(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        String email = principal.getName();

        try {
            Long orderId = orderService.order(orderDtos, email);
            log.info("order created 성공한 ID: {}",orderId);
            return new ResponseEntity<>(orderId, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Order processing failed", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }




    @GetMapping(value = {"/orders", "/orders/{page}"})
    public String orderHist(@PathVariable("page") Optional<Integer> page, Principal principal, Model model) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 5);

        log.debug("Fetching order history for user: {}", principal.getName());
        log.debug("Pageable details: {}", pageable);

        Page<OrderHistDto> orderHistDtoList = orderService.getOrderList(principal.getName(), pageable);

        model.addAttribute("orders", orderHistDtoList);
        model.addAttribute("page", pageable.getPageNumber());
        model.addAttribute("maxPage", 5);
        return "order/orderHist";

    }

    @PostMapping("/order/{orderId}/cancel")
    public @ResponseBody
    ResponseEntity cancelOrder(@PathVariable("orderId") Long orderId, Principal principal) {
        // 주문 권한 검증
        if (!orderService.validateOrder(orderId, principal.getName())) {
            return new ResponseEntity<>("주문 취소 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        try {
            // 주문 취소 처리 (DB 업데이트)
            orderService.cancelOrder(orderId);

            // 환불 처리
            String payNo = orderService.getPayNoByOrderId(orderId); // 주문 번호에 해당하는 결제 번호 가져오기
            String reason = "사용자 요청으로 인한 환불"; // 환불 사유
            portOneService.cancelPayment(payNo, reason); // 환불 API 호출

            return new ResponseEntity<>(orderId, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("환불 처리 중 오류 발생: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

