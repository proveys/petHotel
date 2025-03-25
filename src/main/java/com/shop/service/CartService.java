package com.shop.service;

import com.shop.dto.CartDetailDto;
import com.shop.dto.CartItemDto;
import com.shop.dto.CartOrderDto;
import com.shop.dto.OrderDto;
import com.shop.entity.Cart;
import com.shop.entity.CartItem;
import com.shop.entity.Item;
import com.shop.entity.Member;
import com.shop.repository.CartItemRepository;
import com.shop.repository.CartRepository;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
   private final ItemRepository itemRepository;
   private final MemberRepository memberRepository;
   private final CartRepository cartRepository;
   private final CartItemRepository cartItemRepository;
   private final OrderService orderService;

   public Long addCart(CartItemDto cartItemDto, String email){
      // Item 객체 DB에서 추출
      Item item =itemRepository.findById(cartItemDto.getItemId())
              .orElseThrow(EntityExistsException::new);
      //Member 객체 DB에서 추출
      Member member =memberRepository.findByEmail(email);
      // Member Id를 통해서 Cart 객체 추출
      Cart cart =cartRepository.findByMemberId(member.getId());
      // Cart 객체가 null이면 Cart 객체 생성 <-> 현재 로그인 된 Member
      if(cart==null){
         cart=Cart.createCart(member);
         cartRepository.save(cart);
      }
      //Cart Id와  Item Id를 넣어서 CartItem 객체를 추출
      CartItem savedCartItem =cartItemRepository.findByCartIdAndItemId(cart.getId(),item.getId());
      if(savedCartItem !=null){
         savedCartItem.addCount(cartItemDto.getCount()); //있는 객체에 수량 증가
         return  savedCartItem.getId();
      }
      //추출된 CartItem 객체가 없으면
      else {
         CartItem cartItem =CartItem.createCartItem(cart,item,cartItemDto.getCount());
         cartItemRepository.save(cartItem);
         return cartItem.getId();
      }

   }

   @Transactional(readOnly = true)
   public List<CartDetailDto> getCartList(String email){
      List<CartDetailDto> cartDetailDtoList =new ArrayList<>();

      Member member =memberRepository.findByEmail(email);

      Cart cart =cartRepository.findByMemberId(member.getId());
      if(cart==null){
         return cartDetailDtoList;
      }
      cartDetailDtoList = cartItemRepository.findCartDetailDtoList(cart.getId());
      return cartDetailDtoList;
   }

   @Transactional(readOnly = true)
   public boolean validateCartItem(Long cartItemId, String email){
      //email을 이용해서 Member 엔티티 객체 추출
      Member curMember =memberRepository.findByEmail(email);
      //cartItemId를 이용해서 CartItem 엔티티 객체 추출
      CartItem cartItem =cartItemRepository.findById(cartItemId)
              .orElseThrow(EntityExistsException::new);
      //Cart -> Member 엔티티 객체를 추출

      Member savedMember = cartItem.getCart().getMember();
      //현재 로그인된 Member == CartItem에 있는 Member -> 같지 않으면 true return false
      if(!StringUtils.equals(curMember.getEmail(),savedMember.getEmail())){
         return false;
      }
      //현재 로그인된 Member == CartItem에 있는 Member-> 같으면 return true
      return true;
   }

   public void updateCartItemCount(Long cartItemId, int count){
      CartItem cartItem = cartItemRepository.findById(cartItemId)
              .orElseThrow(EntityExistsException::new);
         cartItem.updateCount(count);
   }

   public void deleteCartItem(Long cartItemId){
         CartItem cartItem = cartItemRepository.findById(cartItemId)
                 .orElseThrow(EntityExistsException::new);
         cartItemRepository.delete(cartItem);
   }

   public Long orderCartItem(List<CartOrderDto> cartOrderDtoList, String email){
      //주문 DTO List 객체 생성
      List<OrderDto> orderDtoList =new ArrayList<>();
      //카트 주문 List에 있는 목록 -> 카트 아이템 객체로 추출
      //주문 Dto에 CartItem 정보를 담고
      //위에 선언된 주문 Dto List에 추가
      for(CartOrderDto cartOrderDto : cartOrderDtoList){
         CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId())
                 .orElseThrow(EntityNotFoundException::new);
         OrderDto orderDto = new OrderDto();
         orderDto.setItemId(cartItem.getItem().getId());
         orderDto.setCount(cartItem.getCount());
         orderDtoList.add(orderDto);

      }
      //주문DTO 리스트 현재 로그인된 이메일 매개변수 넣고
      //주문 서비스 실행 -> 주문
      Long orderId = orderService.orders(orderDtoList, email);

      //Cart 에서 있던 Item 주문이 되니까 CartItem 모두 삭제
      for(CartOrderDto cartOrderDto : cartOrderDtoList){
         CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId())
                 .orElseThrow(EntityNotFoundException::new);
         cartItemRepository.delete(cartItem);
      }
      return orderId;
   }

}
