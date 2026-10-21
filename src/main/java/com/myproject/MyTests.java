package com.myproject;

import com.myproject.collection.CustomList;
import com.myproject.io.FileManager;
import com.myproject.model.Student;
import com.myproject.strategy.EvenValueSortStrategy;
import com.myproject.strategy.SortByGpa;
import com.myproject.strategy.SortByGradeBook;
import com.myproject.strategy.SortByGroup;
import com.myproject.thread.OccurrenceCounter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Тесты на assert (JUnit запрещён по ТЗ).
 * ВАЖНО: запускать с флагом -ea, иначе assert молча пропускается.
 */
public class MyTests {

    public static void main(String[] args) throws Exception {
        boolean assertionsOn = false;
        assert assertionsOn = true;
        if (!assertionsOn) {
            System.err.println("!!! АССЕРТЫ ОТКЛЮЧЕНЫ. Запустите: java -ea com.myproject.MyTests !!!");
        }

        testBuilder();
        testValidation();
        testCustomList();
        testSortByGroup();
        testSortByGpa();
        testSortByGradeBook();
        testEvenValueSort();
        testFileManager();
        testMultithreading();

        System.out.println("\nВСЕ ТЕСТЫ ПРОЙДЕНЫ УСПЕШНО");
    }

    private static void testBuilder() {
        Student s = new Student.Builder()
                .groupNumber("ИВТ-501")
                .averageGpa(4.5)
                .gradeBookNumber(123)
                .build();
        assert "ИВТ-501".equals(s.getGroupNumber()) : "Builder: неверная группа";
        assert s.getAverageGpa() == 4.5 : "Builder: неверный балл";
        assert s.getGradeBookNumber() == 123 : "Builder: неверная зачётка";
        System.out.println("testBuilder: OK");
    }

    private static void testValidation() {
        FileManager.validate("ИВТ", 4.0, 10);
        assertThrows(() -> FileManager.validate("", 4.0, 10));
        assertThrows(() -> FileManager.validate(null, 4.0, 10));
        assertThrows(() -> FileManager.validate("ИВТ", 0.5, 10));
        assertThrows(() -> FileManager.validate("ИВТ", 5.5, 10));
        assertThrows(() -> FileManager.validate("ИВТ", 4.0, 0));
        assertThrows(() -> FileManager.validate("ИВТ", 4.0, -5));
        assertThrows(() -> FileManager.validate("ИВТ;501", 4.0, 10));
        assertThrows(() -> FileManager.validate("ИВТ", Double.NaN, 10));
        FileManager.validate("ИВТ", 1.0, 1);   // нижние границы диапазонов допустимы
        FileManager.validate("ИВТ", 5.0, 10);  // верхняя граница балла допустима
        System.out.println("testValidation: OK");
    }

    private static void testFileManager() throws IOException {
        Path file = Files.createTempFile("students-test", ".txt");
        try {
            FileManager fm = new FileManager();
            CustomList<Student> toWrite = new CustomList<>();
            toWrite.add(stu("ИВТ-501", 4.5, 123));
            fm.appendToFile(file.toString(), toWrite);

            // Дописываем битую строку и ещё одну валидную: битая строка должна
            // быть пропущена (с предупреждением в stderr), а не оборвать загрузку.
            String brokenLine = "БИТАЯ СТРОКА БЕЗ РАЗДЕЛИТЕЛЕЙ" + System.lineSeparator();
            String validLine = "ПИ-301;3.0;7" + System.lineSeparator();
            Files.write(file, (brokenLine + validLine).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.APPEND);

            CustomList<Student> loaded = fm.loadFromFile(file.toString());
            assert loaded.size() == 2 : "FileManager: ожидалось 2 записи (битая строка пропущена), получено " + loaded.size();

            Student first = loaded.get(0);
            assert "ИВТ-501".equals(first.getGroupNumber()) : "FileManager: неверная группа после round-trip";
            assert first.getAverageGpa() == 4.5 : "FileManager: неверный балл после round-trip";
            assert first.getGradeBookNumber() == 123 : "FileManager: неверная зачётка после round-trip";

            Student second = loaded.get(1);
            assert "ПИ-301".equals(second.getGroupNumber()) : "FileManager: строка после битой не загружена";
            assert second.getAverageGpa() == 3.0 : "FileManager: неверный балл в строке после битой";
            assert second.getGradeBookNumber() == 7 : "FileManager: неверная зачётка в строке после битой";
        } finally {
            Files.deleteIfExists(file);
        }
        System.out.println("testFileManager: OK");
    }

