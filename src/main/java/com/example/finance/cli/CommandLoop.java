package com.example.finance.cli;

import com.example.finance.core.model.OperationType;
import com.example.finance.core.services.AuthService;
import com.example.finance.core.services.TransferService;
import com.example.finance.core.services.WalletService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CommandLoop {
    private final AuthService auth;
    private final WalletService wallet;
    private final TransferService transfer;

    public CommandLoop(AuthService auth, WalletService wallet, TransferService transfer) {
        this.auth = auth;
        this.wallet = wallet;
        this.transfer = transfer;
    }

    public void run() {
        System.out.println("Finance CLI запущен. Введите 'help' для списка команд.");
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("> ");
            String line = sc.nextLine();
            if (line == null) break;

            line = line.trim();
            if (line.isEmpty()) continue;

            try {
                String[] parts = line.split("\\s+");
                String cmd = parts[0].toLowerCase();
                String[] args = Arrays.copyOfRange(parts, 1, parts.length);

                switch (cmd) {
                    case "help" -> printHelp();

                    case "register" -> {
                        requireArgs(args, 2, "register <login> <password>");
                        auth.register(args[0], args[1]);
                        System.out.println("Пользователь зарегистрирован: " + args[0]);
                    }

                    case "login" -> {
                        requireArgs(args, 2, "login <login> <password>");
                        auth.login(args[0], args[1]);
                        System.out.println("Вход выполнен: " + args[0]);
                    }

                    case "logout" -> {
                        auth.logoutAndSave();
                        System.out.println("Вы вышли из аккаунта. Данные сохранены.");
                    }

                    case "whoami" -> {
                        var u = auth.getCurrentUserOrNull();
                        System.out.println(u == null ? "(не авторизован)" : ("Вы: " + u.getLogin()));
                    }

                    case "add-category" -> {
                        requireArgs(args, 1, "add-category <name>");
                        var u = auth.requireUser();
                        wallet.addCategory(u.getWallet(), joinFrom(args, 0));
                        System.out.println("Категория добавлена.");
                    }

                    case "set-budget" -> {
                        requireArgs(args, 2, "set-budget <category> <limit>");
                        var u = auth.requireUser();
                        wallet.setBudget(u.getWallet(), args[0], parseDouble(args[1], "limit"));
                        System.out.println("Бюджет установлен.");
                    }

                    case "add-income" -> addOp(OperationType.INCOME, args);
                    case "add-expense" -> addOp(OperationType.EXPENSE, args);

                    case "sum-income" -> {
                        requireArgs(args, 1, "sum-income <cat1,cat2,...>");
                        var u = auth.requireUser();
                        var cats = wallet.parseCategoriesCsv(args[0]);
                        double sum = wallet.sumByCategories(u.getWallet(), OperationType.INCOME, cats);
                        System.out.println("Сумма доходов по категориям " + cats + ": " + sum);
                    }

                    case "sum-expense" -> {
                        requireArgs(args, 1, "sum-expense <cat1,cat2,...>");
                        var u = auth.requireUser();
                        var cats = wallet.parseCategoriesCsv(args[0]);
                        double sum = wallet.sumByCategories(u.getWallet(), OperationType.EXPENSE, cats);
                        System.out.println("Сумма расходов по категориям " + cats + ": " + sum);
                    }

                    case "transfer" -> {
                        requireArgs(args, 2, "transfer <toLogin> <amount> [note...]");
                        var from = auth.requireUser();
                        String toLogin = args[0];
                        double amount = parseDouble(args[1], "amount");
                        String note = args.length >= 3 ? joinFrom(args, 2) : "";
                        transfer.transfer(from, toLogin, amount, note);
                        System.out.println("Перевод выполнен.");
                    }

                    case "list-categories" -> {
                        var u = auth.requireUser();
                        var cats = new java.util.ArrayList<>(u.getWallet().getCategories());
                        cats.sort(String::compareTo);

                        if (cats.isEmpty()) {
                            System.out.println("(категорий нет)");
                        } else {
                            System.out.println("Категории:");
                            for (String c : cats) System.out.println("  - " + c);
                        }
                    }

                    case "list-budgets" -> {
                        var u = auth.requireUser();
                        var budgets = u.getWallet().getBudgetsByCategory();

                        if (budgets.isEmpty()) {
                            System.out.println("(бюджетов нет)");
                        } else {
                            var remaining = wallet.remainingBudgetByCategory(u.getWallet());
                            var keys = new java.util.ArrayList<>(budgets.keySet());
                            keys.sort(String::compareTo);

                            System.out.println("Бюджеты:");
                            for (String cat : keys) {
                                double limit = budgets.get(cat);
                                double rem = remaining.getOrDefault(cat, limit);
                                System.out.println("  " + cat + ": " + limit + ", остаток: " + rem);
                            }
                        }
                    }

                    case "stats" -> {
                        var u = auth.requireUser();
                        System.out.println(wallet.buildStatsReport(u.getWallet()));
                    }

                    case "stats-period" -> {
                        requireArgs(args, 2, "stats-period <from:YYYY-MM-DD> <to:YYYY-MM-DD>");
                        var u = auth.requireUser();
                        LocalDate from = parseIsoDate(args[0]);
                        LocalDate to = parseIsoDate(args[1]);
                        System.out.println(wallet.buildStatsReport(u.getWallet(), from, to));
                    }

                    case "export-stats" -> {
                        requireArgs(args, 1, "export-stats <filepath>");
                        var u = auth.requireUser();

                        String pathStr = joinFrom(args, 0);
                        Path path = Path.of(pathStr);

                        Path parent = path.getParent();
                        if (parent != null) Files.createDirectories(parent);

                        String report = wallet.buildStatsReport(u.getWallet());
                        Files.writeString(path, report);

                        System.out.println("Отчёт сохранён в файл: " + pathStr);
                    }

                    case "exit" -> {
                        auth.logoutAndSave();
                        System.out.println("Данные сохранены. Выход.");
                        return;
                    }

                    default -> System.out.println("Неизвестная команда. Введите 'help'.");
                }

            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void addOp(OperationType type, String[] args) {
        requireArgs(args, 2,
                (type == OperationType.INCOME ? "add-income" : "add-expense") + " <category> <amount> [note...]");

        var u = auth.requireUser();
        String category = args[0];
        double amount = parseDouble(args[1], "amount");
        String note = args.length >= 3 ? joinFrom(args, 2) : "";

        List<String> alerts = (type == OperationType.INCOME)
                ? wallet.addIncome(u.getWallet(), category, amount, note)
                : wallet.addExpense(u.getWallet(), category, amount, note);

        System.out.println("Операция добавлена.");
        for (String a : alerts) System.out.println(a);
    }

    private void printHelp() {
        System.out.println("""
Команды:
  help
  register <login> <password>
  login <login> <password>
  logout
  whoami

  add-category <name>
  set-budget <category> <limit>

  add-income <category> <amount> [note...]
  add-expense <category> <amount> [note...]

  sum-income <cat1,cat2,...>
  sum-expense <cat1,cat2,...>

  transfer <toLogin> <amount> [note...]

  list-categories
  list-budgets

  stats
  stats-period <from:YYYY-MM-DD> <to:YYYY-MM-DD>
  export-stats <filepath>
  exit
""");
    }

    private static void requireArgs(String[] args, int min, String usage) {
        if (args.length < min) throw new IllegalArgumentException("Использование: " + usage);
    }

    private static double parseDouble(String s, String field) {
        try {
            double v = Double.parseDouble(s.replace(",", "."));
            if (Double.isNaN(v) || Double.isInfinite(v)) throw new NumberFormatException();
            return v;
        } catch (Exception e) {
            throw new IllegalArgumentException("Некорректное число для " + field + ": " + s);
        }
    }

    private static String joinFrom(String[] arr, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < arr.length; i++) {
            if (i > start) sb.append(" ");
            sb.append(arr[i]);
        }
        return sb.toString().trim();
    }

    private static LocalDate parseIsoDate(String s) {
        try {
            return LocalDate.parse(s.trim()); // YYYY-MM-DD
        } catch (Exception e) {
            throw new IllegalArgumentException("Некорректная дата: " + s + " (нужно YYYY-MM-DD)");
        }
    }
}
