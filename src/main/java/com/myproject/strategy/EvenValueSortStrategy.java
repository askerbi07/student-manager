package com.myproject.strategy;

import com.myproject.collection.CustomList;
import com.myproject.model.Student;

/**
 * Доп. задание 1.
 * Сортирует ТОЛЬКО объекты с ЧЁТНЫМ номером зачётки: они упорядочиваются
 * между собой по возрастанию и встают на те позиции, где стояли чётные.
 * Объекты с НЕЧЁТНЫМ номером остаются на своих исходных местах.
 */
public class EvenValueSortStrategy implements SortStrategy {

    @Override
    public void sort(CustomList<Student> list) {
        int size = list.size();

        // 1) Собираем чётные объекты и их позиции.
        Student[] evenObjects = new Student[size];
        int[] evenPositions = new int[size];
        int evenCount = 0;
        for (int i = 0; i < size; i++) {
            if (list.get(i).gradeBookNumber() % 2 == 0) {
                evenObjects[evenCount] = list.get(i);
                evenPositions[evenCount] = i;
                evenCount++;
            }
        }

        // 2) Сортируем чётные объекты "пузырьком" (вручную).
        for (int i = 0; i < evenCount - 1; i++) {
            for (int j = 0; j < evenCount - 1 - i; j++) {
                if (evenObjects[j].gradeBookNumber() > evenObjects[j + 1].gradeBookNumber()) {
                    Student tmp = evenObjects[j];
                    evenObjects[j] = evenObjects[j + 1];
                    evenObjects[j + 1] = tmp;
                }
            }
        }

        // 3) Возвращаем отсортированные чётные на их позиции. Нечётные не трогаем.
        for (int k = 0; k < evenCount; k++) {
            list.set(evenPositions[k], evenObjects[k]);
        }
    }
}
