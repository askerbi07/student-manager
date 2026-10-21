package com.myproject;

import com.myproject.collection.CustomList;
import com.myproject.io.FileManager;
import com.myproject.model.Student;
import com.myproject.strategy.EvenValueSortStrategy;
import com.myproject.strategy.SortByGpa;
import com.myproject.strategy.SortByGradeBook;
import com.myproject.strategy.SortByGroup;
import com.myproject.strategy.SortStrategy;
import com.myproject.thread.OccurrenceCounter;
import com.myproject.util.StreamGenerator;

import java.io.IOException;
import java.util.Scanner;

/** Меню и бесконечный цикл. Выход — только по пункту "0. Выход". */
public class Main {

    private static final CustomList<Student> STUDENTS = new CustomList<>();
    private static final FileManager FILE_MANAGER = new FileManager();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SortStrategy byGroup = new SortByGroup();
        SortStrategy byGpa = new SortByGpa();
        SortStrategy byGradeBook = new SortByGradeBook();
        SortStrategy byEven = new EvenValueSortStrategy();

        System.out.println("=== МЕНЕДЖЕР СТУДЕНТОВ ===");
        boolean running = true;
        while (running) {
            // Ввод мог оборваться (EOF) прямо перед выбором пункта меню —
            // проверяем до печати меню, чтобы не показывать его зря.
            if (!scanner.hasNextLine()) {
                System.out.println("\nОбнаружен конец ввода (EOF). Завершение работы.");
                System.exit(0);
            }
            printMenu();
            int choice = readInt(scanner, "Введите пункт меню: ");
            switch (choice) {
                case 1:  fillManually(scanner); break;
                case 2:  fillRandom(scanner); break;
                case 3:  loadFromFile(scanner); break;
                case 4:  printCollection(); break;
                case 5:  sortCollection(byGroup, "по номеру группы"); break;
                case 6:  sortCollection(byGpa, "по среднему баллу"); break;
                case 7:  sortCollection(byGradeBook, "по номеру зачётной книжки"); break;
                case 8:  sortCollection(byEven, "по чётным номерам зачётных книжек (доп.)"); break;
                case 9:  saveToFile(scanner); break;
                case 10: countOccurrences(scanner); break;
                case 0:
                    System.out.println("Выход из программы. До свидания!");
                    running = false;
                    break;
                default:
                    System.out.println("Неверный пункт меню.");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("========== МЕНЮ ==========");
        System.out.println("1.  Заполнить коллекцию вручную");
        System.out.println("2.  Заполнить коллекцию случайными данными (Stream API)");
        System.out.println("3.  Загрузить коллекцию из файла");
        System.out.println("4.  Вывести коллекцию в консоль");
        System.out.println("5.  Сортировка по номеру группы");
        System.out.println("6.  Сортировка по среднему баллу");
        System.out.println("7.  Сортировка по номеру зачётной книжки");
        System.out.println("8.  Сортировка по чётным номерам зачёток (доп.)");
        System.out.println("9.  Сохранить коллекцию в файл (дозапись)");
        System.out.println("10. Многопоточный подсчёт вхождений (доп.)");
        System.out.println("0.  Выход");
        System.out.println("==========================");
    }

    private static void fillManually(Scanner scanner) {
        int n = readInt(scanner, "Введите количество студентов: ");
        if (n <= 0) {
            System.out.println("Количество должно быть больше 0.");
            return;
        }
        int added = 0;
        for (int i = 0; i < n; i++) {
            System.out.println("--- Студент #" + (i + 1) + " ---");
            String group = readLine(scanner, "Номер группы: ");
            double gpa = readDouble(scanner, "Средний балл (1.0-5.0): ");
            int gradeBook = readInt(scanner, "Номер зачётной книжки (>0): ");
            try {
                FileManager.validate(group, gpa, gradeBook);
                Student student = new Student.Builder()
                        .groupNumber(group)
                        .averageGpa(gpa)
                        .gradeBookNumber(gradeBook)
                        .build();
                STUDENTS.add(student);
                added++;
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка валидации: " + e.getMessage() + " (запись пропущена)");
            }
        }
        System.out.println("Добавлено: " + added + " из " + n + ". Всего в коллекции: " + STUDENTS.size());
    }

    private static void fillRandom(Scanner scanner) {
        int n = readInt(scanner, "Введите количество студентов для генерации: ");
        if (n <= 0) {
            System.out.println("Количество должно быть больше 0.");
            return;
        }
        CustomList<Student> generated = StreamGenerator.generate(n);
        for (int i = 0; i < generated.size(); i++) {
            STUDENTS.add(generated.get(i));
        }
        System.out.println("Сгенерировано: " + generated.size() + ". Всего в коллекции: " + STUDENTS.size());
    }

    private static void loadFromFile(Scanner scanner) {
        String path = readLine(scanner, "Введите путь к файлу: ");
        try {
            CustomList<Student> loaded = FILE_MANAGER.loadFromFile(path);
            for (int i = 0; i < loaded.size(); i++) {
                STUDENTS.add(loaded.get(i));
            }
            System.out.println("Загружено из файла: " + loaded.size() + ". Всего в коллекции: " + STUDENTS.size());
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка валидации данных: " + e.getMessage());
        }
    }

    private static void printCollection() {
        if (STUDENTS.size() == 0) {
            System.out.println("Коллекция пуста.");
            return;
        }
        System.out.println("Коллекция (размер: " + STUDENTS.size() + "):");
        for (int i = 0; i < STUDENTS.size(); i++) {
            System.out.println("  [" + i + "] " + STUDENTS.get(i));
        }
    }

    private static void sortCollection(SortStrategy strategy, String description) {
        if (STUDENTS.size() == 0) {
            System.out.println("Коллекция пуста. Нечего сортировать.");
            return;
        }
        strategy.sort(STUDENTS);
        System.out.println("Выполнена сортировка " + description + ":");
        for (int i = 0; i < STUDENTS.size(); i++) {
            System.out.println("  [" + i + "] " + STUDENTS.get(i));
        }
    }

    private static void saveToFile(Scanner scanner) {
        if (STUDENTS.size() == 0) {
            System.out.println("Коллекция пуста. Нечего сохранять.");
            return;
        }
        String path = readLine(scanner, "Введите путь к файлу для сохранения: ");
        try {
            FILE_MANAGER.appendToFile(path, STUDENTS);
            System.out.println("Сохранено в файл (режим дозаписи): " + path);
        } catch (IOException e) {
            System.out.println("Ошибка записи в файл: " + e.getMessage());
        }
    }

    private static void countOccurrences(Scanner scanner) {
        if (STUDENTS.size() == 0) {
            System.out.println("Коллекция пуста.");
            return;
        }
        int target = readInt(scanner, "Введите номер зачётной книжки (N) для подсчёта: ");
        try {
            OccurrenceCounter.countAndPrint(STUDENTS, target);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Подсчёт был прерван.");
        }
    }

    private static int readInt(Scanner scanner, String prompt) {
        System.out.print(prompt);
        while (true) {
            // Проверка стоит первой в теле цикла, поэтому срабатывает и при
            // первом входе в метод, и после повторного ввода при ошибке формата.
            if (!scanner.hasNextLine()) {
                System.out.println("\nОбнаружен конец ввода (EOF). Завершение работы.");
                System.exit(0);
            }
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("Введите целое число. " + prompt);
            }
        }
    }

    private static double readDouble(Scanner scanner, String prompt) {
        System.out.print(prompt);
        while (true) {
            if (!scanner.hasNextLine()) {
                System.out.println("\nОбнаружен конец ввода (EOF). Завершение работы.");
                System.exit(0);
            }
            String line = scanner.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.print("Неверный формат числа. " + prompt);
            }
        }
    }

    private static String readLine(Scanner scanner, String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println("\nОбнаружен конец ввода (EOF). Завершение работы.");
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }
}
