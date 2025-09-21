package example.cashcard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CashCard {
    @Id
    private Long id;
    private Double amount;

    public void balance(Double amount) {
        this.amount += amount;
    }
}
