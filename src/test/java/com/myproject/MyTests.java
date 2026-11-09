package com.myproject;

import com.myproject.collection.CustomList;
import com.myproject.model.Student;
import com.myproject.repository.RepositoryException;
import com.myproject.repository.StudentDatabaseRepository;
import com.myproject.service.StudentService;
import com.myproject.strategy.EvenValueSortStrategy;
import com.myproject.strategy.SortByGpa;
import com.myproject.strategy.SortByGradeBook;
import com.myproject.strategy.SortByGroup;
import com.myproject.thread.OccurrenceCounter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Student manager")
class MyTests {

    @Nested
    @DisplayName("Модель Student (record)")
    class Model {

        @Test
        void constructorIsTheValidationSource() {
            assertThrows(IllegalArgumentException.class, () -> new Student("", 4.0, 10));
            assertThrows(IllegalArgumentException.class, () -> new Student(null, 4.0, 10));
            assertThrows(IllegalArgumentException.class, () -> new Student("   ", 4.0, 10));
            assertThrows(IllegalArgumentException.class, () -> new Student("ИВТ", 0.5, 10));
            assertThrows(IllegalArgumentException.class, () -> new Student("ИВТ", 5.5, 10));
            assertThrows(IllegalArgumentException.class, () -> new Student("ИВТ", 4.0, 0));
            assertThrows(IllegalArgumentException.class, () -> new Student("ИВТ", 4.0, -5));
            assertThrows(IllegalArgumentException.class, () -> new Student("ИВТ", Double.NaN, 10));
        }

        @Test
        void boundariesAreValid() {
            assertDoesNotThrow(() -> new Student("ИВТ", 1.0, 1));
            assertDoesNotThrow(() -> new Student("ИВТ", 5.0, 10));
        }

        @Test
        void accessorsAndToString() {
            Student s = new Student("  ИВТ-501  ", 4.5, 123);
            assertEquals("ИВТ-501", s.groupNumber(), "группа должна обрезаться");
            assertEquals(4.5, s.averageGpa());
            assertEquals(123, s.gradeBookNumber());
            assertTrue(s.toString().contains("ИВТ-501"));
            assertEquals(s, new Student("ИВТ-501", 4.5, 123), "record equals по компонентам");
        }
    }

    @Nested
    @DisplayName("CustomList")
    class Collection {

        @Test
        void basicsAndBounds() {
            CustomList<String> list = new CustomList<>();
            assertEquals(0, list.size());
            list.add("a");
            list.add("b");
            list.add("c");
            assertEquals(3, list.size());
            assertEquals("a", list.get(0));
            assertEquals("c", list.get(2));
            list.set(1, "B");
            assertEquals("B", list.get(1));
            assertThrows(IndexOutOfBoundsException.class, () -> list.get(5));
        }

        @Test
        void removeShiftsTailAndNullsLastSlot() {
            CustomList<String> list = new CustomList<>();
            list.add("a");
            list.add("b");
            list.add("c");
            assertEquals("b", list.removeAt(1));
            assertEquals(2, list.size());
            assertEquals("a", list.get(0));
            assertEquals("c", list.get(1));
            assertThrows(IndexOutOfBoundsException.class, () -> list.removeAt(5));
        }

        @Test
        void growthBeyondInitialCapacity() {
            CustomList<Integer> nums = new CustomList<>();
            for (int i = 0; i < 100; i++) {
                nums.add(i);
            }
            assertEquals(100, nums.size());
            assertEquals(99, nums.get(99));
        }

        @Test
        void iterableForEach() {
            CustomList<Integer> nums = new CustomList<>();
            for (int i = 0; i < 100; i++) {
                nums.add(i);
            }
            int sum = 0;
            for (Integer value : nums) {
                sum += value;
            }
            assertEquals(4950, sum);
        }
    }

    @Nested
    @DisplayName("Стратегии сортировки")
    class Sorting {

        @Test
        void sortByGroup() {
            CustomList<Student> list = sample();
            new SortByGroup().sort(list);
            assertEquals("ИВТ-501", list.get(0).groupNumber());
            assertEquals("ИВТ-501", list.get(1).groupNumber());
            assertEquals("ИВТ-502", list.get(2).groupNumber());
            assertEquals("ПИ-301", list.get(3).groupNumber());
        }

        @Test
        void sortByGpa() {
            CustomList<Student> list = sample();
            new SortByGpa().sort(list);
            assertEquals(2.5, list.get(0).averageGpa());
            assertEquals(4.8, list.get(3).averageGpa());
        }

        @Test
        void sortByGradeBook() {
            CustomList<Student> list = sample();
            new SortByGradeBook().sort(list);
            assertEquals(10, list.get(0).gradeBookNumber());
            assertEquals(40, list.get(3).gradeBookNumber());
        }

        @Test
        void evenValueSortKeepsOddInPlace() {
            CustomList<Student> list = evenSample();
            new EvenValueSortStrategy().sort(list);
            assertEquals(5, list.get(0).gradeBookNumber());
            assertEquals(2, list.get(1).gradeBookNumber());
            assertEquals(3, list.get(2).gradeBookNumber());
            assertEquals(6, list.get(3).gradeBookNumber());
            assertEquals(7, list.get(4).gradeBookNumber());
            assertEquals(8, list.get(5).gradeBookNumber());
        }
    }

    @Nested
    @DisplayName("SQLite JDBC Repository")
    class Database {

