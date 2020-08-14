import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingQueueMain {

    public static void main(String[] args) throws Exception {
        final BlockingQueueMonitor<Integer> q = new BlockingQueueMonitor<>(15);

        Thread t1 = new Thread(() -> {
            try {
                for (int i = 0; i < 105; i++) {
                    q.enqueue(i);
                    System.out.println("t1 enqueued " + i);
                }
            } catch (InterruptedException ie) {
                System.out.println("Interruption happened");
            }
        });

        Thread t4 = new Thread(() -> {
            try {
                for (int i = 0; i < 105; i++) {
                    q.enqueue(i);
                    System.out.println("t4 enqueued " + i);
                }
            } catch (InterruptedException ie) {
                System.out.println("Interruption happened");
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                for (int i = 0; i < 105; i++) {
                    System.out.println("Thread 2 dequeued: " + q.dequeue());
                }
            } catch (InterruptedException ie) {
                System.out.println("Interruption happened");
            }
        });

        Thread t3 = new Thread(() -> {
            try {
                for (int i = 0; i < 105; i++) {
                    System.out.println("Thread 3 dequeued: " + q.dequeue());
                }
            } catch (InterruptedException ie) {
                System.out.println("Interruption happened");
            }
        });

        t1.start();
        t4.start();
        t2.start();
        t3.start();

        t2.join();
        t3.join();
        t1.join();
        t4.join();
    }
}

class BlockingQueueMonitor<T> {

    T[] array;
    int size = 0;
    int capacity;
    int head = 0;
    int tail = 0;

    public BlockingQueueMonitor(int capacity) {
        array = (T[]) new Object[capacity];
        this.capacity = capacity;
    }

    public synchronized void enqueue(T item) throws InterruptedException {
        while (size == capacity) {
            wait();
        }

        if (tail == capacity) {
            tail = 0;
        }

        array[tail++] = item;
        size++;

        notifyAll();
    }

    public synchronized T dequeue() throws InterruptedException {
        while (size == 0) {
            wait();
        }

        if (head == capacity) {
            head = 0;
        }

        head++;
        size--;

        notifyAll();
        return array[head - 1];
    }
}


class BlockingQueueMutex<T> {

    Lock lock = new ReentrantLock();
    T[] array;
    int size = 0;
    int capacity;
    int head = 0;
    int tail = 0;

    public BlockingQueueMutex(int capacity) {
        array = (T[]) new Object[capacity];
        this.capacity = capacity;
    }

    public void enqueue(T item) {
        lock.lock();
        while (size == capacity) {
            lock.unlock();
            lock.lock();
        }

        if (tail == capacity) {
            tail = 0;
        }

        array[tail++] = item;
        size++;

        lock.lock();
    }

    public T dequeue() {
        lock.lock();

        while (size == 0) {
            lock.unlock();
            lock.lock();
        }

        if (head == capacity) {
            head = 0;
        }

        head++;
        size--;

        lock.unlock();
        return array[head - 1];
    }
}