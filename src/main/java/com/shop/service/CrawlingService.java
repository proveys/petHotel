package com.shop.service;

import com.shop.constant.ItemSellStatus;
import com.shop.dto.CrawlingDto;
import com.shop.entity.Crawling;
import com.shop.entity.Item;
import com.shop.entity.ItemImg;
import com.shop.repository.CrawlingRepository;
import com.shop.repository.ItemImgRepository;
import com.shop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CrawlingService {
    private final CrawlingRepository crawlingRepository;
    private final ItemRepository itemRepository;
    private final ItemImgRepository itemImgRepository;

    //웹사이트 상품 정보를 크롤링해서 DTO 목록으로 반환
    public List<CrawlingDto> getCrawlingData() {
        System.out.println("크롤링 데이터 수집 시작");
        List<CrawlingDto> crawlingDtoList = new ArrayList<>();

        // 크롬 드라이버 경로 설정
        System.setProperty("webdriver.chrome.driver", "C:/chromedriver-win64/chromedriver.exe");

        // ChromeDriver 옵션 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu"); // GPU 비활성화
        options.addArguments("--no-sandbox"); // Sandbox 비활성화
        options.addArguments("--headless"); // 브라우저를 화면에 표시하지 않음
        options.addArguments("--disable-dev-shm-usage"); // 메모리 사용 문제 해결
        options.addArguments("--disable-blink-features=AutomationControlled"); // 자동화 탐지 방지
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.206 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);

        try {
            // PethRoom 사이트 접속
            driver.get("https://pethroom.com/product/list.html?cate_no=202");
            Thread.sleep(2000); //브라우저 로딩될때까지 잠시 기다린다.

            // 페이지 스크롤 구현 (추가 데이터를 로드하기 위해)
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            int scrollCount = 0;
            while (scrollCount < 5) { // 최대 5번 스크롤
                long lastHeight = (long) jsExecutor.executeScript("return document.body.scrollHeight");
                jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);"); // 페이지 맨 아래로 스크롤

                // 특정 조건이 만족될 때까지 대기 (예: 이미지가 로드됨)
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
                try {
                    wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".search-product")));
                } catch (Exception e) {
                    System.out.println("No new elements found within timeout.");
                }

                // 스크롤 후 높이 비교
                long newHeight = (long) jsExecutor.executeScript("return document.body.scrollHeight");
                if (newHeight == lastHeight) { // 스크롤 높이가 변하지 않으면 종료
                    break;
                }
                scrollCount++;
            }

            // 제품 목록이 로드될 때까지 대기
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

            // 아이템 이름 크롤링 (셀렉터 수정 필요)
            List<WebElement> elements = driver.findElements(By.cssSelector("li.xans-record-"));
            System.out.println("찾은 목록 수 : " + elements.size());

            //제품 정보 추출
            for (WebElement element : elements) {
                try{
                    String image = element.findElement(By.cssSelector(".thumbnail img")).getAttribute("src");
                    String price = element.findElement(By.cssSelector(".sale_price")).getText();
                    String subject = element.findElement(By.cssSelector(".description .name a")).getText();
                    String review = element.findElement(By.cssSelector(".crmReview .snap_review_count")).getText();
                    String star = element.findElement(By.cssSelector(".snap_review_avg_score span")).getText();
                    String url = element.findElement(By.cssSelector(".description .name a")).getAttribute("href");

                    //크롤링 DTO 객체 생성 및 리스트에 추가
                    CrawlingDto crawlingDto = CrawlingDto.builder() //크롤링Dto 클래스의 빌더 패턴을 사용해서 객체 생성
                            .image(image)
                            .price(price)
                            .subject(subject)
                            .review(review)
                            .star(star)
                            .url(url)
                            .build();   //dto 객체를 최종 생성
                    crawlingDtoList.add(crawlingDto); //생성된 크롤링Dto 객체를 list에 추가

                } catch (Exception e) {
                    System.out.println("아이템 불러오는 중 오류 : " + e.getMessage());
                }
            }
            //디버깅 출력
            int count = 1;
            for (CrawlingDto dto : crawlingDtoList) { // DtoList 에 저장된 dto 객체를 하나씩 반복
                System.out.println("저장된 크롤링 데이터 : " + (count++) + dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("크롤링 중 에러 : " + e.getMessage());
        } finally {
            driver.quit(); // 드라이버 종료
        }
        //크롤링 된 DTO List 반환
        return crawlingDtoList;
    }
    //크롤링 데이터를 DB에 저장하는 메서드
    @Transactional
    public void saveCrawlingData(List<CrawlingDto> crawlingDtoList) {
        // 크롤링 데이터를 저장할 리스트
        List<Crawling> entities = new ArrayList<>();
        // 아이템 데이터를 저장할 리스트
        List<Item> items = new ArrayList<>();
        // ItemImg 리스트 추가
        List<ItemImg> itemImgs = new ArrayList<>();

        for (CrawlingDto dto : crawlingDtoList) { //크롤링된dto 리스트를 하나씩 순차적으로
            // 데이터 유효성 검사
            if (dto.getImage() == null || dto.getSubject() == null || dto.getPrice() == null || dto.getUrl() == null) {
                System.out.println("유효하지 않은 DTO : " + dto);
                continue; // 유효하지 않은 데이터는 건너뛰기
            }

            // DTO 데이터를 Crawling 엔티티로 변환
            Crawling entity = new Crawling();
            entity.setImage(dto.getImage());
            entity.setPrice(dto.getPrice());
            entity.setSubject(dto.getSubject());
            entity.setReview(dto.getReview());
            entity.setStar(dto.getStar());
            entity.setUrl(dto.getUrl());
            entities.add(entity); // Crawling 리스트에 추가

            // DTO 데이터를 Item 엔티티로 변환
            Item item = new Item();
            item.setItemNm(dto.getSubject()); // 상품명 매핑

            // price 처리 (쉼표 제거 및 숫자 변환)
            try {
                String priceString = dto.getPrice().replaceAll("[^0-9]", ""); // 숫자만 남기기
                item.setPrice(!priceString.isEmpty() ? Integer.parseInt(priceString) : 1); // 비어 있으면 1으로 처리
            } catch (NumberFormatException e) {
                System.out.println("가격 변환 중 오류 발생: " + dto.getPrice());
                item.setPrice(0); // 변환 실패 시 기본값 0으로 설정
            }
            item.setItemDetail(dto.getReview()); // 상세 설명 매핑
            item.setItemSellStatus(ItemSellStatus.SELL); // 기본 판매 상태 설정
            item.setStockNumber(100); // 기본 재고 설정
            items.add(item); // Item 리스트에 추가

            // DTO 데이터를 ItemImg 엔티티로 변환
            ItemImg itemImg = new ItemImg();
            itemImg.setImgName("default.jpg"); // 기본 이미지 이름 (원하는 값 설정)
            itemImg.setOriImgName(dto.getImage()); // 크롤링된 원본 이미지 URL
            itemImg.setImgUrl(dto.getImage()); // 이미지 URL 매핑
            itemImg.setRepImgYn("Y"); // 대표 이미지 설정
            itemImg.setItem(item); // ItemImg 엔티티가 Item 엔티티와 관계를 설정
            itemImgs.add(itemImg); // ItemImg 리스트에 추가
        }

        // Crawling 엔티티를 DB에 저장
        crawlingRepository.saveAll(entities);
        System.out.println("Crawling 엔티티 저장 성공 : " + entities);

        // Item 엔티티를 DB에 저장
        itemRepository.saveAll(items);
        System.out.println("Item 엔티티 저장 성공 : " + items);

        // ItemImg 엔티티를 DB에 저장
        itemImgRepository.saveAll(itemImgs);
        System.out.println("ItemImg 엔티티 저장 성공 : " + itemImgs);
    }




    // DB에서 저장된 크롤링 데이터를 조회하는 메서드
    public List<CrawlingDto> getSavedData() {
        // 모든 엔티티 조회
        List<Crawling> entities = crawlingRepository.findAll(); //모든 클롤링 엔티티 조회
        List<CrawlingDto> dtoList = new ArrayList<>(); //CrawlingDto는 Crawling 엔티티와 다르게, 화면에 데이터를 표시하거나 API 응답을 위한 데이터 전송 객체
        for (Crawling entity : entities) {
            // 엔티티를 DTO로 변환
            CrawlingDto dto = CrawlingDto.builder()
                    .image(entity.getImage())
                    .price(entity.getPrice())
                    .subject(entity.getSubject())
                    .review(entity.getReview())
                    .star(entity.getStar())
                    .url(entity.getUrl())
                    .build();
            dtoList.add(dto);   //변환된 CrawlingDto 객체를 dtoList 리스트에 추가
        }
        return dtoList; // 변환된 DTO 목록 반환

    }
    public List<CrawlingDto> getPagedData(int page, int size) {
        Page<Crawling> entities = crawlingRepository.findAll(PageRequest.of(page, size)); //페이지 번호와 크기를 이용해 페이징을 처리할 수 있도록 PageRequest 객체를 생성
        return entities.stream()
                //page 객체는 list와 같은 데이터를 포함, 이걸 스트림으로 변환
                // map 매서드를 사용해서 스트림의 각 요소를 변환
                .map(entity -> CrawlingDto.builder() // 각 크롤링 엔티티를 크롤링dto 로 변환
                        .image(entity.getImage())
                        .price(entity.getPrice())
                        .subject(entity.getSubject())
                        .review(entity.getReview())
                        .star(entity.getStar())
                        .url(entity.getUrl())
                        .build())
                .toList(); // 변환된 크롤링Dto 객체들을 list에 모은뒤 결과를 반환
    }
}
