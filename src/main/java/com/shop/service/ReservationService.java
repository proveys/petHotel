package com.shop.service;


import com.shop.dto.ReservationRequestDTO;
import com.shop.dto.ReservationResponseDTO;
import com.shop.entity.Reservation;
import com.shop.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PortOneService portOneService;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, PortOneService portOneService){
        this.reservationRepository = reservationRepository;
        this.portOneService = portOneService;
    }


    // 새로운 예약 생성
    @Transactional
    public ReservationResponseDTO createReservation(ReservationRequestDTO requestDTO){
        // 객실 중복 확인
        List<Reservation> conflictingReservations = reservationRepository
                .findByRoomIdAndCheckinDateLessThanEqualAndCheckoutDateGreaterThanEqual(
                        requestDTO.getRoomId(),
                        requestDTO.getCheckoutDate(),
                        requestDTO.getCheckinDate()
                );

        if(!conflictingReservations.isEmpty()) {
            throw new IllegalStateException(" 이 날짜에 해당 객실은 이미 예약되었습니다.");
        }

        // 예약 저장
        Reservation reservation = new Reservation(
                requestDTO.getUserId(),
                requestDTO.getRoomId(),
                requestDTO.getCheckinDate(),
                requestDTO.getCheckoutDate(),
                requestDTO.getAdultCount(),
                requestDTO.getChildCount(),
                requestDTO.getTotalPrice()
        );

        reservationRepository.save(reservation);

        // 저장된 예약 정보를 DTO 로 변환하여 반환
        return toResponseDTO(reservation);
    }

    // 특정 사용자의 예약 목록 조회
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getUserReservations(Long userId){
        List<Reservation> reservations = reservationRepository.findByUserId(userId);
        return reservations.stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    /**
     * 예약 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public ReservationResponseDTO getReservationById(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약이 존재하지 않습니다. ID: " + reservationId));

        return toResponseDTO(reservation);
    }

    /**
     * DTO → Entity 변환
     */
    private Reservation toEntity(ReservationRequestDTO dto) {
        return new Reservation(
                dto.getUserId(),
                dto.getRoomId(),
                dto.getCheckinDate(),
                dto.getCheckoutDate(),
                dto.getAdultCount(),
                dto.getChildCount(),
                dto.getTotalPrice()
        );
    }

    /**
     * Entity → DTO 변환
     */
    private ReservationResponseDTO toResponseDTO(Reservation reservation) {
        ReservationResponseDTO responseDTO = new ReservationResponseDTO();
        responseDTO.setId(reservation.getId());
        responseDTO.setUserId(reservation.getUserId());
        responseDTO.setRoomId(reservation.getRoomId());
        responseDTO.setCheckinDate(reservation.getCheckinDate());
        responseDTO.setCheckoutDate(reservation.getCheckoutDate());
        responseDTO.setAdultCount(reservation.getAdultCount());
        responseDTO.setChildCount(reservation.getChildCount());
        responseDTO.setTotalPrice(reservation.getTotalPrice());
        responseDTO.setCreatedAt(reservation.getCreatedAt());

        System.out.println("Converted DTO: " + responseDTO); // 로그 추가
        return responseDTO;
    }

    public boolean isReservationDuplicate(ReservationRequestDTO requestDTO){
        return reservationRepository.existsByRoomIdAndCheckinDateAndCheckoutDate(
                requestDTO.getRoomId(),
                requestDTO.getCheckinDate(),
                requestDTO.getCheckoutDate()
        );
    }

    // 모든 예약 목록 조회
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();

        if (reservations == null || reservations.isEmpty()) {
            System.out.println("No reservations found in the database.");
            return Collections.emptyList(); // 빈 리스트 반환
        }

        System.out.println("Reservations fetched from DB: " + reservations);

        return reservations.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelAndRefundReservation(Long reservationId){
        // 1. 예약 데이터 가저오기
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다."));

        // 2. 환불 처리( 예약 데이터 활용)
        processRefund(reservation);

        // 3. 예약 삭제
        reservationRepository.delete(reservation);
    }

    private void processRefund(Reservation reservation) {
        String merchantUid = "reservation-" + reservation.getId();
        String reason = "사용자 요청으로 인한 예약 취소";

        log.info("환불 처리 시작 - 예약 ID: {}, MerchantUID: {}", reservation.getId(), merchantUid);

        try {
            portOneService.cancelPayment(merchantUid, reason);
            log.info("환불 처리 성공 - 예약 ID: {}", reservation.getId());
        } catch (Exception e) {
            log.error("환불 처리 중 오류 발생 - 예약 ID: {}", reservation.getId(), e);
            throw new RuntimeException("환불 처리 중 오류 발생", e);
        }
    }



    // 결제 확인 및 예약 저장
    @Transactional
    public Reservation confirmAndSaveReservation(Map<String, Object> paymentData, String impUid, int amount) {
        // 결제 정보 확인
        Map<String, Object> paymentInfo = portOneService.getPaymentData(impUid);
        int paidAmount = (Integer) paymentInfo.get("amount");
        if (paidAmount != amount) {
            throw new IllegalStateException("결제 금액 불일치");
        }

        try {
            // 예약 데이터 유효성 검사
            if (!paymentData.containsKey("userId") || paymentData.get("userId") == null) {
                throw new IllegalArgumentException("사용자 ID가 누락되었습니다.");
            }
            if (!paymentData.containsKey("roomId") || paymentData.get("roomId") == null) {
                throw new IllegalArgumentException("객실 ID가 누락되었습니다.");
            }
            if (!paymentData.containsKey("checkinDate") || paymentData.get("checkinDate") == null) {
                throw new IllegalArgumentException("체크인 날짜가 누락되었습니다.");
            }
            if (!paymentData.containsKey("checkoutDate") || paymentData.get("checkoutDate") == null) {
                throw new IllegalArgumentException("체크아웃 날짜가 누락되었습니다.");
            }

            // 예약 데이터 저장
            Reservation reservation = new Reservation(
                    Long.valueOf((Integer) paymentData.get("userId")), // Integer -> Long 변환
                    (String) paymentData.get("roomId"),
                    LocalDate.parse((String) paymentData.get("checkinDate")),
                    LocalDate.parse((String) paymentData.get("checkoutDate")),
                    (Integer) paymentData.get("adultCount"),
                    (Integer) paymentData.get("childCount"),
                    amount
            );

            reservationRepository.save(reservation);
            System.out.println("예약이 성공적으로 저장되었습니다. ID: " + reservation.getId());

            // 저장된 Reservation 객체 반환
            return reservation;

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("예약 데이터 저장 중 오류 발생", e);
        }
    }
}