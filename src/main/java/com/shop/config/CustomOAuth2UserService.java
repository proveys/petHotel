package com.shop.config;

import com.shop.dto.SessionUser;
import com.shop.entity.User;
import com.shop.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

   @Autowired
   private UserRepository userRepository;

   @Autowired
   private HttpSession httpSession;

   @Override
   public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException{
      OAuth2UserService oAuth2UserService = new DefaultOAuth2UserService();
      OAuth2User oAuth2User = oAuth2UserService.loadUser(oAuth2UserRequest);

      //naver, kakao, google id

      String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
      System.out.println("registarionId : "+registrationId);

      String userNameAttributeName = oAuth2UserRequest.getClientRegistration().getProviderDetails()
              .getUserInfoEndpoint().getUserNameAttributeName();

      System.out.println("userNameAtrributeName: "+userNameAttributeName);
      OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName
              ,oAuth2User.getAttributes());

      User user = saverOrUpdate(attributes); // DB 저장

      httpSession.setAttribute("user",new SessionUser(user)); //로그인 세팅

      return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
      ,attributes.getAttributes()
              ,attributes.getNameAttributeKey()
      );
   }

   private User saverOrUpdate(OAuthAttributes attributes){
      User user =userRepository.findByEmail(attributes.getEmail())
              .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
              .orElse(attributes.toEntity());

      return userRepository.save(user);
   }
}
