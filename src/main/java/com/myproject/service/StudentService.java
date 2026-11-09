package com.myproject.service;

import com.myproject.collection.CustomList;
import com.myproject.model.Student;
import com.myproject.repository.StudentDatabaseRepository;
import com.myproject.strategy.SortStrategyFactory;
import com.myproject.thread.OccurrenceCounter;
import com.myproject.util.StreamGenerator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Сервисный слой поверх JDBC-репозитория.
 */
@Service
public class StudentService {

    private final StudentDatabaseRepository repository;

    public StudentService(StudentDatabaseRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public int size() {
        return repository.count();
    }

    public CustomList<Student> loadAll() {
        return repository.loadAll();
    }

    public List<Student> snapshot() {
        return toList(loadAll());
    }

    public List<Student> sortedSnapshot(String sortKey) {
        CustomList<Student> students = loadAll();
        SortStrategyFactory.getStrategy(sortKey).sort(students);
        return toList(students);
    }

    public Student addStudent(String groupNumber, double averageGpa, int gradeBookNumber) {
        Student student = new Student(groupNumber, averageGpa, gradeBookNumber);
        repository.save(student);
        return student;
    }

    public int addStudents(Iterable<Student> source) {
        Objects.requireNonNull(source, "source");
        int added = 0;
        for (Student student : source) {
            repository.save(student);
            added++;
        }
        return added;
    }

    public int generateRandom(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count <= 0: " + count);
        }
        return addStudents(StreamGenerator.generate(count));
    }

    public int deleteByGradeBookNumber(int gradeBookNumber) {
        return repository.deleteByGradeBookNumber(gradeBookNumber);
    }

    public int countOccurrences(int targetGradeBook) throws InterruptedException {
        return OccurrenceCounter.countOccurrences(loadAll(), targetGradeBook);
    }

    private static List<Student> toList(CustomList<Student> source) {
        List<Student> target = new ArrayList<>(source.size());
        for (Student student : source) {
            target.add(student);
        }
        return target;
    }
}
