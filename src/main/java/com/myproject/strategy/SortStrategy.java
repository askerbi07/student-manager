package com.myproject.strategy;

import com.myproject.collection.CustomList;
import com.myproject.model.Student;

/**
 * Паттерн "Стратегия". Каждая реализация — свой алгоритм сортировки
 * (все написаны вручную "пузырьком", без Collections.sort/Arrays.sort/Comparator).
 */
public interface SortStrategy {
    void sort(CustomList<Student> list);
}
