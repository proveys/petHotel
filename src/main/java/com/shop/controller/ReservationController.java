package com.shop.controller;


import com.shop.domain.Room;
import com.shop.dto.ReservationRequestDTO;
import com.shop.dto.ReservationResponseDTO;
import com.shop.entity.Member;
import com.shop.entity.Reservation;
import com.shop.service.MemberService;
import com.shop.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@Controller
@RequestMapping("/reservation")
public class ReservationController {

    private final ReservationService reservationService;
    private final MemberService memberService;

    @Autowired
    public ReservationController(ReservationService reservationService, MemberService memberService) {
        this.reservationService = reservationService;
        this.memberService = memberService;
    }

    // 예약 페이지 반환 (HTML 렌더링)
    @GetMapping("/view")
    public String getReservationPage(Model model) {
        // 기본적으로 4개의 객실 정보를 추가
        model.addAttribute("rooms", List.of(
                new Room("101", "101호", 234000, 2, 3),
                new Room("102", "102호", 324000, 4, 5),
                new Room("201", "201호", 234000, 2, 3),
                new Room("202", "202호", 324000, 4, 5)
        ));

        return "reservation/reservation2"; // reservation2.html 반환
    }

    // 새로운 예약 생성 (REST API - JSON 응답)
    @PostMapping("/api")
    @ResponseBody
    public ReservationResponseDTO createReservationApi(@RequestBody ReservationRequestDTO requestDTO) {
        // 중복 확인 로직
        if (reservationService.isReservationDuplicate(requestDTO)) {
            throw new IllegalStateException("중복된 예약 요청입니다,");
        }

        // 로그인한 사용자 정보 가져오기
        Member loggedInMember = memberService.getLoggedInMember();

        // 로그인한 사용자의 ID를 DTO 에 설정
        requestDTO.setUserId(loggedInMember.getId());

        // 예약 생성 처리
        return reservationService.createReservation(requestDTO);
    }

    // 새로운 예약 생성 (HTML Form 처리)
    @PostMapping("/form")
    public String createReservationForm(@ModelAttribute ReservationRequestDTO requestDTO, Model model) {
        try {
            ReservationResponseDTO reservation = reservationService.createReservation(requestDTO);
            model.addAttribute("reservation", reservation);
            return "reservation/reservation-complete"; // 예약 완료 페이지 반환
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "reservation/reservation2"; // 오류 시 다시 예약 페이지 반환
        }
    }

    // 사용자 예약 목록 반환 (REST API)
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<List<ReservationResponseDTO>> getUserReservations(@RequestParam Long userId) {
        List<ReservationResponseDTO> reservations = reservationService.getUserReservations(userId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/list/view")
    public String getReservationListPage(Model model){
        // 예약 목록 데이터를 가져올 수 있다면 모델에 추가
        List<ReservationResponseDTO> reservations = reservationService.getAllReservations(); // ex) 모든 예약 조회
        model.addAttribute("reservations", reservations);

        return "reservation/reservation1";
    }

    @GetMapping("/api/reservations")
    @ResponseBody
    public List<ReservationResponseDTO> getAllReservations() {
        return reservationService.getAllReservations();
    }


    @GetMapping("/reservation-complete")
    public String showReservationCompletePage(HttpSession session, Model model) {
        Reservation reservation = (Reservation) session.getAttribute("reservation");
        if (reservation != null) {
            System.out.println("Received Reservation : " + reservation);
            model.addAttribute("reservation", reservation);
        } else {
            System.out.println("NO reservation data found.");
        }
        return "/reservation/reservation-complete";
    }
}


