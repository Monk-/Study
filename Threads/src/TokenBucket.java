import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TokenBucket {

    public static void main(String []args) throws InterruptedException {
        Set<Thread> allThreads = new HashSet<>();
        final Bucket bucket = new Bucket(3);

        for (int i = 0; i < 100; i++) {

            Thread thread = new Thread(bucket::getTokenLock);
            thread.setName("Thread_" + (i + 1));
            allThreads.add(thread);
        }

        for (Thread t : allThreads) {
            t.start();
        }

        for (Thread t : allThreads) {
            t.join();
        }
    }
}


class Bucket {

    private final int MAX_TOKENS;
    private long lastRequestTime = System.currentTimeMillis();
    long possibleTokens = 0;

    private final Lock lock = new ReentrantLock();

    public Bucket(int maxTokens) {
        MAX_TOKENS = maxTokens;
    }

    void getTokenLock() {
        lock.lock();

        while (doesTokenAvailable()) {
            lock.unlock();
            lock.lock();
        }

        possibleTokens--;
        lastRequestTime = System.currentTimeMillis();

        System.out.println(
                "Granting " + Thread.currentThread().getName() + " token at " + LocalTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault()).getSecond());
        lock.unlock();
    }

    boolean doesTokenAvailable() {
        possibleTokens += (System.currentTimeMillis() - lastRequestTime) / 1000;

        if (possibleTokens > MAX_TOKENS) {
            possibleTokens = MAX_TOKENS;
        }

        return possibleTokens != 0;
    }

    synchronized void getToken() throws InterruptedException {

        // Divide by a 1000 to get granularity at the second level.
        possibleTokens += (System.currentTimeMillis() - lastRequestTime) / 1000;

        if (possibleTokens > MAX_TOKENS) {
            possibleTokens = MAX_TOKENS;
        }

        if (possibleTokens == 0) {
            Thread.sleep(1000);
        } else {
            possibleTokens--;
        }
        lastRequestTime = System.currentTimeMillis();

        System.out.println(
                "Granting " + Thread.currentThread().getName() + " token at " + (System.currentTimeMillis() / 1000));
    }
}