package main.java.model;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Producer {
    private final LinkedBlockingQueue<String> messageBuffer;
    private final List<LocalDateTime> limitedTimeMessageBuffer;
    private final List<Consumer> consumers = new ArrayList<>();


    private final static Integer LIMIT = 5;
    private final static Integer LIMITED_TIME = 1000;


    public Producer() {
        this.messageBuffer = new LinkedBlockingQueue<>();
        this.limitedTimeMessageBuffer = Collections.synchronizedList(new ArrayList<>());
    }

    public void addConsumer(final Consumer consumer) {
        consumers.add(consumer);
    }

    public void produce(final String message) {
        synchronized (limitedTimeMessageBuffer) {
            if (limitedTimeMessageBuffer.size() >= LIMIT) {
                if (limitedTimeMessageBuffer
                        .stream()
                        .anyMatch(messageTime -> Duration.between(LocalDateTime.now(), messageTime).toMillis() > LIMITED_TIME)) {
                    limitedTimeMessageBuffer.clear();
                } else {
                    try {
                        final long sleepTime = LIMITED_TIME - Duration
                                .between(LocalDateTime.now(), limitedTimeMessageBuffer.get(0))
                                .toMillis();
                        Thread.sleep(sleepTime);
                        limitedTimeMessageBuffer.clear();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                limitedTimeMessageBuffer.add(LocalDateTime.now());
            }
        }
        messageBuffer.add(message);
        System.out.println("sent: " + message);
    }

    public void send() {
        consumers.stream().parallel().forEach(consumer -> {
            try {
                consumer.consume(String.valueOf(messageBuffer.poll(LIMITED_TIME, TimeUnit.MILLISECONDS)));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        send();
    }
}