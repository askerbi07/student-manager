package com.myproject.controller;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на создание студента")
public record StudentRequest(
        @Schema(description = "Номер группы", example = "GroupA")
        String groupNumber,
        @Schema(description = "Средний балл от 1.0 до 5.0", example = "4.5")
        double averageGpa,
        @Schema(description = "Номер зачётной книжки", example = "123")
        int gradeBookNumber
) {
}
