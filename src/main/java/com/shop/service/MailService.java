package com.shop.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// controller쪽에서 바로 데이터베이스에 접근하여 정보를 얻고 가공해서 가져가는건 정보가 손상될 우려가 크기 때문에
// 정보 변동의 위험이 큰 로직은 service에서 진행한다.
@Service

//final, @NotNull 변수가 붙으면 자동주입(Autowired)을 해준다.
@RequiredArgsConstructor

public class MailService {

    // @AutoWired를 붙이기 싫어서 그냥 변수에 final이 있으면 그냥 붙었다 생각하면 된다.
    private final JavaMailSender javaMailSender;
    // 이메일을 전송하는 데 필요한 JavaMailSender 객체 (자동으로 주입된다)
    private static final String senderEmail = "fudnr4020@gmail.com";
    // 생성된 인증 번호를 저장 할 변수
    private static int number;

    // 랜덤으로 숫자 생성
    //6자리 숫자
    public static void creatNumber() {
        number = (int) (Math.random() * (90000)) + 100000;
    }

    //이메일 메시지를 생성하는 메서드
    // 전달받은 'mail' 파라미터는 인증을 보낼 대상 이메일 주소이다.
    public MimeMessage CreateMail(String mail) {
        // 이메일 주소가 비어있거나 null일 경우, 예외처리한다.
        if (mail == null || mail.isEmpty()) {
            throw new IllegalStateException("메일 없음"); // 이메일 주소가 없으면 예외 발생.
        }
        // 인증 번호 생성
        creatNumber();

        // javaMailSender 객체를 통해 이메일을 전송할 MimeMessage 객체 생성
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            message.setFrom(senderEmail);  // 발신자 이메일 생성
            message.setRecipients(MimeMessage.RecipientType.TO, mail); // 수신자 이메일 설정
            message.setSubject("이메일 인증"); // 이메일 제목 설정
            // 이메일 본문을 html 형식으로 설정하여 내용 생성
            String body = "";
            body += "<h3>" + "요청하신 인증 번호 입니다." + "</h3>";
            body += "<h1>" + number + "</h1>";  // 랜덤으로 생성된 인증번호
            body += "<h3>" + "감사합니다." + "</h3>";
            message.setText(body, "UTF-8", "html");
        } catch (MessagingException e) { // 이메일 생성 과정에서 예외가 발생하면 에러 출력
            e.printStackTrace();
        }
        // 생성된 이메일 메시지 객체를 반환
        return message;
    }

    public int sendMail(String mail) {
        // 실제로 이메일을 발송하는 메서드이고 mail은 컨트롤러에서 받은 수신자 이메일 주소이다.

        MimeMessage message = CreateMail(mail);  // 이메일 메시지를 생성후 message에 넣어놈
        javaMailSender.send(message); // 생성한 message를 가지고 이메일을 전송

        return number;  // 생성된 인증 번호를 반환 ajax에서 사용할 data로 반환된다.
    }
}
