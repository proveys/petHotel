package com.shop.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter


public class PointDto {
   @NotNull
private String pointId;


   private int point;



}