        @Test
        void schemaSaveLoadDelete() {
            try (StudentDatabaseRepository repository = StudentDatabaseRepository.inMemory()) {
                assertEquals(0, repository.count());

                repository.save(stu("ИВТ-501", 4.5, 123));
                repository.save(stu("ПИ-301", 3.0, 7));

                CustomList<Student> loaded = repository.loadAll();
                assertEquals(2, loaded.size());
                assertEquals("ИВТ-501", loaded.get(0).groupNumber());
                assertEquals(4.5, loaded.get(0).averageGpa());
                assertEquals(123, loaded.get(0).gradeBookNumber());

                assertEquals(1, repository.deleteByGradeBookNumber(7));
                assertEquals(1, repository.count());
                assertEquals("ИВТ-501", repository.loadAll().get(0).groupNumber());
            }
        }

        @Test
        void invalidUrlThrowsRepositoryException() {
            assertThrows(RepositoryException.class, () -> new StudentDatabaseRepository("jdbc:nonexistent:none"));
        }
    }

    @Nested
    @DisplayName("StudentService на SQLite")
    class Service {

        @Test
        void nullRepositoryIsRejected() {
            assertThrows(NullPointerException.class, () -> new StudentService(null));
        }

        @Test
        void addLoadAndDeleteThroughRepository() {
            try (StudentDatabaseRepository repository = StudentDatabaseRepository.inMemory()) {
                StudentService service = new StudentService(repository);

                service.addStudent("  ИВТ-501  ", 4.5, 123);
                service.addStudent("ПИ-301", 3.0, 7);
                assertEquals(2, service.size());

                List<Student> snapshot = service.snapshot();
                assertEquals("ИВТ-501", snapshot.get(0).groupNumber());

                assertEquals(1, service.deleteByGradeBookNumber(7));
                assertEquals(1, service.size());
                assertEquals("ИВТ-501", service.snapshot().get(0).groupNumber());
            }
        }

        @Test
        void deleteByGradeBookRemovesAllMatches() {
            try (StudentDatabaseRepository repository = StudentDatabaseRepository.inMemory()) {
                StudentService service = new StudentService(repository);
                service.addStudent("ГР-1", 3.0, 7);
                service.addStudent("ГР-2", 4.0, 7);

                assertEquals(2, service.deleteByGradeBookNumber(7));
                assertEquals(0, service.size());
            }
        }

        @Test
        void invalidStudentIsRejectedBeforeSave() {
            try (StudentDatabaseRepository repository = StudentDatabaseRepository.inMemory()) {
                StudentService service = new StudentService(repository);
                assertThrows(IllegalArgumentException.class, () -> service.addStudent("", 4.0, 10));
                assertEquals(0, service.size());
            }
        }

        @Test
        void sortedSnapshotThroughRepository() {
            try (StudentDatabaseRepository repository = StudentDatabaseRepository.inMemory()) {
                StudentService service = new StudentService(repository);
                service.addStudents(sample());

                assertEquals(2.5, service.sortedSnapshot("gpa").get(0).averageGpa());
                assertEquals("ИВТ-501", service.sortedSnapshot("group").get(0).groupNumber());
            }
        }

        @Test
        void evenSortedSnapshotThroughRepository() {
            try (StudentDatabaseRepository repository = StudentDatabaseRepository.inMemory()) {
                StudentService service = new StudentService(repository);
                service.addStudents(evenSample());

                List<Student> sorted = service.sortedSnapshot("even");
                assertEquals(5, sorted.get(0).gradeBookNumber());
                assertEquals(2, sorted.get(1).gradeBookNumber());
                assertEquals(3, sorted.get(2).gradeBookNumber());
                assertEquals(6, sorted.get(3).gradeBookNumber());
                assertEquals(7, sorted.get(4).gradeBookNumber());
                assertEquals(8, sorted.get(5).gradeBookNumber());
            }
        }

        @Test
        void generateRejectsNonPositiveCount() {
            try (StudentDatabaseRepository repository = StudentDatabaseRepository.inMemory()) {
                StudentService service = new StudentService(repository);
                assertThrows(IllegalArgumentException.class, () -> service.generateRandom(0));
            }
        }

        @Test
        void countOccurrencesThroughService() throws InterruptedException {
            try (StudentDatabaseRepository repository = StudentDatabaseRepository.inMemory()) {
                StudentService service = new StudentService(repository);
                service.addStudents(occurrenceSample());
                assertEquals(10, service.countOccurrences(5));
            }
        }
    }

    @Nested
    @DisplayName("Многопоточный подсчёт")
    class Threading {

        @Test
        void countOccurrencesAcrossThreads() throws InterruptedException {
            CustomList<Student> list = occurrenceSample();
            assertEquals(10, OccurrenceCounter.countOccurrences(list, 5));
        }
    }

    private static Student stu(String groupNumber, double averageGpa, int gradeBookNumber) {
        return new Student(groupNumber, averageGpa, gradeBookNumber);
    }

    private static CustomList<Student> sample() {
        CustomList<Student> list = new CustomList<>();
        list.add(stu("ИВТ-502", 4.0, 20));
        list.add(stu("ИВТ-501", 3.5, 30));
        list.add(stu("ПИ-301", 4.8, 10));
        list.add(stu("ИВТ-501", 2.5, 40));
        return list;
    }

    private static CustomList<Student> evenSample() {
        CustomList<Student> list = new CustomList<>();
        list.add(stu("A", 3.0, 5));
        list.add(stu("A", 3.0, 8));
        list.add(stu("A", 3.0, 3));
        list.add(stu("A", 3.0, 2));
        list.add(stu("A", 3.0, 7));
        list.add(stu("A", 3.0, 6));
        return list;
    }

    private static CustomList<Student> occurrenceSample() {
        CustomList<Student> list = new CustomList<>();
        for (int i = 0; i < 100; i++) {
            list.add(stu("ГР-" + (i % 5 + 1), 3.0, i % 10 + 1));
        }
        return list;
    }
}
