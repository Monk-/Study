import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MissedSignals {

    public static void main(String[] args) throws InterruptedException {
        //WrongSendingSignal wrongSendingSignal = new WrongSendingSignal();
        //wrongSendingSignal.run();

        CorrectSendingSignal correctSendingSignal = new CorrectSendingSignal();
        correctSendingSignal.run();
    }

    static class Predicate {
        public Boolean value = false;
    }


    /* Signal is sent by wrongSignaller, but waiter never gets it */
    static class WrongSendingSignal {
        final Lock lock = new ReentrantLock();
        final Condition condition = lock.newCondition();

        Thread wrongSignaller = new Thread(() -> {
            lock.lock();
            condition.signal();
            System.out.println("Sent signal");
            lock.unlock();
        });

        Thread wrongWaiter = new Thread(() -> {
            lock.lock();
            try {
                condition.await();  // we are waiting for a signal that will never come
                System.out.println("Received signal");
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt(); // set interrupt flag
                System.out.println("Interruption happened");
            }
            lock.unlock();

        });

        public void run() throws InterruptedException {
            wrongSignaller.start();
            wrongSignaller.join();

            wrongWaiter.start();
            wrongWaiter.join();

            System.out.println("Done.");
        }
    }


    /* Signal is sent by correctSignaler and predicate is changed,
    correctWaiter checks predicate and don't even go on a waiting queue just goes with carrying on it's task*/
    static class CorrectSendingSignal {
        final Lock lock = new ReentrantLock();
        final Condition condition = lock.newCondition();
        final Predicate predicate = new Predicate();

        Thread correctSignaller = new Thread(() -> {
            lock.lock();
            // do important stuff
            predicate.value = true;
            condition.signal();
            System.out.println("Sent signal");
            lock.unlock();
        });

        Thread correctWaiter = new Thread(() -> {

            lock.lock();
            try {
                while (!predicate.value) { // with addition of a predicate we avoid waiting or signal that will never come
                    condition.await();
                }
                // do what we want to do
                System.out.println("Received signal");
            } catch (InterruptedException ie) {
                System.out.println("Interruption happened");
            }
            lock.unlock();

        });

        public void run() throws InterruptedException {
            correctSignaller.start();
            correctSignaller.join();

            correctWaiter.start();
            correctWaiter.join();

            System.out.println("Done.");
        }
    }


}
