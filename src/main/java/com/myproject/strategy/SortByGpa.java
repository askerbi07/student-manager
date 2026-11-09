package com.myproject.strategy;

import com.myproject.collection.CustomList;
import com.myproject.model.Student;

/** Сортировка по среднему баллу (по возрастанию). */
public class SortByGpa implements SortStrategy {

    @Override
    public void sort(CustomList<Student> list) {
        int size = list.size();
        for (int i = 0; i < size - 1; i++) {
            for (int j = 0; j < size - 1 - i; j++) {
                Student a = list.get(j);
                Student b = list.get(j + 1);
                if (a.averageGpa() > b.averageGpa()) {
                    Student tmp = a;
                    list.set(j, b);
                    list.set(j + 1, tmp);
                }
            }
        }
    }
}
