package org.example.accounting.usecases;

import lombok.RequiredArgsConstructor;
import org.example.accounting.queries.CompanyStatementQuery;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class GetCompanyStatement {
    private final CompanyStatementQuery companyStatementQuery;

    @GetMapping("/company/statement")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'ADMIN')")
    public CompanyStatementQuery.Result getCompanyStatement(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return companyStatementQuery.execute(startDate, endDate);
    }
}
