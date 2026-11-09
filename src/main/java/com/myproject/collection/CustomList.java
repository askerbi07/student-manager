package com.myproject.collection;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Простой динамический список (аналог ArrayList).
 * now Iterable — поддерживает for-each и enhanced for.
 */
public class CustomList<T> implements Iterable<T> {

    private static final int DEFAULT_CAPACITY = 10;

    private Object[] elements;
    private int size;

    public CustomList() {
        this(DEFAULT_CAPACITY);
    }

    public CustomList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("initialCapacity < 0: " + initialCapacity);
        }
        this.elements = new Object[initialCapacity];
        this.size = 0;
    }

    private void ensureCapacity(int needed) {
        if (needed <= elements.length) {
            return;
        }
        int newCapacity = elements.length == 0 ? DEFAULT_CAPACITY : elements.length * 2;
        while (newCapacity < needed) {
            newCapacity *= 2;
        }
        elements = Arrays.copyOf(elements, newCapacity);
    }

    public void add(T element) {
        ensureCapacity(size + 1);
        elements[size++] = element;
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
        }
        return (T) elements[index];
    }

    public T set(int index, T element) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
        }
        @SuppressWarnings("unchecked")
        T old = (T) elements[index];
        elements[index] = element;
        return old;
    }

    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    public T removeAt(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
        }
        T removed = (T) elements[index];
        int moved = size - index - 1;
        if (moved > 0) {
            System.arraycopy(elements, index + 1, elements, index, moved);
        }
        elements[--size] = null;
        return removed;
    }

    @Override
    public Iterator<T> iterator() {
        return new CustomListIterator();
    }

    private final class CustomListIterator implements Iterator<T> {

        private int cursor;

        @Override
        public boolean hasNext() {
            return cursor < size;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return (T) elements[cursor++];
        }
    }
}
