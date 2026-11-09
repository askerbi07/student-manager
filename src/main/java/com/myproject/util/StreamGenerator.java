package com.myproject.util;

import com.myproject.collection.CustomList;
import com.myproject.model.Student;

import java.util.Random;
import java.util.stream.Stream;

/**
 * Генерация случайных студентов через Stream API.
 * Объект создаётся напрямую конструктором record Student.
 */
public class StreamGenerator {

    private static final Random RANDOM = new Random();

    public static CustomList<Student> generate(int count) {
        CustomList<Student> result = new CustomList<>();
        Stream.generate(StreamGenerator::randomStudent)
              .limit(count)
              .forEach(result::add);
        return result;
    }

    private static Student randomStudent() {
        String group = "ГР-" + (RANDOM.nextInt(5) + 1);
        double gpa = 1.0 + Math.round(RANDOM.nextDouble() * 40) / 10.0;
        int gradeBook = RANDOM.nextInt(999) + 1;
        return new Student(group, gpa, gradeBook);
    }
}
