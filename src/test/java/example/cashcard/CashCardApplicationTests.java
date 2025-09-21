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

    private static final long[] INSERTED_IDS = {99, 100};
    private static final double[] INSERTED_AMOUNTS = {123.45, 223.45};

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
    void findCashCardTest() {
        // Die Einträge sollten durch die test/resources/data.sql initialisiert worden sein
        ResponseEntity<CashCard> response = restTemplate.getForEntity("/cashcards/" + INSERTED_IDS[0], CashCard.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CashCard cashCard = response.getBody();
        assertNotNull(cashCard);
        assertEquals(INSERTED_IDS[0], cashCard.getId());
        assertEquals(INSERTED_AMOUNTS[0], cashCard.getAmount());

    }

    @Test
    void createNewCashCardTest() {
        ResponseEntity<CashCard> response = restTemplate.postForEntity("/cashcards/create", null, CashCard.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation() != null);
        CashCard cashCard = response.getBody();
        assertNotNull(cashCard);
        assertNotNull(cashCard.getId());
        assertThat(response.getHeaders().getLocation().toString().equals("/cashcards/" + cashCard.getId()));
        assertThat(cashCard.getAmount()).isEqualTo(0.0);

        ResponseEntity<CashCard> responseForCreatedEntity = restTemplate.getForEntity(response.getHeaders().getLocation(), CashCard.class);
        assertThat(responseForCreatedEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void balanceTest() {
        Map<String, String> map = new HashMap<>();
        double addedAmount = 30.0;
        double expectedTotalAmount = 30.0 + INSERTED_AMOUNTS[1];
        map.put("amount", String.valueOf(addedAmount));

        ResponseEntity<CashCard> getResponse = restTemplate.postForEntity("/cashcards/balance/" + INSERTED_IDS[1], map, CashCard.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        CashCard balancedCashCard = getResponse.getBody();
        assertNotNull(balancedCashCard);
        assertEquals(INSERTED_IDS[1], balancedCashCard.getId());
        assertEquals(expectedTotalAmount, balancedCashCard.getAmount());
    }

    @Test
    void shouldNotReturnACashCardWithAnUnknownId() {
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/1000", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }


}
