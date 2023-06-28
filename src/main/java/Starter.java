package main.java;

import main.java.model.Producer;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Starter {


    private final static Integer CONSUMER_DELAY = 400;
    private final static Integer PRODUCERS_COUNT = 20;
    private final static Integer CONSUMERS_COUNT = 5;

    public Starter() {
    }

    public void start() {
        final Producer producer = new Producer();
        for (int i = 0; i < CONSUMERS_COUNT; i++) {
            producer.addConsumer(message -> {
                try {
                    Thread.sleep(CONSUMER_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("received: " + message);
            });
        }
        final ExecutorService producers = Executors.newFixedThreadPool(20);
        for (int i = 0; i < PRODUCERS_COUNT; i++) {
            final Thread prod = new Thread(new Runnable() {
                private final Random random = new Random();

                @Override
                public void run() {
                    final int addedNumber = random.nextInt();
                    producer.produce(Integer.toString(addedNumber));
                }
            });
            producers.submit(prod);
        }
        producer.send();
    }

    public static void main(String[] args) {
        final Starter starter = new Starter();
        starter.start();
    }

}
