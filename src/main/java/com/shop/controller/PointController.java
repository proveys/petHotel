package com.shop.controller;

import com.shop.dto.PointDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;


@Controller
@RequiredArgsConstructor
@RequestMapping("/point")
public class PointController {
   

   @GetMapping(value = "/charge")

   public String pointCharge() {
      return "point/charge";
   }

   @PostMapping(value = "/points")
   public @ResponseBody ResponseEntity points(@RequestBody @Valid PointDto pointDto, BindingResult bindingResult,
                                              Principal principal) {
      if (bindingResult.hasErrors()) {
         StringBuilder sb = new StringBuilder();
         List<FieldError> fieldErrors = bindingResult.getFieldErrors();
         for (FieldError fieldError : fieldErrors) {
            // sb.append(fieldErrors.getDefaultMessage());
         }
         return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
      }

      String email = principal.getName();
      Long pointId;

      try {
         //pointId = pointService.point(pointDto,email);

      } catch (Exception e) {
         return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
      }
      //  return new ResponseEntity<Long>(pointId,HttpStatus.OK);
      //}

 return new ResponseEntity<Long>(HttpStatus.OK);
   }
}










