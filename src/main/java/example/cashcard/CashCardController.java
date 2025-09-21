package example.cashcard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {
    @Autowired
    private ICashCardRepository cashCardRepository;


    // Handler-Methode
    // Path-Variable, wenn man eindeutige Ressource per ID ansprechen will
    @GetMapping("/{requestedId}")
    public ResponseEntity<CashCard> findById(@PathVariable() Long requestedId) {
        Optional<CashCard> cashCardOptional = cashCardRepository.findById(requestedId);
        if (cashCardOptional.isPresent()) {
            return ResponseEntity.ok(cashCardOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(value = "balance/{requestedId}", consumes = "application/json")
    public ResponseEntity<CashCard> balance(@PathVariable() Long requestedId, @RequestBody() BalanceRequestDto input) {
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

    @PostMapping("create")
    public ResponseEntity<CashCard> create(UriComponentsBuilder builder) {
        // UriComponentsBuilder wird vom IoC-Container bereitgestellt. @Autowired wird meist nur bei Feldern und eigenen
        // Methoden verwendet. In den Parametern von Handler-Methoden werden die Objekte automatisch bereitgestellt
        CashCard cashCard = CashCard.builder()
                .id(null)
                .amount(0.0)
                .build();

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

    @PostMapping("insert")
    public ResponseEntity<CashCard> insert(@RequestBody() CashCard cashCard) {
        if (cashCardRepository.findById(cashCard.getId()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(cashCardRepository.save(cashCard));
    }

    private record BalanceRequestDto(Double amount) {

    }
}
