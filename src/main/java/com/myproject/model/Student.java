package com.myproject.model;

/**
 * Студент как неизменяемая запись (Java 17 record).
 * Единственный источник доменной валидации — компактный конструктор.
 */
public record Student(
        String groupNumber,
        double averageGpa,
        int gradeBookNumber
) {

    public Student {
        if (groupNumber == null || groupNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Номер группы не может быть пустым");
        }
        groupNumber = groupNumber.trim();

        if (!Double.isFinite(averageGpa)) {
            throw new IllegalArgumentException("Средний балл должен быть конечным числом (не NaN или Infinity)");
        }
        if (averageGpa < 1.0 || averageGpa > 5.0) {
            throw new IllegalArgumentException("Средний балл должен быть от 1.0 до 5.0, получено: " + averageGpa);
        }

        if (gradeBookNumber <= 0) {
            throw new IllegalArgumentException("Номер зачётной книжки должен быть больше 0, получено: " + gradeBookNumber);
        }
    }

    @Override
    public String toString() {
        return "Student{группа='" + groupNumber + "', балл=" + averageGpa
                + ", зачётка=" + gradeBookNumber + "}";
    }
}
