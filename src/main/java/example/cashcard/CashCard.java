package example.cashcard;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
@Builder
public class CashCard {
    @Id
    private Long id;
    private double amount;
    private String owner;

    public void balance(double amount) {
        this.amount += amount;
    }
}
