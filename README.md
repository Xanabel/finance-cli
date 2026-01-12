![Tests](https://github.com/USERNAME/REPO/actions/workflows/tests.yml/badge.svg)

# Finance CLI — управление личными финансами (Java, OOP)

Консольное приложение для учёта личных финансов:
- несколько пользователей (логин/пароль),
- доходы/расходы по категориям,
- бюджеты по категориям,
- статистика (общая, по категориям, за период),
- оповещения о перерасходе,
- сохранение данных в JSON,
- экспорт отчёта в файл,
- переводы между пользователями.

## Технологии
- Java 17
- Maven
- Jackson (JSON) + jackson-datatype-jsr310 (даты)
- WalletServiceTest — покрывает добавление операций, бюджеты, подсчёты, отчёты, ошибки ввода
- TransferServiceTest — покрывает переводы и валидацию

## Структура проекта (кратко)
- `src/main/java` — код приложения
- `src/test/java` — тесты (JUnit)
- `data/` — данные пользователей и кошельков (создаётся автоматически)
    - `data/users.json` — список пользователей
    - `data/wallet-<login>.json` или аналогично (в зависимости от реализации `JsonWalletStorage`)

## Запуск приложения (IntelliJ IDEA)
1. Открой проект в IntelliJ.
2. Убедись, что установлен JDK 17 (Project SDK = 17).
3. Запусти класс:
    - `com.example.finance.Main`

После запуска появится приглашение: "Finance CLI запущен. Введите 'help' для списка команд.".


## Команды CLI

### Пользователи
- `register <login> <password>` — регистрация
- `login <login> <password>` — вход
- `logout` — выход из аккаунта (с сохранением кошелька)
- `whoami` — показать текущего пользователя

### Категории и бюджеты
- `add-category <name>` — добавить категорию
- `set-budget <category> <limit>` — установить бюджет на категорию
- `list-categories` — показать список категорий
- `list-budgets` — показать бюджеты и остаток по каждой категории

### Операции
- `add-income <category> <amount> [note...]` — добавить доход
- `add-expense <category> <amount> [note...]` — добавить расход

### Подсчёты
- `sum-income <cat1,cat2,...>` — сумма доходов по выбранным категориям
- `sum-expense <cat1,cat2,...>` — сумма расходов по выбранным категориям

### Статистика
- `stats` — полный отчёт по кошельку
- `stats-period <from:YYYY-MM-DD> <to:YYYY-MM-DD>` — отчёт за период

### Переводы между пользователями
- `transfer <toLogin> <amount> [note...]`  
  У отправителя фиксируется расход, у получателя — доход в категории `Перевод`.

### Экспорт отчёта в файл
- `export-stats <filepath>` — сохранить отчёт `stats` в файл  
  Пример:
    - `export-stats data/report.txt`

### Выход
- `exit` — сохранить данные и выйти

## Пример сценария
register xana 123
login den 456
add-category Еда
add-category Зарплата
set-budget Еда 4000
add-expense Еда 500 обед
add-income Зарплата 30000
list-categories
list-budgets
stats
export-stats data/report.txt
exit

## Оповещения
Приложение предупреждает если:
- превышен бюджет по категории,
- общие расходы превысили общие доходы.

## Где хранятся данные
Данные сохраняются в папку `data/` (создаётся автоматически).
- Пользователи: `data/users.json`
- Кошелёк пользователя: файлы JSON в `data/` (формат зависит от реализации `JsonWalletStorage`).

## Тестирование

### Запуск тестов в IntelliJ
- Открой папку `src/test/java`
- ПКМ → **Run 'All Tests'**

Тесты находятся в:
- `WalletServiceTest`
- `TransferServiceTest`

### GitHub Actions
В репозитории настроен автопрогон тестов при `push` и `pull_request`.
Workflow: `.github/workflows/tests.yml`

### Запуск тестов через Maven (если Maven доступен в системе)
```bash
mvn test

