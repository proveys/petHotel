package com.shop.controller;

import com.shop.dto.CartDetailDto;
import com.shop.dto.CartItemDto;
import com.shop.dto.CartOrderDto;
import com.shop.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor

public class CartController {
   private final CartService cartService;

   @PostMapping(value = "/cart")

   public @ResponseBody ResponseEntity order(@RequestBody @Valid CartItemDto cartItemDto, BindingResult bindingResult, Principal principal) {

      if (bindingResult.hasErrors()) {
         StringBuilder sb = new StringBuilder();
         List<FieldError> fieldErrors = bindingResult.getFieldErrors();
         for (FieldError fieldError : fieldErrors) {
            sb.append(fieldError.getDefaultMessage());
         }
         return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
      }
      String email = principal.getName();
      Long cartItemId;

      try {
         cartItemId = cartService.addCart(cartItemDto, email);
      } catch (Exception e) {
         return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);

      }
      return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
   }

   @GetMapping(value = "/cart")
   public String orderHist(Principal principal, Model model) {
      List<CartDetailDto> cartDetailDtoList = cartService.getCartList(principal.getName());
      model.addAttribute("cartItems", cartDetailDtoList);
      return "cart/cartList";
   }

   @PatchMapping(value = "/cartItem/{cartItemId}")
   public @ResponseBody ResponseEntity updateCartItem(@PathVariable("cartItemId") Long cartItemId,
                                                      int count, Principal principal) {
      System.out.println(cartItemId);
      if (count <= 0) {
         return new ResponseEntity<String>("최소 1개 이상 담아주세요.", HttpStatus.BAD_REQUEST);
      } else if (!cartService.validateCartItem(cartItemId, principal.getName())) {
         return new ResponseEntity<String>("수정권한이 없습니다.", HttpStatus.FORBIDDEN);
      }

      cartService.updateCartItemCount(cartItemId, count);
      return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);

   }

   @DeleteMapping(value = "/cartItem/{cartItemId}")
   public @ResponseBody ResponseEntity deleteCartItem(@PathVariable("cartItemId") Long cartItemId,
                                                      Principal principal) {
      if (!cartService.validateCartItem(cartItemId, principal.getName())) {
         return new ResponseEntity<String>("수정권한이 없습니다.", HttpStatus.FORBIDDEN);
      }
      cartService.deleteCartItem(cartItemId);
      return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
   }

   @PostMapping(value = "/cart/orders")
   public @ResponseBody ResponseEntity orderCartItem(@RequestBody CartOrderDto cartOrderDto,
                                                     Principal principal) {
      System.out.println(cartOrderDto.getCartItemId());
      //CartOrderDtoList List <- getCartOrderDtoList 통해서 views 내리는 리스트

      List<CartOrderDto> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();
      //null,size가 0이면 실행
      if (cartOrderDtoList == null || cartOrderDtoList.size() == 0) {
         return new ResponseEntity<String>("주문할 상품을 선택해주세요. ", HttpStatus.FORBIDDEN);
      }
      //전체 유효검사

      for (CartOrderDto cartOrder : cartOrderDtoList) {
         if (!cartService.validateCartItem(cartOrder.getCartItemId(), principal.getName())) {
            return new ResponseEntity<String>("주문 권한이 없습니다.", HttpStatus.FORBIDDEN);
         }
      }
      Long orderId;
      try {
         //cart ->order
         //cartService ->orderService
         //cartOrderDtoist (CartOrderDtoList)
         //cartOrderDtoList -> OrderDtoList ->OrderItem
         //order <- OrderItem
         // Order save Order 저장 OderItemList 저장

         orderId = cartService.orderCartItem(cartOrderDtoList, principal.getName());
      } catch (Exception e) { //실패하면
         return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);

      }
      //성공
      return new ResponseEntity<Long>(orderId, HttpStatus.OK);
   }
}