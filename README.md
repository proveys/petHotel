
# 🐶 애견호텔 프로젝트

## 🎬 프로젝트 소개
애견을 위한 전용 호텔 예약 플랫폼입니다.  
사용자는 객실 조회, 예약, 수정 및 취소를 할 수 있고 관리자는 객실 및 예약 관리를 할 수 있습니다.  
간편한 UI/UX로 빠르고 직관적인 예약 서비스 제공.

## 🚀 배포환경
- AWS EC2, RDS  
- 현재 서버는 비용 문제로 비활성화 상태입니다.

## 🏆 주요 기능
- 객실 목록 조회, 예약 가능 날짜 확인  
- 회원가입, 로그인 (세션 기반)  
- 예약 등록 / 수정 / 취소  
- 마이페이지에서 예약 내역 확인  
- 관리자 페이지에서 객실 및 예약 관리  
- 애견용품 구매 및 장바구니 기능

## 🛠 사용 기술

### ⚙️ 백엔드
- Java 21  
- Spring Boot 3.4.1  
- Spring Security  
- JPA (Hibernate)  
- MySQL  
- Validation  

### 💻 프론트엔드
- HTML5 / CSS3 / JavaScript  
- Thymeleaf  
- Bootstrap  

### 🛠 기타
- Lombok  
- Spring Scheduler (자동 예약 취소 등)

## 📦 디렉토리 구조
```
src
├── main
│   ├── java/com/hc/pethotel
│   │   ├── controller
│   │   ├── service
│   │   ├── repository
│   │   └── entity
│   └── resources
│       └── application.yml
```

## 💾 기술 선정 이유
- **Spring Boot**: 빠른 개발 및 내장 톰캣 지원  
- **MySQL**: 안정적인 RDBMS  
- **Thymeleaf**: 서버사이드 렌더링 템플릿 엔진  
- **Spring Security**: 인증/인가 처리
