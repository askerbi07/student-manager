package com.myproject.collection;

/**
 * Собственная коллекция на обычном массиве Object[].
 * Без ArrayList/LinkedList и без готовых коллекций.
 * Методы по ТЗ: add, get, set, size.
 */
public class CustomList<T> {

    private Object[] elements;
    private int size;

    private static final int DEFAULT_CAPACITY = 10;

    public CustomList() {
        this.elements = new Object[DEFAULT_CAPACITY];
        this.size = 0;
    }

    public void add(T element) {
        ensureCapacity(size + 1);
        elements[size] = element;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        checkIndex(index);
        return (T) elements[index];
    }

    @SuppressWarnings("unchecked")
    public void set(int index, T element) {
        checkIndex(index);
        elements[index] = element;
    }

    public int size() {
        return size;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            elements[i] = null;
        }
        size = 0;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    // Ручное расширение массива (без Arrays.copyOf)
    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= elements.length) {
            return;
        }
        int newCapacity = elements.length * 2;
        if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
        }
        Object[] newElements = new Object[newCapacity];
        for (int i = 0; i < size; i++) {
            newElements[i] = elements[i];
        }
        this.elements = newElements;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            if (i > 0) sb.append(", ");
            sb.append(elements[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
