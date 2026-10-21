package com.myproject.io;

import com.myproject.collection.CustomList;
import com.myproject.model.Student;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Чтение/запись файлов + валидация.
 * Формат строки в файле: группа;балл;зачётка   (например: ИВТ-501;4.5;123)
 */
public class FileManager {

    public CustomList<Student> loadFromFile(String path) throws IOException {
        CustomList<Student> result = new CustomList<>();
        int lineNumber = 0;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                // Одна битая строка не должна ронять всю загрузку: сообщаем
                // о пропуске в stderr и продолжаем читать файл дальше.
                try {
                    Student student = parseLine(line);
                    result.add(student);
                } catch (IllegalArgumentException e) {
                    System.err.println("Предупреждение (строка " + lineNumber + "): "
                            + e.getMessage() + " -> строка пропущена");
                }
            }
        }
        return result;
    }

    private Student parseLine(String line) {
        // limit = -1: сохраняем пустые поля в конце, чтобы "ИВТ;4.5;"
        // не превращалось в 2 элемента, а корректно резалось на 3 части.
        String[] parts = line.split(";", -1);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Неверный формат строки: " + line);
        }
        String group = parts[0].trim();
        double gpa;
        int gradeBook;
        try {
            gpa = Double.parseDouble(parts[1].trim());
            gradeBook = Integer.parseInt(parts[2].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректные числа в строке: " + line);
        }
        validate(group, gpa, gradeBook);
        return new Student.Builder()
                .groupNumber(group)
                .averageGpa(gpa)
                .gradeBookNumber(gradeBook)
                .build();
    }

    // Валидация по ТЗ: балл 1.0..5.0, зачётка > 0, группа не пустая.
    public static void validate(String group, double gpa, int gradeBook) {
        if (group == null || group.trim().isEmpty()) {
            throw new IllegalArgumentException("Номер группы не может быть пустым.");
        }
        // ';' — разделитель формата файла: попав в группу, он превращает одну
        // запись в четыре поля, и сохранённые данные уже не прочитать обратно.
        if (group.contains(";")) {
            throw new IllegalArgumentException("Номер группы не должен содержать символ ';'.");
        }
        if (group.indexOf('\n') >= 0 || group.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("Номер группы не должен содержать перевод строки.");
        }
        // isFinite отсекает NaN и ±Infinity: для NaN сравнения ниже дают false,
        // и без этой проверки невалидный балл прошёл бы в файл.
        if (!Double.isFinite(gpa) || gpa < 1.0 || gpa > 5.0) {
            throw new IllegalArgumentException("Средний балл должен быть от 1.0 до 5.0.");
        }
        if (gradeBook <= 0) {
            throw new IllegalArgumentException("Номер зачётной книжки должен быть больше 0.");
        }
    }

    // Доп. задание 2: дозапись (Append) через Files.write + StandardOpenOption.APPEND.
    public void appendToFile(String path, CustomList<Student> list) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            Student s = list.get(i);
            sb.append(s.getGroupNumber()).append(';')
              .append(s.getAverageGpa()).append(';')
              .append(s.getGradeBookNumber())
              .append(System.lineSeparator());
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        Files.write(Paths.get(path), bytes,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }
}
