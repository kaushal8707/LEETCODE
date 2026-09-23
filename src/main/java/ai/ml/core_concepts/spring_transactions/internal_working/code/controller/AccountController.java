package ai.ml.core_concepts.spring_transactions.internal_working.code.controller;
import ai.ml.core_concepts.spring_transactions.internal_working.code.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/transfer")
    public String transfer(@RequestParam Long from,
                           @RequestParam Long to,
                           @RequestParam double amount) {
        accountService.transfer(from, to, amount);
        return "Transfer successful!";
    }
}
