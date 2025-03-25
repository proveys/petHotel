package com.shop.controller;

import com.shop.dto.CrawlingDto;
import com.shop.service.CrawlingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CrawlingController {
    private final CrawlingService crawlingService;

    // 크롤링 데이터를 화면에 표시
    @GetMapping("/crawling")
    public String displayCrawlingData(Model model) {
        log.info("크롤링 조회 프로그램을 시작합니다.");
        List<CrawlingDto> crawlingDtoList = crawlingService.getSavedData();
        log.info("저장된 데이터 : {}", crawlingDtoList);  // 크롤링된 데이터가 있는지 확인
        model.addAttribute("crawlingDtoList", crawlingDtoList);
        return "crawling/start";
    }

    @GetMapping("/crawling/update")
    public ResponseEntity<String> handleGetRequestForUpdate() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body("GET 요청은 지원되지 않습니다. POST 요청만 가능합니다.");
    }

    // 크롤링 데이터를 업데이트
    @PostMapping("/crawling/update")
    public String updateCrawlingData(Model model) {
        log.info("업데이트를 위한 크롤링 프로세스를 실행");
        List<CrawlingDto> crawlingDtoList = crawlingService.getCrawlingData();  // 크롤링 실행

        // 크롤링 데이터가 정상적으로 들어갔는지 확인
        if (crawlingDtoList.isEmpty()) {
            log.error("크롤링된 데이터가 없습니다.");
        } else {
            log.info("크롤링된 데이터 수 : {}", crawlingDtoList.size());
        }

        crawlingService.saveCrawlingData(crawlingDtoList);  // DB 업데이트
        model.addAttribute("crawlingDtoList", crawlingDtoList);
        log.info("크롤링 과 업데이트가 완료되었습니다.");
        return "redirect:/crawling";  // 업데이트 후 목록 페이지로 리다이렉트
    }

    @GetMapping("/crawling/api")
    @ResponseBody
    public List<CrawlingDto> getCrawlingData(@RequestParam int page, @RequestParam int size) {
        return crawlingService.getPagedData(page, size);  // 크롤링 데이터를 페이지로 반환
    }

}
