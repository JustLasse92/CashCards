package example.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface ICashCardRepository extends CrudRepository<CashCard, Long>, PagingAndSortingRepository<CashCard, Long> {
    // Spring Data JPA kann automatisch Implementierungen für Methoden bereitstellen, die bestimmten Namenskonventionen folgen.
    // Diese Methoden werden als "Query Methods" bezeichnet
    Optional<CashCard> findByIdAndOwner(Long id, String owner);

    Page<CashCard> findByOwner(String owner, PageRequest pageRequest);
}
