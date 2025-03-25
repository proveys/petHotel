package com.shop.controller;

import com.shop.dto.MemberFormDto;
import com.shop.entity.Member;
import com.shop.service.MailService;
import com.shop.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RequestMapping("/members")
@Controller
@RequiredArgsConstructor // 오토와이드를 붙이기 싫어서
public class MemberController {
    private final MemberService memberService;
    // 붙이는 이유 :비즈니스 로직(핵심 로직)을 처리합니다.
    //예를 들어, 회원가입, 로그인, 결제, 이메일 발송 등의 로직이 여기에 있습니다.
    //필요에 따라 Repository와 소통하며, 데이터를 가져오거나 저장합니다
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;


    @GetMapping(value = "/new")
    // get방식으로 /new가 나오면 memberForm으로 나오게 해
    public String memberForm(Model model) { //
        model.addAttribute("memberFormDto", new MemberFormDto());
        return "member/memberForm";
    }

    @PostMapping(value = "/new")
    public String memberForm(@Valid MemberFormDto memberFormDto, BindingResult bindingResult, Model model) {  // 뷰에서 쓴 memberFormDto를 가져온다.  // valid는 유효성 검사 한다는 뜻이다.
        // 모델 만든 이유는 문제가 생겼을 시, 에러메시지를 담으려고 만들었다.
        // 유효성검사가 if문으로 먼저 들어간다. 만약 에러가 있으면 트루가 나온다 트루가 나오면 리턴값으로 들어간다.
        // 문제가 생긴 애를 bindingResult로 들어간다. memberForm에 있는 field 에러가 생긴다.
        if (bindingResult.hasErrors()) {  // @valid 붙은 객체를 검사해서 결과에 에러가 있으면 실행
            return "member/memberForm"; // 다시 회원가입으로 돌려보낸다 get으로
        }  // 여기서는 MemberService를 받지 않는다.
        try {
            Member member = Member.createMember(memberFormDto, passwordEncoder);  // 비밀번호까지 암호화 할 수 있게
            // Member 객체 생성(왜 CreateMember를 주는 이유 : option을 주었기 때문에 memberFormDto ))
            memberService.saveMember(member);  // 여기서 service로 돌아가게 된다.
            // 디비에 저장
        } catch (IllegalStateException e) {  //중복된 회원이 있을 경우 서비스에서 던진다.
            model.addAttribute("errorMessage", e.getMessage());
            return "member/memberForm"; //다시 회원가입으로 돌려보냅니다. GET으로  // 여기서는 service를 들어가지 않고 dto로 들어가게 된다.
        }
        return "redirect:/";  // 그냥 /를 쓰게 되면 members가 남아있기 때문에 redirect로 써야 한다.
        // 만약 문제가 하나도 없으면 main이 나온다 (/)
    }

    @GetMapping(value = "/login")  // 로그인만 있는 이유
    public String loginMember() {
        return "member/memberLoginForm";
    }

    @GetMapping(value = "/login/error")
    public String loginError(Model model) {
        model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해주세요.");
        return "member/memberLoginForm";

    }

    @PostMapping("/mail") // 클라이언트에서 메일 전송을 요청할 때 사용되는 POST 요청을 처리하는 메서드
// POST 방식으로 요청을 받아 메일을 발송하기 위해 사용한다. GET 방식이 아닌 POST 방식으로 요청을 받는 이유는 보안 상 이유로 중요한 데이터를 처리하기 위해서는 POST 방식이 안전하기 때문이다.

    // @RequestBody 어노테이션을 사용하여 클라이언트에서 JSON 형식으로 전달된 데이터를 Map<String, String> 형태로 받을 수 있다.
    // mailData는 ajax에서 key:"value" 형태로 전달된 JSON데이터를 받아 키에 해당하는 값을 사용할 수 있게 해준다.
    public @ResponseBody String MailSend(@RequestBody Map<String, String> mailData) {
        String mail = mailData.get("mail"); // mailData에서 받은 mail의 값을 mail에 넣어주었다.
        System.out.println("컨트롤러에 있는 mail : " + mail);
        int number = mailService.sendMail(mail);  // mailService의 sendMail을 이용해 메일을 전송하고, 그 결과를 member에 넣어준다.
        System.out.println("mail에 있는 number: " + number);
        String num = "" + number; //반환된 인증 번호를 String 타입으로 변환하여 반환할 수 있게 한다.
        // 인증 번호는 숫자형이므로 이를 문자열로 변환한 뒤 클라이언트에 응답으로 보낸다.

        return num;
        // 최종적으로 생성된 인증 번호를 클라이언트에게 반환한다.
        //
    }

    @GetMapping("/user-info")
    @ResponseBody
    public Map<String, Object> getCurrentUserInfo(){
        // 현재 인증된 사용자의 정보를 가져옴
        Member loggedInMember = memberService.getLoggedInMember();

        // 사용자 정보를 Map에 담아 반환합니다.
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id",loggedInMember.getId());
        userInfo.put("email",loggedInMember.getEmail());
        userInfo.put("name",loggedInMember.getName());
        userInfo.put("role",loggedInMember.getRole().toString());
        return userInfo;
    }

    @PostMapping("/members/logout")
    @ResponseBody
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // ✅ 세션 삭제
        }
        SecurityContextHolder.clearContext(); // ✅ Spring Security 컨텍스트 초기화
        return "로그아웃 성공!";
    }
}