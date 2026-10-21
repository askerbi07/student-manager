package com.myproject.thread;

import com.myproject.collection.CustomList;
import com.myproject.model.Student;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Доп. задание 4: многопоточный подсчёт вхождений элемента N (зачётки с номером N)
 * с использованием ExecutorService и AtomicInteger.
 *
 * Ожидание завершения потоков: массив Future<?> + get(timeout) — это даёт и ожидание,
 * и защиту от бесконечного зависания, если задача упала или повисла.
 * Пул гарантированно освобождается в finally (иначе при прерывании потоки утекают
 * и JVM не может завершиться).
 */
public class OccurrenceCounter {

    /** Максимальное время ожидания одной задачи, секунды. */
    private static final long TASK_TIMEOUT_SECONDS = 10;

    public static int countAndPrint(CustomList<Student> list, int targetGradeBook) throws InterruptedException {
        int result = countOccurrences(list, targetGradeBook);
        System.out.println("Многопоточный подсчёт вхождений зачётки №" + targetGradeBook + " = " + result);
        return result;
    }

    public static int countOccurrences(CustomList<Student> list, int targetGradeBook) throws InterruptedException {
        int size = list.size();
        if (size == 0) {
            return 0;
        }

        int threadCount = Math.min(4, size);
        int chunkSize = (size + threadCount - 1) / threadCount;

        // Разбиваем диапазон на чанки (массив, не коллекция).
        int[][] chunks = new int[threadCount][2];
        int taskCount = 0;
        for (int t = 0; t < threadCount; t++) {
            int from = t * chunkSize;
            if (from >= size) {
                break;
            }
            int to = Math.min(from + chunkSize, size);
            chunks[taskCount][0] = from;
            chunks[taskCount][1] = to;
            taskCount++;
        }

        ExecutorService executor = Executors.newFixedThreadPool(taskCount);
        AtomicInteger counter = new AtomicInteger(0);
        Future<?>[] futures = new Future<?>[taskCount];

        try {
            for (int t = 0; t < taskCount; t++) {
                final int from = chunks[t][0];
                final int to = chunks[t][1];
                futures[t] = executor.submit(() -> {
                    for (int i = from; i < to; i++) {
                        if (list.get(i).getGradeBookNumber() == targetGradeBook) {
                            counter.incrementAndGet();
                        }
                    }
                });
            }

            // Ожидание завершения всех задач с таймаутом:
            // get() возвращается только после завершения задачи, а по таймауту
            // бросает TimeoutException — JVM не подвиснет навсегда.
            for (Future<?> future : futures) {
                try {
                    future.get(TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    throw new IllegalStateException(
                            "Поток подсчёта не завершился за " + TASK_TIMEOUT_SECONDS + " секунд.", e);
                } catch (ExecutionException e) {
                    throw new IllegalStateException(
                            "Ошибка в потоке подсчёта: " + e.getCause(), e.getCause());
                }
            }

            return counter.get();
        } finally {
            // Освобождаем пул в любом случае: и при успехе, и при исключении
            // (в том числе InterruptedException). Иначе non-daemon потоки пула
            // остаются живыми и JVM не завершается.
            executor.shutdownNow();
        }
    }
}
