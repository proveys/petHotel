package com.shop.entity;


import jakarta.persistence.*;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "cart")
@Getter
@Setter
@ToString

public class Cart  {
    @Id
    @Column(name = "cart_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)  // 앞에 One 은 Cart 뒤에 One 은 member
    @JoinColumn(name = "member_id") //JoinColumn 매핑할 외래키를 지정합니다. 외래키 이름을 설정
    // name을 명시하지 않으면 JPA 가 알아서 ID 를 찾지만 원하는 이름이 아닐 수도 있슴
    private Member member;

    public static Cart createCart(Member member){
        Cart cart =new Cart();
        cart.setMember(member);
        return cart;
    }
}
