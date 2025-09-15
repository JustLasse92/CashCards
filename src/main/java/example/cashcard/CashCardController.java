package example.cashcard;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {
    private static long nextId = 0;

    private Map<Long, CashCard> cashCards = new HashMap<>();

    // Handler-Methode
    // Path-Variable, wenn man eindeutige Ressource per ID ansprechen will
    @GetMapping("/{requestedId}")
    public ResponseEntity<CashCard> findById(@PathVariable() Long requestedId) {
        if (cashCards.containsKey(requestedId)) {
            return ResponseEntity.ok(cashCards.get(requestedId));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("balance/{requestedId}")
    public ResponseEntity<CashCard> balance(@PathVariable() Long requestedId, @RequestBody() Double amount) {
        if (!cashCards.containsKey(requestedId)) {
            return ResponseEntity.notFound().build();
        }
        CashCard cashCard = cashCards.get(requestedId);
        cashCard.balance(amount);
        return ResponseEntity.ok(cashCard);
    }

    @PostMapping("create")
    public ResponseEntity<CashCard> create() {
        long id = ++nextId;
        while (cashCards.containsKey(id)) {
            id = ++nextId;
        }
        CashCard cashCard = new CashCard(id, 0.0);
        cashCards.put(id, cashCard);
        return ResponseEntity.ok(cashCard);
    }

    @PostMapping("insert")
    public ResponseEntity<CashCard> insert(@RequestBody() CashCard cashCard) {
        if (cashCards.containsKey(cashCard.getId())) {
            return ResponseEntity.badRequest().build();
        }

        cashCards.put(cashCard.getId(), cashCard);
        return ResponseEntity.ok(cashCard);
    }
}
