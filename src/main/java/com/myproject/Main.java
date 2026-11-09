package com.myproject;

import com.myproject.model.Student;
import com.myproject.repository.RepositoryException;
import com.myproject.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Консольный клиент. Отключён по умолчанию: активен только в Spring-профиле "cli".
 */
@Component
@Profile("cli")
public class Main implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private final StudentService service;
    private final List<MenuItem> menuItems;

    public Main(StudentService service) {
        this.service = service;
        this.menuItems = buildMenuItems();
    }

    public static void main(String[] args) {
        String[] springArgs = args.length > 0 ? args : new String[] {"--spring.profiles.active=cli"};
        StudentManagerApplication.main(springArgs);
    }

    @Override
    public void run(String... args) {
        run(new Scanner(System.in));
    }

    public void run(Scanner scanner) {
        log.info("=== МЕНЕДЖЕР СТУДЕНТОВ ===");
        try {
            while (true) {
                if (!scanner.hasNextLine()) {
                    log.info("Обнаружен конец ввода (EOF). Завершение работы.");
                    return;
                }
                printMenu();
                int choice = readInt(scanner, "Введите пункт меню: ");
                if (choice == 0) {
                    log.info("Выход из программы. До свидания!");
                    return;
                }
                if (choice < 1 || choice > menuItems.size()) {
                    log.info("Неверный пункт меню.");
                    continue;
                }
                MenuItem item = menuItems.get(choice - 1);
                try {
                    item.action().run(scanner);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.info("Операция прервана.");
                } catch (RepositoryException e) {
                    log.error("Ошибка базы данных: {}", e.getMessage(), e);
                } catch (IllegalArgumentException e) {
                    log.warn("Ошибка ввода: {}", e.getMessage());
                }
            }
        } catch (InputClosedException e) {
            log.info("Обнаружен конец ввода (EOF). Завершение работы.");
        }
    }

    private List<MenuItem> buildMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        items.add(new MenuItem("Заполнить коллекцию вручную", this::fillManually));
        items.add(new MenuItem("Заполнить случайными данными (Stream API)", this::fillRandom));
        items.add(new MenuItem("Вывести коллекцию из базы данных", (scanner) -> showCollection()));
        addSortItem(items, "group", "по номеру группы");
        addSortItem(items, "gpa", "по среднему баллу");
        addSortItem(items, "gradebook", "по номеру зачётной книжки");
        addSortItem(items, "even", "по чётным номерам зачётных книжек (доп.)");
        items.add(new MenuItem("Удалить студентов по номеру зачётной книжки", this::deleteByGradeBook));
        items.add(new MenuItem("Многопоточный подсчёт вхождений (доп.)", this::countOccurrences));
        return items;
    }

    private void addSortItem(List<MenuItem> items, String key, String label) {
        items.add(new MenuItem("Сортировка " + label, (scanner) -> sortCollection(key, label)));
    }

    private void printMenu() {
        StringBuilder sb = new StringBuilder();
        sb.append("========== МЕНЮ ==========\n");
        for (int i = 0; i < menuItems.size(); i++) {
            sb.append(i + 1).append(".  ").append(menuItems.get(i).label()).append('\n');
        }
        sb.append("0.  Выход\n");
        sb.append("==========================");
        log.info("\n{}", sb);
    }

    private void fillManually(Scanner scanner) {
        int n = readInt(scanner, "Введите количество студентов: ");
        if (n <= 0) {
            log.info("Количество должно быть больше 0.");
            return;
        }
        int added = 0;
        for (int i = 0; i < n; i++) {
            log.info("--- Студент #{} ---", i + 1);
            String group = readLine(scanner, "Номер группы: ");
            double gpa = readDouble(scanner, "Средний балл (1.0-5.0): ");
            int gradeBook = readInt(scanner, "Номер зачётной книжки (>0): ");
            try {
                service.addStudent(group, gpa, gradeBook);
                added++;
            } catch (IllegalArgumentException e) {
                log.warn("Ошибка валидации: {} (запись пропущена)", e.getMessage());
            }
        }
        log.info("Добавлено: {} из {}. Всего в базе: {}", added, n, service.size());
    }

    private void fillRandom(Scanner scanner) {
        int n = readInt(scanner, "Введите количество студентов для генерации: ");
        if (n <= 0) {
            log.info("Количество должно быть больше 0.");
            return;
        }
        int added = service.generateRandom(n);
        log.info("Сгенерировано: {}. Всего в базе: {}", added, service.size());
    }

    private void showCollection() {
        List<Student> students = service.snapshot();
        if (students.isEmpty()) {
            log.info("Коллекция в базе пуста.");
            return;
        }
        printSnapshot(students, "Коллекция из базы данных (размер: " + students.size() + "):");
    }

    private void sortCollection(String key, String description) {
        List<Student> sorted = service.sortedSnapshot(key);
        if (sorted.isEmpty()) {
            log.info("Коллекция в базе пуста. Нечего сортировать.");
            return;
        }
        printSnapshot(sorted, "Выполнена сортировка " + description + ":");
    }

    private void deleteByGradeBook(Scanner scanner) {
        int gradeBook = readInt(scanner, "Введите номер зачётной книжки для удаления: ");
        if (gradeBook <= 0) {
            log.info("Номер зачётной книжки должен быть положительным.");
            return;
        }
        int deleted = service.deleteByGradeBookNumber(gradeBook);
        log.info("Удалено записей с зачёткой №{}: {}", gradeBook, deleted);
    }

    private void countOccurrences(Scanner scanner) throws InterruptedException {
        if (service.size() == 0) {
            log.info("Коллекция в базе пуста.");
            return;
        }
        int target = readInt(scanner, "Введите номер зачётной книжки (N) для подсчёта: ");
        int count = service.countOccurrences(target);
        log.info("Вхождений зачётки №{}: {}", target, count);
    }

    private void printSnapshot(List<Student> students, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(title);
        int index = 0;
        for (Student student : students) {
            sb.append("\n  [").append(index++).append("] ").append(student);
        }
        log.info("\n{}", sb);
    }

    private int readInt(Scanner scanner, String prompt) {
        System.out.print(prompt);
        while (true) {
            if (!scanner.hasNextLine()) {
                throw new InputClosedException();
            }
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("Введите целое число. " + prompt);
            }
        }
    }

    private double readDouble(Scanner scanner, String prompt) {
        System.out.print(prompt);
        while (true) {
            if (!scanner.hasNextLine()) {
                throw new InputClosedException();
            }
            String line = scanner.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.print("Неверный формат числа. " + prompt);
            }
        }
    }

    private String readLine(Scanner scanner, String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new InputClosedException();
        }
        return scanner.nextLine().trim();
    }

    private interface MenuAction {
        void run(Scanner scanner) throws InterruptedException;
    }

    private record MenuItem(String label, MenuAction action) {
    }

    private static class InputClosedException extends RuntimeException {
        InputClosedException() {
            super("Входной поток закрыт (EOF)");
        }
    }
}
