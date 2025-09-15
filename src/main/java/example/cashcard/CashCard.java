package example.cashcard;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CashCard {
    private Long id;
    private Double amount;

    public void balance(Double amount) {
        this.amount += amount;
    }
}
