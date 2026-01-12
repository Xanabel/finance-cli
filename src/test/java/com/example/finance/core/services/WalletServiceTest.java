package com.example.finance.core.services;

import com.example.finance.core.model.Operation;
import com.example.finance.core.model.OperationType;
import com.example.finance.core.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class WalletServiceTest {

    private WalletService walletService;
    private Wallet w;

    @BeforeEach
    void setUp() {
        walletService = new WalletService();
        w = new Wallet();

        // базовые категории, чтобы проходить requireCategory
        walletService.addCategory(w, "Еда");
        walletService.addCategory(w, "Зарплата");
        walletService.addCategory(w, "Такси");
        walletService.addCategory(w, "Перевод");
    }

    @Test
    void addCategory_addsTrimmed() {
        walletService.addCategory(w, "  Развлечения  ");
        assertTrue(w.getCategories().contains("Развлечения"));
    }

    @Test
    void addCategory_empty_throws() {
        assertThrows(IllegalArgumentException.class, () -> walletService.addCategory(w, "   "));
    }

    @Test
    void setBudget_ok() {
        walletService.setBudget(w, "Еда", 4000);
        assertEquals(4000.0, w.getBudgetsByCategory().get("Еда"));
    }

    @Test
    void setBudget_unknownCategory_throws() {
        assertThrows(IllegalArgumentException.class, () -> walletService.setBudget(w, "НетКатегории", 100));
    }

    @Test
    void setBudget_negative_throws() {
        assertThrows(IllegalArgumentException.class, () -> walletService.setBudget(w, "Еда", -1));
    }

    @Test
    void addIncome_ok_addsOperation() {
        walletService.addIncome(w, "Зарплата", 20000, "ок");
        assertEquals(1, w.getOperations().size());
        assertEquals(OperationType.INCOME, w.getOperations().get(0).getType());
    }

    @Test
    void addExpense_ok_addsOperation() {
        walletService.addExpense(w, "Еда", 500, "обед");
        assertEquals(1, w.getOperations().size());
        assertEquals(OperationType.EXPENSE, w.getOperations().get(0).getType());
    }

    @Test
    void addIncome_badAmount_throws() {
        assertThrows(IllegalArgumentException.class, () -> walletService.addIncome(w, "Зарплата", 0, ""));
        assertThrows(IllegalArgumentException.class, () -> walletService.addIncome(w, "Зарплата", -10, ""));
    }

    @Test
    void addExpense_unknownCategory_throws() {
        assertThrows(IllegalArgumentException.class, () -> walletService.addExpense(w, "НетКатегории", 10, ""));
    }

    @Test
    void totals_work() {
        walletService.addIncome(w, "Зарплата", 20000, "");
        walletService.addIncome(w, "Зарплата", 3000, "");
        walletService.addExpense(w, "Еда", 500, "");
        walletService.addExpense(w, "Такси", 150, "");

        assertEquals(23000.0, walletService.totalIncome(w));
        assertEquals(650.0, walletService.totalExpense(w));
    }

    @Test
    void incomeByCategory_groupsCorrectly() {
        walletService.addIncome(w, "Зарплата", 20000, "");
        walletService.addIncome(w, "Зарплата", 3000, "");

        Map<String, Double> map = walletService.incomeByCategory(w);
        assertEquals(23000.0, map.get("Зарплата"));
    }

    @Test
    void expenseByCategory_groupsCorrectly() {
        walletService.addExpense(w, "Еда", 300, "");
        walletService.addExpense(w, "Еда", 100, "");
        walletService.addExpense(w, "Такси", 200, "");

        Map<String, Double> map = walletService.expenseByCategory(w);
        assertEquals(400.0, map.get("Еда"));
        assertEquals(200.0, map.get("Такси"));
    }

    @Test
    void remainingBudgetByCategory_calculatesCorrectly() {
        walletService.setBudget(w, "Еда", 1000);
        walletService.addExpense(w, "Еда", 300, "");

        Map<String, Double> remaining = walletService.remainingBudgetByCategory(w);
        assertEquals(700.0, remaining.get("Еда"));
    }

    @Test
    void alerts_budgetExceeded_and_expensesOverIncome() {
        walletService.setBudget(w, "Еда", 100);
        // расходов больше бюджета
        var alerts1 = walletService.addExpense(w, "Еда", 150, "");
        assertTrue(alerts1.stream().anyMatch(s -> s.contains("Превышен бюджет")));

        // расходов больше доходов
        var alerts2 = walletService.addExpense(w, "Такси", 10, "");
        assertTrue(alerts2.stream().anyMatch(s -> s.contains("расходы превысили доходы")));
    }

    @Test
    void sumByCategories_multipleCategories_ok() {
        walletService.addExpense(w, "Еда", 300, "");
        walletService.addExpense(w, "Такси", 200, "");
        walletService.addExpense(w, "Еда", 100, "");

        double sum = walletService.sumByCategories(w, OperationType.EXPENSE, List.of("Еда", "Такси"));
        assertEquals(600.0, sum);
    }

    @Test
    void sumByCategories_unknownCategory_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> walletService.sumByCategories(w, OperationType.EXPENSE, List.of("Еда", "НетКатегории")));
    }

    @Test
    void statsPeriod_countsOnlyOperationsInsideRange() throws Exception {
        walletService.addIncome(w, "Зарплата", 1000, "a");
        walletService.addIncome(w, "Зарплата", 2000, "b");
        walletService.addExpense(w, "Еда", 500, "c");

        setCreatedAt(w.getOperations().get(0), LocalDateTime.of(2026, 1, 10, 12, 0));
        setCreatedAt(w.getOperations().get(1), LocalDateTime.of(2026, 2, 5, 12, 0));
        setCreatedAt(w.getOperations().get(2), LocalDateTime.of(2026, 1, 15, 12, 0));

        String reportJan = walletService.buildStatsReport(
                w,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        );

        // В январе должны попасть: income 1000 и expense 500
        assertTrue(reportJan.contains("Общий доход: 1000.0") || reportJan.contains("Общий доход: 1000"));
        assertTrue(reportJan.contains("Общие расходы: 500.0") || reportJan.contains("Общие расходы: 500"));

        // Не должно быть суммы 3000 (1000+2000) в январском отчёте
        assertFalse(reportJan.contains("Общий доход: 3000.0"));
    }

    private static void setCreatedAt(Operation op, LocalDateTime dt) throws Exception {
        Field f = op.getClass().getDeclaredField("createdAt");
        f.setAccessible(true);
        f.set(op, dt);
    }
}


