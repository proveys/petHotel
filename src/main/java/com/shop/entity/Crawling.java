package com.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "crawling")
@Getter
@Setter
public class Crawling {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String image;

    @Column(nullable = false)
    private String price;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String review;

    @Column(nullable = false)
    private String star;

    @Column(nullable = false, length = 12000)
    private String url;
}
