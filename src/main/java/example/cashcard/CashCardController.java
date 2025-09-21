package example.cashcard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {
    @Autowired
    private ICashCardRepository cashCardRepository;


    // Handler-Methode
    // Path-Variable, wenn man eindeutige Ressource per ID ansprechen will
    @GetMapping("/{requestedId}")
    private ResponseEntity<CashCard> findById(@PathVariable() Long requestedId) {
        Optional<CashCard> cashCardOptional = cashCardRepository.findById(requestedId);
        return cashCardOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(value = "balance/{requestedId}", consumes = "application/json")
    private ResponseEntity<CashCard> balance(@PathVariable() Long requestedId, @RequestBody() BalanceRequestDto input) {
        // Statt das DTO kann man auch Map<String, String> balance verwenden nehmen
        ResponseEntity<CashCard> responseEntity = findById(requestedId);
        if (!responseEntity.getStatusCode().is2xxSuccessful() || !responseEntity.hasBody()) {
            return ResponseEntity.notFound().build();
        }
        CashCard cashCard = responseEntity.getBody();
        cashCard.balance(input.amount);
        cashCardRepository.save(cashCard);
        return ResponseEntity.ok(cashCard);
    }

    @PostMapping()
    private ResponseEntity<CashCard> create(@RequestBody CashCard cashCard, UriComponentsBuilder builder) {
        // UriComponentsBuilder wird vom IoC-Container bereitgestellt. @Autowired wird meist nur bei Feldern und eigenen
        // Methoden verwendet. In den Parametern von Handler-Methoden werden die Objekte automatisch bereitgestellt
        CashCard savedCashCard = cashCardRepository.save(cashCard);

        //  HTTP-Standard (RFC 7231) sollte eine erfolgreiche POST-Anfrage, die eine neue Ressource erstellt (Status 201 Created),
        //  im Response-Body die erzeugte Ressource zurückgeben. Zusätzlich sollte der Location-Header die URI der neuen Ressource enthalten
        URI locationOfNewCashCard = builder
                .path("cashcards/{id}")
                .buildAndExpand(savedCashCard.getId())
                .toUri();

//        return ResponseEntity.created(URI.create("/cashcards/" + cashCard.getId())).body(cashCard);
        return ResponseEntity.created(locationOfNewCashCard).body(cashCard);
    }

    @GetMapping
    // Pageable wird automatisch aus den Request-Parametern page und size befüllt
    // Standardwerte können in application.properties mit spring.data.web.pageable.* konfiguriert werden
    private ResponseEntity<List<CashCard>> findAll(Pageable pageable) {
        // Das Repository muss allerdings das Paging unterstützen (extends PagingAndSortingRepository)
        Page<CashCard> page = cashCardRepository.findAll(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        // Wenn kein Sortierparameter angegeben wurde, wird standardmäßig nach amount aufsteigend sortiert
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                ));
        return ResponseEntity.ok(page.getContent());
    }

    private record BalanceRequestDto(Double amount) {

    }
}
