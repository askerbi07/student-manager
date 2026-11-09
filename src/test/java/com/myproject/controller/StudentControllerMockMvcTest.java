package com.myproject.controller;

import com.myproject.repository.StudentDatabaseRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = com.myproject.StudentManagerApplication.class,
        properties = "app.datasource.url=jdbc:sqlite::memory:"
)
@AutoConfigureMockMvc
class StudentControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentDatabaseRepository repository;

    @AfterEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void getStudentsReturnsEmptyJsonArray() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void postAddsStudent() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"groupNumber":"GroupA","averageGpa":4.5,"gradeBookNumber":123}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.groupNumber").value("GroupA"))
                .andExpect(jsonPath("$.averageGpa").value(4.5))
                .andExpect(jsonPath("$.gradeBookNumber").value(123));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].groupNumber").value("GroupA"));
    }

    @Test
    void postInvalidGroupNumberReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"groupNumber":"","averageGpa":3.0,"gradeBookNumber":5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void postInvalidAverageGpaReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"groupNumber":"GroupA","averageGpa":10.0,"gradeBookNumber":5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deleteExistingStudentReturnsNoContent() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"groupNumber":"GroupB","averageGpa":4.0,"gradeBookNumber":777}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/students/{gradeBookNumber}", 777))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deleteMissingStudentReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/students/{gradeBookNumber}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void openApiDocsAreAvailable() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info").exists());
    }
}
