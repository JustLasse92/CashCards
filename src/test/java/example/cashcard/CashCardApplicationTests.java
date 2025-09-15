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

import static org.assertj.core.api.Assertions.assertThat;
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
    void findByIdTest() {

    }

    @Test
    void shouldReturnACashCardWhenDataIsSaved() {
        Double amount = 5452.45;
        CashCard cashCard = new CashCard(-1L, amount);

//        ResponseEntity<CashCard> addResponse = restTemplate.postForEntity("/cashcards/add?id=" + id + "&amount=" + amount, CashCard.class);
        ResponseEntity<CashCard> addResponse = restTemplate.postForEntity("/cashcards/addF", cashCard, CashCard.class);
        assertThat(addResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        CashCard addedCashCard = addResponse.getBody();
        assertNotNull(addedCashCard);
        assertNotNull(addedCashCard.id());
        assertEquals(amount, addedCashCard.amount());

        Long id = addedCashCard.id();
        ResponseEntity<CashCard> getResponse = restTemplate.getForEntity("/cashcards/" + id, CashCard.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        CashCard getCashCard = getResponse.getBody();
        assertNotNull(getCashCard);
        assertEquals(id, getCashCard.id());
        assertEquals(amount, addedCashCard.amount());
    }

    @Test
    void shouldNotReturnACashCardWithAnUnknownId() {
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/1000", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }


}