    private static void testMultithreading() throws InterruptedException {
        CustomList<Student> list = new CustomList<>();
        for (int i = 0; i < 100; i++) {
            // Номер зачётки повторяется по кругу 1..10, т.е. каждое значение
            // встречается ровно 10 раз. 0 не используем: по ТЗ зачётка > 0.
            list.add(stu("ГР-" + (i % 5 + 1), 3.0, i % 10 + 1));
        }
        int result = OccurrenceCounter.countOccurrences(list, 5);
        assert result == 10 : "Многопоточный подсчёт: ожидалось 10 вхождений, получено " + result;
        System.out.println("testMultithreading: OK");
    }

    private interface ThrowingRunnable {
        void run();
    }

    private static void assertThrows(ThrowingRunnable r) {
        try {
            r.run();
        } catch (IllegalArgumentException e) {
            return;
        }
        throw new AssertionError("Не выброшено IllegalArgumentException");
    }

    private static void testCustomList() {
        CustomList<String> list = new CustomList<>();
        assert list.size() == 0 : "Новый список должен быть пустым";
        list.add("a");
        list.add("b");
        list.add("c");
        assert list.size() == 3 : "Размер должен быть 3";
        assert "a".equals(list.get(0)) : "get(0) неверный";
        assert "c".equals(list.get(2)) : "get(2) неверный";
        list.set(1, "B");
        assert "B".equals(list.get(1)) : "set(1) не сработал";

        boolean thrown = false;
        try {
            list.get(5);
        } catch (IndexOutOfBoundsException e) {
            thrown = true;
        }
        assert thrown : "get(5) должен выбросить IndexOutOfBoundsException";

        CustomList<Integer> nums = new CustomList<>();
        for (int i = 0; i < 100; i++) {
            nums.add(i);
        }
        assert nums.size() == 100 : "Размер должен быть 100";
        assert nums.get(99) == 99 : "get(99) неверный";
        System.out.println("testCustomList: OK");
    }

    private static Student stu(String g, double a, int b) {
        return new Student.Builder().groupNumber(g).averageGpa(a).gradeBookNumber(b).build();
    }

    private static CustomList<Student> sample() {
        CustomList<Student> list = new CustomList<>();
        list.add(stu("ИВТ-502", 4.0, 20));
        list.add(stu("ИВТ-501", 3.5, 30));
        list.add(stu("ПИ-301", 4.8, 10));
        list.add(stu("ИВТ-501", 2.5, 40));
        return list;
    }

    private static void testSortByGroup() {
        CustomList<Student> list = sample();
        new SortByGroup().sort(list);
        assert "ИВТ-501".equals(list.get(0).getGroupNumber()) : "Сорт. по группе: [0] неверна";
        assert "ИВТ-501".equals(list.get(1).getGroupNumber()) : "Сорт. по группе: [1] неверна";
        assert "ИВТ-502".equals(list.get(2).getGroupNumber()) : "Сорт. по группе: [2] неверна";
        assert "ПИ-301".equals(list.get(3).getGroupNumber()) : "Сорт. по группе: [3] неверна";
        System.out.println("testSortByGroup: OK");
    }

    private static void testSortByGpa() {
        CustomList<Student> list = sample();
        new SortByGpa().sort(list);
        assert list.get(0).getAverageGpa() == 2.5 : "Сорт. по баллу: [0] неверен";
        assert list.get(3).getAverageGpa() == 4.8 : "Сорт. по баллу: [3] неверен";
        System.out.println("testSortByGpa: OK");
    }

    private static void testSortByGradeBook() {
        CustomList<Student> list = sample();
        new SortByGradeBook().sort(list);
        assert list.get(0).getGradeBookNumber() == 10 : "Сорт. по зачётке: [0] неверна";
        assert list.get(3).getGradeBookNumber() == 40 : "Сорт. по зачётке: [3] неверна";
        System.out.println("testSortByGradeBook: OK");
    }

    private static void testEvenValueSort() {
        CustomList<Student> list = new CustomList<>();
        list.add(stu("A", 3.0, 5));
        list.add(stu("A", 3.0, 8));
        list.add(stu("A", 3.0, 3));
        list.add(stu("A", 3.0, 2));
        list.add(stu("A", 3.0, 7));
        list.add(stu("A", 3.0, 6));
        new EvenValueSortStrategy().sort(list);
        assert list.get(0).getGradeBookNumber() == 5 : "Even sort: [0] неверен";
        assert list.get(1).getGradeBookNumber() == 2 : "Even sort: [1] неверен";
        assert list.get(2).getGradeBookNumber() == 3 : "Even sort: [2] неверен";
        assert list.get(3).getGradeBookNumber() == 6 : "Even sort: [3] неверен";
        assert list.get(4).getGradeBookNumber() == 7 : "Even sort: [4] неверен";
        assert list.get(5).getGradeBookNumber() == 8 : "Even sort: [5] неверен";
        System.out.println("testEvenValueSort: OK");
    }
}
