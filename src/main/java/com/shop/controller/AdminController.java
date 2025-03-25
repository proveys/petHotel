package com.shop.controller;

import com.shop.dto.ReservationResponseDTO;
import com.shop.entity.ChatRoom;
import com.shop.entity.Order;
import com.shop.repository.OrderRepository;
import com.shop.service.ChatRoomService;
import com.shop.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ChatRoomService chatRoomService;
    private final ReservationService reservationService;
    private final OrderRepository orderRepository;

    public AdminController(ChatRoomService chatRoomService, ReservationService reservationService,
                           OrderRepository orderRepository){
        this.chatRoomService = chatRoomService;
        this.reservationService = reservationService;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/adminPage")
    public String adminPage(Model model) {
        // 채팅방 목록 데이터 추가
        List<ChatRoom> activeChatRooms = chatRoomService.getActiveChatRooms();
        model.addAttribute("chatRooms", activeChatRooms);

        // 예약 목록 데이터 추가
        List<ReservationResponseDTO> reservations = reservationService.getAllReservations();
        model.addAttribute("reservations", reservations);

        List<Order> orders = orderRepository.findAll();
        for (Order order : orders) {
            System.out.println("Order ID: " + order.getId()); // 디버깅용 로그
        }
        model.addAttribute("orders",orders);

        return "admin/adminPage";
    }


    // 특정 예약 정보 팝업
    @GetMapping("/reservation/{id}")
    public String getReservationDetails(@PathVariable Long id, Model model) {
        ReservationResponseDTO reservation = reservationService.getReservationById(id);
        model.addAttribute("reservation", reservation); // DTO 사용
        return "admin/reservationPopup"; // 팝업 HTML 파일
    }


    @PostMapping("/reservations/cancel/{id}")
    @ResponseBody
    public ResponseEntity<String> cancelReservation(@PathVariable Long id) {
        try {
            reservationService.cancelAndRefundReservation(id); // 예약 취소 로직 실행
            return ResponseEntity.ok("예약이 성공적으로 취소되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("취소 중 오류 발생: " + e.getMessage());
        }
    }

    @GetMapping("/orders/{id}/items")
    public String getOrderItems(@PathVariable("id") Long orderId, Model model) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Invalid order ID"));
        model.addAttribute("orderItems", order.getOrderItems());
        model.addAttribute("order", order);
        return "admin/orderItemPage"; // OrderItem 목록 페이지
    }
}
