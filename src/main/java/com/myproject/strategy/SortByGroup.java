package com.myproject.strategy;

import com.myproject.collection.CustomList;
import com.myproject.model.Student;

/** Сортировка по номеру группы (лексикографический порядок). */
public class SortByGroup implements SortStrategy {

    @Override
    public void sort(CustomList<Student> list) {
        int size = list.size();
        for (int i = 0; i < size - 1; i++) {
            for (int j = 0; j < size - 1 - i; j++) {
                Student a = list.get(j);
                Student b = list.get(j + 1);
                if (compareStrings(a.groupNumber(), b.groupNumber()) > 0) {
                    Student tmp = a;
                    list.set(j, b);
                    list.set(j + 1, tmp);
                }
            }
        }
    }

    // Ручное сравнение строк (без готовых компараторов)
    private int compareStrings(String a, String b) {
        int lenA = a.length();
        int lenB = b.length();
        int common = lenA < lenB ? lenA : lenB;
        for (int i = 0; i < common; i++) {
            char ca = a.charAt(i);
            char cb = b.charAt(i);
            if (ca < cb) return -1;
            if (ca > cb) return 1;
        }
        if (lenA < lenB) return -1;
        if (lenA > lenB) return 1;
        return 0;
    }
}
