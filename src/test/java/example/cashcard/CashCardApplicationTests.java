package example.cashcard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// Write Operationen in den Tests verändern den ApplicationContext.
// Damit die Tests unabhängig voneinander sind, wird der Context nach jeder Test-Methode zurückgesetzt
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CashCardApplicationTests {

    private static final List<Long> INSERTED_IDS = List.of(99L, 100L, 101L);
    private static final List<Double> INSERTED_AMOUNTS = List.of(223.45, 123.45, 323.45);
    private static final List<CashCard> INSERTED_CASH_CARDS;

    static {
        INSERTED_CASH_CARDS = new ArrayList<>();
        for (int i = 0; i < INSERTED_IDS.size(); i++) {
            INSERTED_CASH_CARDS.add(CashCard.builder().id(INSERTED_IDS.get(i)).amount(INSERTED_AMOUNTS.get(i)).build());
        }
    }

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
        ResponseEntity<CashCard> response = restTemplate.getForEntity("/cashcards/" + INSERTED_CASH_CARDS.get(0).getId(), CashCard.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CashCard cashCard = response.getBody();
        assertThat(cashCard).isEqualTo(INSERTED_CASH_CARDS.get(0));
    }

    @Test
    void shouldReturnAllCashCardsWhenListIsRequested() {
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Mit den JSONPath-Expressions können JSON-Dokumente analysiert werden
        DocumentContext documentContext = JsonPath.parse(response.getBody());
        // $.length() liefert die Anzahl der Elemente im Array zurück
        int cashCardCount = documentContext.read("$.length()");
        assertThat(cashCardCount).isEqualTo(INSERTED_CASH_CARDS.size());

        // $..id liefert alle id-Werte in einem Array zurück
        JSONArray ids = documentContext.read("$..id");
        // InAnyOrder prüft nicht die Reihenfolge
        assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);

        // $..amount liefert alle amount-Werte in einem Array zurück
        JSONArray amounts = documentContext.read("$..amount");
        assertThat(amounts).containsExactlyInAnyOrder(123.45, 223.45, 323.45);
    }


    @Test
    void shouldReturnAPageOfCashCards() {
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards?page=0&size=1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        // $[*] liefert alle Elemente des Arrays zurück
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnASortedPageOfCashCards() {
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray read = documentContext.read("$[*]");
        assertThat(read.size()).isEqualTo(1);

        double amount = documentContext.read("$[0].amount");
        assertThat(amount).isEqualTo(323.45);
    }

    @Test
    void shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() {
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(3);

        JSONArray amounts = documentContext.read("$..amount");
        assertThat(amounts).containsExactly(123.45, 223.45, 323.45);
    }

    @Test
    void createNewCashCardTest() {
        ResponseEntity<CashCard> response = restTemplate.postForEntity("/cashcards", null, CashCard.class);
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
        double expectedTotalAmount = 30.0 + INSERTED_CASH_CARDS.get(1).getAmount();
        map.put("amount", String.valueOf(addedAmount));

        ResponseEntity<CashCard> getResponse = restTemplate.postForEntity("/cashcards/balance/" + INSERTED_CASH_CARDS.get(1).getId(), map, CashCard.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        CashCard balancedCashCard = getResponse.getBody();
        assertNotNull(balancedCashCard);
        assertEquals(INSERTED_CASH_CARDS.get(1).getId(), balancedCashCard.getId());
        assertEquals(expectedTotalAmount, balancedCashCard.getAmount());
    }

    @Test
    void shouldNotReturnACashCardWithAnUnknownId() {
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/1000", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }


}
