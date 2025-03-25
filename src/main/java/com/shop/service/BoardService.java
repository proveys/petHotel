package com.shop.service;

import com.shop.dto.BoardRequestDto;
import com.shop.dto.BoardResponseDto;
import com.shop.dto.SuccessResponseDto;
import com.shop.entity.Board;
import com.shop.repository.BoardRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

   private final BoardRepository boardRepository;

   @Transactional(readOnly = true)
   public List<BoardResponseDto> getPost() {
      return boardRepository.findAllByOrderByUpdateTimeDesc().stream().map(BoardResponseDto::new).toList();

   }


   @Transactional
   public BoardResponseDto createPost(BoardRequestDto requestsDto) {
      Board board = new Board(requestsDto);
      System.out.println("Saving Board: " + board);
      board.setRegTime(LocalDateTime.now()); //등록일 설정
      boardRepository.save(board);

      Board savedBoard = boardRepository.findById(board.getNumber()).orElse(null);
      System.out.println("Saved Board in DB: " + savedBoard);

      return new BoardResponseDto(board);
   }






   @Transactional
   public BoardResponseDto getPost(Long number) {
      return boardRepository.findById(number).map(BoardResponseDto::new).orElseThrow(
              () -> new IllegalArgumentException("아이디가 존재하지 않습니다.")
      );


   }
   @Transactional
   public BoardResponseDto updatePost(Long id,BoardRequestDto requestDto) throws Exception{

      Board board = boardRepository.findById(id).orElseThrow(
              () -> new IllegalArgumentException("아이디가 존재하지 않습니다.")
      );

      board.update(requestDto);
      return new BoardResponseDto(board);
   }



   @Transactional
   public SuccessResponseDto deletePost(Long id) throws Exception {
      Board board = boardRepository.findById(id).orElseThrow(
              () -> new IllegalArgumentException("아이디가 존재하지 않습니다.")
      );

      boardRepository.deleteById(id);
      return new SuccessResponseDto(true);
   }



}