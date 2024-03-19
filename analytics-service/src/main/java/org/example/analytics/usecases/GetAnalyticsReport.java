package org.example.analytics.usecases;

import lombok.RequiredArgsConstructor;
import org.example.analytics.queries.CompanyIncomeQuery;
import org.example.analytics.queries.MostExpensiveTaskQuery;
import org.example.analytics.queries.NegativeBalanceAccountsQuery;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class GetAnalyticsReport {
    private final CompanyIncomeQuery companyIncomeQuery;
    private final MostExpensiveTaskQuery mostExpensiveTaskQuery;
    private final NegativeBalanceAccountsQuery negativeBalanceAccountsQuery;

    @GetMapping("/report")
    @PreAuthorize("hasRole('ADMIN')")
    public Report getReportForDates(@RequestParam(required = false) LocalDate startDate,
                                    @RequestParam(required = false) LocalDate endDate) {
        startDate = Optional.ofNullable(startDate).orElse(LocalDate.EPOCH);
        endDate = Optional.ofNullable(endDate).orElse(LocalDate.now());

        return new Report(
                startDate,
                endDate,
                companyIncomeQuery.execute(startDate, endDate),
                mostExpensiveTaskQuery.execute(startDate, endDate),
                new Report.BalancesReport(
                        negativeBalanceAccountsQuery.execute(startDate),
                        negativeBalanceAccountsQuery.execute(endDate)));

    }

    public record Report(LocalDate startDate,
                         LocalDate endDate,
                         CompanyIncomeQuery.QueryResult incomeReport,
                         MostExpensiveTaskQuery.QueryResult mostExpensiveTaskReport,
                         BalancesReport negativeBalancesReport) {
        public record BalancesReport(NegativeBalanceAccountsQuery.QueryResult startOfPeriod,
                                     NegativeBalanceAccountsQuery.QueryResult endOfPeriod) {
        }
    }
}
