package example.cashcard;

import org.springframework.data.repository.CrudRepository;

public interface ICashCardRepository extends CrudRepository<CashCard, Long> {
}
