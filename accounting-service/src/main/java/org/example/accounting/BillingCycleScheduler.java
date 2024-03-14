package org.example.accounting;

import lombok.RequiredArgsConstructor;
import org.example.accounting.commands.CloseBillingCyclesCommand;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class BillingCycleScheduler {

    private final CloseBillingCyclesCommand closeBillingCyclesCommand;

    @Scheduled(cron = "0 1 * * *", zone = "UTC")
    public void closeAllBillingCycles() {
        LocalDate yesterday = LocalDate.now(ZoneId.of("UTC")).minusDays(1);
        closeBillingCyclesCommand.executeFor(yesterday);
    }
}
