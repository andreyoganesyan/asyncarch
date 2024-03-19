package org.example.accounting.usecases;

import lombok.RequiredArgsConstructor;
import org.example.accounting.db.UserEntity;
import org.example.accounting.db.UserEntityRepository;
import org.example.accounting.queries.AccountBalanceQuery;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class GetAccountBalance {
    private final AccountBalanceQuery accountBalanceQuery;
    private final UserEntityRepository userEntityRepository;


    @GetMapping("/user/{userId}/balance")
    @PreAuthorize("isAuthenticated() and (authentication.principal.id == #userId or hasAnyRole('ACCOUNTANT', 'ADMIN'))")
    public AccountBalanceQuery.Result getAccountBalance(@PathVariable UUID userId) {
        UserEntity user = userEntityRepository.findById(userId)
                .orElseThrow(() ->
                        HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));


        UUID accountId = user.getAccountId();
        return accountBalanceQuery.execute(accountId);
    }
}
