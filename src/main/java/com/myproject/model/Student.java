package com.myproject.model;

/**
 * Модель "Студент" + ручной паттерн Builder (без Lombok).
 */
public class Student {

    private final String groupNumber;
    private final double averageGpa;
    private final int gradeBookNumber;

    private Student(Builder builder) {
        this.groupNumber = builder.groupNumber;
        this.averageGpa = builder.averageGpa;
        this.gradeBookNumber = builder.gradeBookNumber;
    }

    public String getGroupNumber() {
        return groupNumber;
    }

    public double getAverageGpa() {
        return averageGpa;
    }

    public int getGradeBookNumber() {
        return gradeBookNumber;
    }

    @Override
    public String toString() {
        return "Student{группа='" + groupNumber + "', балл=" + averageGpa
                + ", зачётка=" + gradeBookNumber + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student)) return false;
        Student s = (Student) o;
        return gradeBookNumber == s.gradeBookNumber
                && Double.compare(s.averageGpa, averageGpa) == 0
                && groupNumber.equals(s.groupNumber);
    }

    @Override
    public int hashCode() {
        int result = groupNumber.hashCode();
        result = 31 * result + Double.hashCode(averageGpa);
        result = 31 * result + Integer.hashCode(gradeBookNumber);
        return result;
    }

    public static class Builder {
        private String groupNumber;
        private double averageGpa;
        private int gradeBookNumber;

        public Builder groupNumber(String groupNumber) {
            this.groupNumber = groupNumber;
            return this;
        }

        public Builder averageGpa(double averageGpa) {
            this.averageGpa = averageGpa;
            return this;
        }

        public Builder gradeBookNumber(int gradeBookNumber) {
            this.gradeBookNumber = gradeBookNumber;
            return this;
        }

        public Student build() {
            return new Student(this);
        }
    }
}
