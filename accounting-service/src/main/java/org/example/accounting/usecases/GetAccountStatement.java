package org.example.accounting.usecases;

import lombok.RequiredArgsConstructor;
import org.example.accounting.db.UserEntity;
import org.example.accounting.db.UserEntityRepository;
import org.example.accounting.queries.AccountStatementQuery;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class GetAccountStatement {
    private final AccountStatementQuery accountStatementQuery;
    private final UserEntityRepository userEntityRepository;

    @GetMapping("/user/{userId}/statement")
    @PreAuthorize("isAuthenticated() and (authentication.principal.id == #userId or hasAnyRole('ACCOUNTANT', 'ADMIN'))")
    public AccountStatementQuery.Result getAccountStatement(
            @PathVariable UUID userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        UserEntity user = userEntityRepository.findById(userId)
                .orElseThrow(() ->
                        HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));


        UUID accountId = user.getAccountId();
        return accountStatementQuery.execute(accountId, startDate, endDate);
    }
}
