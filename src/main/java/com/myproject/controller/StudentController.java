package com.myproject.controller;

import com.myproject.model.Student;
import com.myproject.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Students", description = "Операции со студентами")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @Operation(summary = "Получить всех студентов")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Список студентов"))
    @GetMapping
    public List<Student> getAllStudents() {
        return service.snapshot();
    }

    @Operation(summary = "Создать студента")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Студент создан"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные")
    })
    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody StudentRequest request) {
        Student created = service.addStudent(
                request.groupNumber(),
                request.averageGpa(),
                request.gradeBookNumber()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Удалить студента по номеру зачётной книжки")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Студент удалён"),
            @ApiResponse(responseCode = "404", description = "Студент не найден")
    })
    @DeleteMapping("/{gradeBookNumber}")
    public ResponseEntity<Void> deleteStudent(
            @Parameter(description = "Номер зачётной книжки")
            @PathVariable int gradeBookNumber
    ) {
        int deleted = service.deleteByGradeBookNumber(gradeBookNumber);
        if (deleted == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
