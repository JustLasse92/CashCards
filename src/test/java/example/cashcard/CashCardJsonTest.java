package example.cashcard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest // Markiert den Test für das Jackson Framework (wird in Spring-Boot mitgegeben)
public class CashCardJsonTest {

    // JacksonTester ist ein Wrapper für die Jackson JSON parsing libary.
    @Autowired
    private JacksonTester<CashCard> json;
    @Autowired
    private JacksonTester<List<CashCard>> jsonList;


    @Test
    void cashCardSerializationTest() throws IOException {
        CashCard cashCard = new CashCard(99L, 123.45, "owner1");
        assertThat(json.write(cashCard)).isStrictlyEqualToJson("single.json");
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.id");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.id").isEqualTo(99);
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.amount");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.amount").isEqualTo(123.45);
    }

    @Test
    void cashCardDeserializationTest() throws IOException {
        String expected = """
                { "id": 99, "amount": 123.45, "owner": "owner1" }
                """;
        assertThat(json.parse(expected).getObject())
                .isEqualTo(new CashCard(99L, 123.45, "owner1"));
        assertThat(json.parseObject(expected).getId()).isEqualTo(99);
        assertThat(json.parseObject(expected).getAmount()).isEqualTo(123.45);
    }

    @Test
    void cashCardListSerializationTest() throws IOException {
        List<CashCard> cashCards = Arrays.asList(new CashCard(99L, 123.45, "owner1"),
                new CashCard(100L, 1.00, "owner1"),
                new CashCard(101L, 150.00, "owner1"));
        assertThat(jsonList.write(cashCards)).isStrictlyEqualToJson("list.json");
    }
}
