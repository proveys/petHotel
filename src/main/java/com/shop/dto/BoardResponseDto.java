package com.shop.dto;

import com.shop.entity.Board;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString

public class BoardResponseDto {
   @Id
   @Column(name = "board_no")
   @GeneratedValue(strategy = GenerationType.AUTO)
   private Long number;

   private String subject;

   private String content;
   private String name;
   private LocalDateTime regTime;
   private LocalDateTime updateTime;
   private String password;
   private String formattedRegTime; // 추가

   public BoardResponseDto(Board entity){
      this.number = entity.getNumber();
      this.subject = entity.getSubject();
      this.content = entity.getContent();
      this.name = entity.getName();
      this.password = entity.getPassword();
    this.regTime = entity.getRegTime();
    this.updateTime = entity.getUpdateTime();
   }


}
