package example.cashcard;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CashCardApplicationTests {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;

    // mit @JacksonTest wird JacksonTester durch den Spring IoC initialisiert. Durch @SpringBootTest wird dies jedoch nicht als Bean initialisiert
    private JacksonTester<CashCard> json;

    @BeforeEach
    void setup() {
        JacksonTester.initFields(this, objectMapper);
    }

    @Test
    void createNewCashCardTest() {
        ResponseEntity<CashCard> response = restTemplate.postForEntity("/cashcards/create", null, CashCard.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CashCard cashCard = response.getBody();
        assertNotNull(cashCard);
        assertNotNull(cashCard.getId());
        // Reihenfolge der Tests bestimmt, welche ID vergeben wird
        //  assertEquals(0L, cashCard.getId());
        assertThat(cashCard.getAmount()).isEqualTo(0.0);
    }

    @Test
    void createAndReturnACashCardTest() {
        ResponseEntity<CashCard> response = restTemplate.postForEntity("/cashcards/create", null, CashCard.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CashCard cashCard = response.getBody();
        assertNotNull(cashCard);
        assertNotNull(cashCard.getId());

        ResponseEntity<CashCard> getResponse = restTemplate.getForEntity("/cashcards/" + cashCard.getId(), CashCard.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isEqualTo(cashCard);
    }

    @Test
    void balanceTest() {
        ResponseEntity<CashCard> response = restTemplate.postForEntity("/cashcards/create", null, CashCard.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CashCard cashCard = response.getBody();
        assertNotNull(cashCard);
        assertNotNull(cashCard.getId());
        assertThat(cashCard.getAmount()).isEqualTo(0.0);

        Map<String, String> map = new HashMap<>();
        map.put("amount", "30.0");

        ResponseEntity<CashCard> getResponse = restTemplate.postForEntity("/cashcards/balance/" + cashCard.getId(), map, CashCard.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        CashCard balancedCashCard = getResponse.getBody();
        assertNotNull(balancedCashCard);
        assertEquals(cashCard.getId(), balancedCashCard.getId());
        assertEquals(30.0, balancedCashCard.getAmount());
    }

    @Test
    void shouldNotReturnACashCardWithAnUnknownId() {
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/1000", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }


}
