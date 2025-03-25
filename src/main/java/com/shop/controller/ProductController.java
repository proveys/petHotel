package com.shop.controller;

import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.service.ItemService;
import com.shop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;
@Controller
@RequiredArgsConstructor
public class ProductController {
   private final ItemService itemService;
   private final MemberService memberService;

   @GetMapping(value = "/product")
   public String main(ItemSearchDto itemSearchDto, Optional<Integer> page, Model model, Authentication authentication) {
      Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 10);
      Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);
      model.addAttribute("items", items);
      model.addAttribute("itemSearchDto", itemSearchDto);
      model.addAttribute("maxPage", 5);
      return "product/productlist";
   }
}
