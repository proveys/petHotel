import com.shop.service.PortOneService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = com.shop.ShopApplication.class) // ShopApplication 명시
public class PortOneServiceTest {

   @Autowired
   private PortOneService portOneService;

   @Test
   public void testGetAccessToken() {
      // 액세스 토큰 요청
      String accessToken = portOneService.getAccessToken();

      // 액세스 토큰 출력 및 검증
      System.out.println("획득한 액세스 토큰: " + accessToken);
      assert accessToken != null && !accessToken.isEmpty() : "액세스 토큰을 가져오지 못했습니다.";
   }
}
