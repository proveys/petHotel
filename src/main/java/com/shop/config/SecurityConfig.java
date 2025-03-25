package com.shop.config;

import com.shop.service.MemberService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity // 웹 보안을 가능하게 한다




public class SecurityConfig {


    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    MemberService memberService;

    @Bean  // 스프링 컨테이너에 올라가는 객체 (Bean)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/img/**", "favicon.ico", "/error").permitAll()
                        .requestMatchers("/", "/members/**", "/item/**", "/images/**","/map/**","/chatbot/**","/board/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/reservation/api").hasRole("USER")
                        .requestMatchers("/reservation/**").authenticated()
                        .requestMatchers("/payment/**").authenticated()
                        .anyRequest().authenticated()
                ).formLogin(formLogin -> formLogin  // form 로그인 경우 여기로 옴
                        .loginPage("/members/login")   // 로그인 페이지는 /members/login (url)
                        .defaultSuccessUrl("/",true)         // 로그인이 성공하면 "/" (url)
                        .usernameParameter("email")     // 로그인에 필요한 파라미터("email")
                        .failureUrl("/members/login/error") // 실패시 이동 url
                ).logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))
                        .logoutSuccessUrl("/")
                ).oauth2Login(oauthLogin -> oauthLogin
                        .defaultSuccessUrl("/")
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/ws/**")
                        .ignoringRequestMatchers("/chatbot/**")
                        .ignoringRequestMatchers("/payment/**")
                        .ignoringRequestMatchers("/admin/**")
                        .ignoringRequestMatchers("/reservations/**")
                        .ignoringRequestMatchers("/members/**")
                );




        //exceptionHandling 예외처리 핸들링 예외처리가 발생하면 CustomAuthenticationEntryPoint 클래스 위임
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint()));


        return http.build();
    }

    @Bean // 패스워드 암호화 해주는 객체
    public static PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Autowired
    // AuthenticationManagerBuilder -> UserDetailService를 구현 하고 그 객체 MemberService 지정과
    // 동시에 비밀번호를 암호화
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(memberService).passwordEncoder(passwordEncoder());
    }
    @Configuration
    public class WebMvcConfig implements WebMvcConfigurer {
        @Bean
        public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
            return new HiddenHttpMethodFilter();
        }
    }
}