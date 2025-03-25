package com.shop.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@Builder
@ToString
public class CrawlingDto {
    private String image;
    private String price;
    private String subject;
    private String review;
    private String star;
    private String url;
}
