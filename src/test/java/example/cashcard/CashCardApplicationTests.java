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

    public static final String USERNAME_1 = "owner1";
    public static final String USERNAME_2 = "owner2";
    public static final String USERNAME_HANK = "hank-owns-no-cards";
    private static final List<Long> INSERTED_IDS = List.of(99L, 100L, 101L);
    private static final List<Double> INSERTED_AMOUNTS = List.of(223.45, 123.45, 323.45);
    private static final String PASSWORD_1 = "12345";
    private static final String PASSWORD_2 = "22345";
    private static final String PASSWORD_HANK = "54321";
    private static final List<String> INSERTED_OWNERS = List.of(USERNAME_1, USERNAME_2, USERNAME_HANK);
    private static final List<CashCard> INSERTED_CASH_CARDS;

    static {
        INSERTED_CASH_CARDS = new ArrayList<>();
        for (int i = 0; i < INSERTED_IDS.size(); i++) {
            CashCard cashCard = CashCard.builder()
                    .id(INSERTED_IDS.get(i))
                    .amount(INSERTED_AMOUNTS.get(i))
                    .owner(INSERTED_OWNERS.get(i))
                    .build();
            INSERTED_CASH_CARDS.add(cashCard);
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
        ResponseEntity<CashCard> response = restTemplate
                .withBasicAuth(USERNAME_1, PASSWORD_1)
                .getForEntity("/cashcards/" + INSERTED_CASH_CARDS.get(0).getId(), CashCard.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CashCard cashCard = response.getBody();
        assertThat(cashCard).isEqualTo(INSERTED_CASH_CARDS.get(0));
    }

    @Test
    void shouldReturnAllCashCardsWhenListIsRequested() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(USERNAME_1, PASSWORD_1)
                .getForEntity("/cashcards", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Mit den JSONPath-Expressions können JSON-Dokumente analysiert werden
        DocumentContext documentContext = JsonPath.parse(response.getBody());
        // $.length() liefert die Anzahl der Elemente im Array zurück
        int cashCardCount = documentContext.read("$.length()");
        // Für den User sind nur zwei Karten sichtbar
        assertThat(cashCardCount).isEqualTo(2);

        // $..id liefert alle id-Werte in einem Array zurück
        JSONArray ids = documentContext.read("$..id");
        // InAnyOrder prüft nicht die Reihenfolge
        assertThat(ids).containsExactlyInAnyOrder(INSERTED_IDS.get(0).intValue(), INSERTED_IDS.get(1).intValue());

        // $..amount liefert alle amount-Werte in einem Array zurück
        JSONArray amounts = documentContext.read("$..amount");
        assertThat(amounts).containsExactlyInAnyOrder(INSERTED_AMOUNTS.get(0), INSERTED_AMOUNTS.get(1));
    }


    @Test
    void shouldReturnAPageOfCashCards() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(USERNAME_1, PASSWORD_1)
                .getForEntity("/cashcards?page=0&size=1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        // $[*] liefert alle Elemente des Arrays zurück
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnASortedPageOfCashCards() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(USERNAME_1, PASSWORD_1)
                .getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray read = documentContext.read("$[*]");
        assertThat(read.size()).isEqualTo(1);

        double amount = documentContext.read("$[0].amount");
        assertThat(amount).isEqualTo(INSERTED_AMOUNTS.get(0));
    }

    @Test
    void shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(USERNAME_1, PASSWORD_1)
                .getForEntity("/cashcards", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(2);

        JSONArray amounts = documentContext.read("$..amount");
        assertThat(amounts).containsExactly(INSERTED_AMOUNTS.get(1), INSERTED_AMOUNTS.get(0));
    }

    @Test
    void createNewCashCardTest() {
        CashCard requestedCashCard = new CashCard(null, 250.0, "someOtherUser");
        ResponseEntity<CashCard> response = restTemplate
                .withBasicAuth(USERNAME_1, PASSWORD_1)
                .postForEntity("/cashcards", requestedCashCard, CashCard.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        CashCard cashCard = response.getBody();
        assertNotNull(cashCard);
        assertNotNull(cashCard.getId());
        assertThat(response.getHeaders().getLocation() != null);
        assertThat(response.getHeaders().getLocation().toString().equals("/cashcards/" + cashCard.getId()));
        assertThat(cashCard.getAmount()).isEqualTo(0);
        assertThat(cashCard.getOwner()).isEqualTo(USERNAME_1);

        ResponseEntity<CashCard> responseForCreatedEntity = restTemplate
                .withBasicAuth(USERNAME_1, PASSWORD_1)
                .getForEntity(response.getHeaders().getLocation(), CashCard.class);
        assertThat(responseForCreatedEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void balanceTest() {
        Map<String, String> map = new HashMap<>();
        double addedAmount = 30.0;
        double expectedTotalAmount = 30.0 + INSERTED_CASH_CARDS.get(1).getAmount();
        map.put("amount", String.valueOf(addedAmount));

        ResponseEntity<CashCard> getResponse = restTemplate
                .withBasicAuth(USERNAME_1, PASSWORD_1)
                .postForEntity("/cashcards/balance/" + INSERTED_CASH_CARDS.get(1).getId(), map, CashCard.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        CashCard balancedCashCard = getResponse.getBody();
        assertNotNull(balancedCashCard);
        assertEquals(INSERTED_CASH_CARDS.get(1).getId(), balancedCashCard.getId());
        assertEquals(expectedTotalAmount, balancedCashCard.getAmount());
    }

    @Test
    void shouldNotReturnACashCardWithAnUnknownId() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(USERNAME_1, PASSWORD_1)
                .getForEntity("/cashcards/1000", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }

    @Test
    void shouldNotReturnACashCardWhenUsingBadPassword() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(USERNAME_1, "XXXXX")
                .getForEntity("/cashcards/" + INSERTED_IDS.get(0), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isBlank();
    }

    @Test
    void shouldNotReturnACashCardWhenUsingBadUsername() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("XXXX", PASSWORD_1)
                .getForEntity("/cashcards/" + INSERTED_IDS.get(0), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isBlank();
    }

    @Test
    void shouldReturnACashCardWhenUserHasAccess() {
        ResponseEntity<CashCard> response = restTemplate
                .withBasicAuth(USERNAME_1, PASSWORD_1)
                .getForEntity("/cashcards/" + INSERTED_IDS.get(0), CashCard.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CashCard cashCard = response.getBody();
        assertNotNull(cashCard);
        assertThat(cashCard.getOwner()).isEqualTo(USERNAME_1);
        assertThat(cashCard.getId()).isEqualTo(INSERTED_IDS.get(0));
        assertThat(cashCard.getAmount()).isEqualTo(INSERTED_AMOUNTS.get(0));
    }

    @Test
    void shouldNotReturnACashCardWhenUserHasNoAccess() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(USERNAME_1, PASSWORD_1)
                .getForEntity("/cashcards/" + INSERTED_IDS.get(2), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }


}
