package com.example.finance.core.services;

import com.example.finance.core.model.Operation;
import com.example.finance.core.model.OperationType;
import com.example.finance.core.model.Wallet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletService {

    public void addCategory(Wallet w, String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Название категории пустое.");
        w.getCategories().add(name.trim());
    }

    public void setBudget(Wallet w, String category, double limit) {
        requireCategory(w, category);
        validateAmount(limit, "Лимит бюджета должен быть > 0.");
        w.getBudgetsByCategory().put(category, limit);
    }

    public List<String> addIncome(Wallet w, String category, double amount, String note) {
        validateAmount(amount, "Сумма дохода должна быть > 0.");
        requireCategory(w, category);
        w.getOperations().add(new Operation(OperationType.INCOME, category, amount, safe(note)));
        return alerts(w, category);
    }

    public List<String> addExpense(Wallet w, String category, double amount, String note) {
        validateAmount(amount, "Сумма расхода должна быть > 0.");
        requireCategory(w, category);
        w.getOperations().add(new Operation(OperationType.EXPENSE, category, amount, safe(note)));
        return alerts(w, category);
    }

    public double totalIncome(Wallet w) {
        return w.getOperations().stream()
                .filter(o -> o.getType() == OperationType.INCOME)
                .mapToDouble(Operation::getAmount)
                .sum();
    }

    public double totalExpense(Wallet w) {
        return w.getOperations().stream()
                .filter(o -> o.getType() == OperationType.EXPENSE)
                .mapToDouble(Operation::getAmount)
                .sum();
    }

    public Map<String, Double> incomeByCategory(Wallet w) {
        Map<String, Double> map = new HashMap<>();
        for (Operation o : w.getOperations()) {
            if (o.getType() == OperationType.INCOME) {
                map.put(o.getCategory(), map.getOrDefault(o.getCategory(), 0.0) + o.getAmount());
            }
        }
        return map;
    }

    public Map<String, Double> expenseByCategory(Wallet w) {
        Map<String, Double> map = new HashMap<>();
        for (Operation o : w.getOperations()) {
            if (o.getType() == OperationType.EXPENSE) {
                map.put(o.getCategory(), map.getOrDefault(o.getCategory(), 0.0) + o.getAmount());
            }
        }
        return map;
    }

    public Map<String, Double> remainingBudgetByCategory(Wallet w) {
        Map<String, Double> spent = expenseByCategory(w);
        Map<String, Double> remaining = new HashMap<>();
        for (var e : w.getBudgetsByCategory().entrySet()) {
            double used = spent.getOrDefault(e.getKey(), 0.0);
            remaining.put(e.getKey(), e.getValue() - used);
        }
        return remaining;
    }

    public String buildStatsReport(Wallet w) {
        StringBuilder sb = new StringBuilder();

        sb.append("Общий доход: ").append(totalIncome(w)).append("\n");
        sb.append("Доходы по категориям:\n");
        var inc = incomeByCategory(w);
        if (inc.isEmpty()) sb.append("  (нет)\n");
        else inc.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(e -> sb.append("  ").append(e.getKey()).append(": ").append(e.getValue()).append("\n"));

        sb.append("Общие расходы: ").append(totalExpense(w)).append("\n");
        sb.append("Расходы по категориям:\n");
        var exp = expenseByCategory(w);
        if (exp.isEmpty()) sb.append("  (нет)\n");
        else exp.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(e -> sb.append("  ").append(e.getKey()).append(": ").append(e.getValue()).append("\n"));


        sb.append("Бюджет по категориям:\n");
        if (w.getBudgetsByCategory().isEmpty()) sb.append("  (нет)\n");
        else {
            var remaining = remainingBudgetByCategory(w);
            w.getBudgetsByCategory().entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .forEach(e -> {
                        String cat = e.getKey();
                        double limit = e.getValue();
                        double rem = remaining.getOrDefault(cat, limit);
                        sb.append("  ").append(cat).append(": ").append(limit)
                                .append(", Оставшийся бюджет: ").append(rem).append("\n");
                    });
        }
        return sb.toString();
    }

    public List<String> alerts(Wallet w, String category) {
        List<String> res = new ArrayList<>();

        if (w.getBudgetsByCategory().containsKey(category)) {
            double limit = w.getBudgetsByCategory().get(category);
            double spent = expenseByCategory(w).getOrDefault(category, 0.0);
            double remaining = limit - spent;
            if (remaining < 0) res.add("⚠ Превышен бюджет по категории '" + category + "' на " + (-remaining));
        }

        if (totalExpense(w) > totalIncome(w)) res.add("⚠ Общие расходы превысили доходы.");

        return res;
    }

    private static void requireCategory(Wallet w, String category) {
        if (category == null || category.trim().isEmpty())
            throw new IllegalArgumentException("Категория пустая.");
        if (!w.getCategories().contains(category))
            throw new IllegalArgumentException("Категория не найдена: " + category);
    }

    private static void validateAmount(double value, String message) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0)
            throw new IllegalArgumentException(message);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    public double sumByCategories(Wallet w, OperationType type, List<String> categories) {
        if (categories == null || categories.isEmpty())
            throw new IllegalArgumentException("Список категорий пуст.");

        // проверяем существование категорий
        for (String c : categories) {
            if (!w.getCategories().contains(c)) {
                throw new IllegalArgumentException("Категория не найдена: " + c);
            }
        }

        return w.getOperations().stream()
                .filter(o -> o.getType() == type)
                .filter(o -> categories.contains(o.getCategory()))
                .mapToDouble(Operation::getAmount)
                .sum();
    }

    public List<String> parseCategoriesCsv(String csv) {
        if (csv == null || csv.trim().isEmpty())
            throw new IllegalArgumentException("Категории не заданы.");
        String[] parts = csv.split(",");
        List<String> res = new ArrayList<>();
        for (String p : parts) {
            String c = p.trim();
            if (!c.isEmpty()) res.add(c);
        }
        if (res.isEmpty()) throw new IllegalArgumentException("Категории не заданы.");
        return res;
    }
    public String buildStatsReport(Wallet w, LocalDate from, LocalDate to) {
        if (from == null || to == null) throw new IllegalArgumentException("Даты не заданы.");
        if (from.isAfter(to)) throw new IllegalArgumentException("Дата 'from' позже даты 'to'.");

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay(); // исключающая граница

        return buildStatsReportByDateTimeRange(w, start, end);
    }

    private String buildStatsReportByDateTimeRange(Wallet w, LocalDateTime startInclusive, LocalDateTime endExclusive) {
        StringBuilder sb = new StringBuilder();

        double incTotal = w.getOperations().stream()
                .filter(o -> o.getCreatedAt() != null)
                .filter(o -> !o.getCreatedAt().isBefore(startInclusive) && o.getCreatedAt().isBefore(endExclusive))
                .filter(o -> o.getType() == OperationType.INCOME)
                .mapToDouble(Operation::getAmount).sum();

        double expTotal = w.getOperations().stream()
                .filter(o -> o.getCreatedAt() != null)
                .filter(o -> !o.getCreatedAt().isBefore(startInclusive) && o.getCreatedAt().isBefore(endExclusive))
                .filter(o -> o.getType() == OperationType.EXPENSE)
                .mapToDouble(Operation::getAmount).sum();

        sb.append("Период: ").append(startInclusive.toLocalDate()).append(" .. ").append(endExclusive.minusNanos(1).toLocalDate()).append("\n");
        sb.append("Общий доход: ").append(incTotal).append("\n");

        sb.append("Доходы по категориям:\n");
        Map<String, Double> inc = new HashMap<>();
        w.getOperations().stream()
                .filter(o -> o.getCreatedAt() != null)
                .filter(o -> !o.getCreatedAt().isBefore(startInclusive) && o.getCreatedAt().isBefore(endExclusive))
                .filter(o -> o.getType() == OperationType.INCOME)
                .forEach(o -> inc.put(o.getCategory(), inc.getOrDefault(o.getCategory(), 0.0) + o.getAmount()));

        if (inc.isEmpty()) sb.append("  (нет)\n");
        else inc.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(e -> sb.append("  ").append(e.getKey()).append(": ").append(e.getValue()).append("\n"));

        sb.append("Общие расходы: ").append(expTotal).append("\n");

        sb.append("Расходы по категориям:\n");
        Map<String, Double> exp = new HashMap<>();
        w.getOperations().stream()
                .filter(o -> o.getCreatedAt() != null)
                .filter(o -> !o.getCreatedAt().isBefore(startInclusive) && o.getCreatedAt().isBefore(endExclusive))
                .filter(o -> o.getType() == OperationType.EXPENSE)
                .forEach(o -> exp.put(o.getCategory(), exp.getOrDefault(o.getCategory(), 0.0) + o.getAmount()));

        if (exp.isEmpty()) sb.append("  (нет)\n");
        else exp.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(e -> sb.append("  ").append(e.getKey()).append(": ").append(e.getValue()).append("\n"));

        // бюджеты показываем как раньше (за весь кошелёк), а “остаток” считаем по расходам периода — так логичнее для отчёта периода
        sb.append("Бюджет по категориям (расходы за период):\n");
        if (w.getBudgetsByCategory().isEmpty()) sb.append("  (нет)\n");
        else {
            for (var e : w.getBudgetsByCategory().entrySet()) {
                String cat = e.getKey();
                double limit = e.getValue();
                double spentInPeriod = exp.getOrDefault(cat, 0.0);
                double remaining = limit - spentInPeriod;
                sb.append("  ").append(cat).append(": ").append(limit)
                        .append(", Оставшийся бюджет: ").append(remaining).append("\n");
            }
        }

        return sb.toString();
    }
}

