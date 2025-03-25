package com.shop.controller;

import com.shop.dto.BoardRequestDto;
import com.shop.dto.BoardResponseDto;
import com.shop.dto.SuccessResponseDto;
import com.shop.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/board")
@RequiredArgsConstructor
public class BoardController {
   private final BoardService boardService;

   /**
    * 게시판 목록 페이지
    */
   @GetMapping("/list")
   public String getPosts(Model model) {
      List<BoardResponseDto> posts = boardService.getPost();

      //regTime 포맷팅 추가
      posts.forEach(post -> {
         if (post.getRegTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            post.setFormattedRegTime(post.getRegTime().format(formatter));
         }
      });



      model.addAttribute("posts", posts);
      return "board/list"; // list.html 템플릿
   }

   /**
    * 게시글 작성 페이지
    */
   @GetMapping("/write")
   public String writePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
      // 로그인한 사용자의 이름 가져오기
      String username = userDetails.getUsername(); // UserDetails에서 사용자 이름 추출

      // 모델에 사용자 이름 추가
      model.addAttribute("username", username);

      return "board/write"; // write.html 템플릿 반환
   }
   /**
    * 게시글 작성 요청 처리
    */
   @PostMapping("/write")
   public String createPost(@ModelAttribute BoardRequestDto requestsDto) {
      boardService.createPost(requestsDto);
      return "redirect:/board/list"; // 작성 완료 후 목록 페이지로 리다이렉트
   }

   /**
    * 게시글 상세 페이지
    */
   @GetMapping("/{id:\\d+}")
   public String getPost(@PathVariable("id") Long id, Model model) {
      BoardResponseDto post = boardService.getPost(id);
      System.out.println("Post: "+post);
      model.addAttribute("post", post);
      System.out.println("Post number: " + post.getNumber());
      return "board/detail"; // detail.html 템플릿
   }


   /**
    * 게시글 수정 요청 처리
    */
   @GetMapping("/edit/{id}")
   public String editPage(@PathVariable("id") Long id, Model model) {
      System.out.println("Edit page ID: "+ id);//디버깅 로그
      BoardResponseDto post = boardService.getPost(id);
      model.addAttribute("post", post);
      return "board/edit"; // edit.html 템플릿
   }

   @PostMapping("/{id}/edit")
   @ResponseBody
   public ResponseEntity<String> updatePostWithPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
      try {
         // 요청 데이터에서 비밀번호와 업데이트할 내용을 추출
         String inputPassword = request.get("password");
         String subject = request.get("subject");
         String content = request.get("content");
         String name = request.get("name");

         // 기존 게시글 조회
         BoardResponseDto post = boardService.getPost(id);

         // 비밀번호 검증
         if (!post.getPassword().equals(inputPassword)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 일치하지 않습니다.");
         }

         // 요청 데이터를 DTO에 매핑 후 업데이트
         BoardRequestDto requestDto = new BoardRequestDto();
         requestDto.setSubject(subject);
         requestDto.setContent(content);
         requestDto.setName(name);
         boardService.updatePost(id, requestDto);

         return ResponseEntity.ok("게시글이 수정되었습니다.");
      } catch (Exception e) {
         e.printStackTrace();
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                 .body("게시글 수정 중 오류가 발생했습니다: " + e.getMessage());
      }
   }
//@PostMapping("/{id}/edit")
//@ResponseBody
//public ResponseEntity<String> updatePostWithPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
//   try {
//      // 요청 데이터 확인
//      System.out.println("Received request data: " + request);
//
//      String inputPassword = request.get("password");
//      String subject = request.get("subject");
//      String content = request.get("content");
//      String name = request.get("name");
//
//      BoardResponseDto post = boardService.getPost(id);
//
//      if (!post.getPassword().equals(inputPassword)) {
//         return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 일치하지 않습니다.");
//      }
//
//      BoardRequestDto requestDto = new BoardRequestDto();
//      requestDto.setSubject(subject);
//      requestDto.setContent(content);
//      requestDto.setName(name);
//      boardService.updatePost(id, requestDto);
//
//      return ResponseEntity.ok("게시글이 수정되었습니다.");
//   } catch (Exception e) {
//      e.printStackTrace();
//      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//              .body("게시글 수정 중 오류가 발생했습니다: " + e.getMessage());
//   }
//}







   /**
    * 게시글 비밀번호 검증
    */
   @PostMapping("/{id}/verify")
   @ResponseBody
   public ResponseEntity<?> verifyPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
      try {
         String inputPassword = request.get("password");
         BoardResponseDto post = boardService.getPost(id);

         if (post.getPassword().equals(inputPassword)) {
            return ResponseEntity.ok().build();
         } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
         }
      } catch (Exception e) {
         e.printStackTrace();
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                 .body("Internal Server Error: " + e.getMessage());
      }
   }


   /**
    * 게시글 삭제
    */
   @DeleteMapping("/{id}")
   @ResponseBody
   public SuccessResponseDto deletePost(@PathVariable Long id) {
      try {
         System.out.println("삭제 요청 ID: " + id);
         boardService.deletePost(id);
         System.out.println("게시글 삭제 완료 ID: " + id);
         return new SuccessResponseDto(true);
      } catch (Exception e) {
         e.printStackTrace();
         return new SuccessResponseDto(false);
      }
   }




}