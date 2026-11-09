package com.myproject.repository;

import com.myproject.collection.CustomList;
import com.myproject.model.Student;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/**
 * JDBC-репозиторий SQLite для студентов.
 */
@Repository
public class StudentDatabaseRepository implements AutoCloseable {

    private static final String IN_MEMORY_URL = "jdbc:sqlite::memory:";
    private final Connection connection;

    public StudentDatabaseRepository(@Value("${app.datasource.url:jdbc:sqlite:students.db}") String jdbcUrl) {
        Objects.requireNonNull(jdbcUrl, "jdbcUrl");
        try {
            this.connection = DriverManager.getConnection(jdbcUrl);
            initSchema();
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось открыть базу данных: " + jdbcUrl, e);
        }
    }

    public static StudentDatabaseRepository inMemory() {
        return new StudentDatabaseRepository(IN_MEMORY_URL);
    }

    public synchronized void save(Student student) {
        Objects.requireNonNull(student, "student");
        String sql = "INSERT INTO students(group_number, average_gpa, grade_book_number) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, student.groupNumber());
            statement.setDouble(2, student.averageGpa());
            statement.setInt(3, student.gradeBookNumber());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось сохранить студента", e);
        }
    }

    public synchronized CustomList<Student> loadAll() {
        CustomList<Student> students = new CustomList<>();
        String sql = "SELECT group_number, average_gpa, grade_book_number FROM students ORDER BY id";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                students.add(new Student(
                        resultSet.getString("group_number"),
                        resultSet.getDouble("average_gpa"),
                        resultSet.getInt("grade_book_number")
                ));
            }
            return students;
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось загрузить студентов", e);
        }
    }

    public synchronized int deleteByGradeBookNumber(int gradeBookNumber) {
        String sql = "DELETE FROM students WHERE grade_book_number = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, gradeBookNumber);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось удалить студента по зачётной книжке: " + gradeBookNumber, e);
        }
    }

    public synchronized void deleteAll() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM students");
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось очистить таблицу студентов", e);
        }
    }

    public synchronized int count() {
        String sql = "SELECT COUNT(*) FROM students";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось посчитать студентов", e);
        }
    }

    @Override
    public synchronized void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось закрыть соединение с базой данных", e);
        }
    }

    private void initSchema() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS students (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    group_number TEXT NOT NULL,
                    average_gpa REAL NOT NULL,
                    grade_book_number INTEGER NOT NULL
                )
                """;
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
}
