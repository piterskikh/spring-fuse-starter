package com.example.fuse;

import com.example.fuse.strategy.SingleKeyStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FuseBus {

    private final FuseProperties props;
    private final SingleKeyStrategy strategy;
    private final Map<String, Bucket<Object>> buckets = new ConcurrentHashMap<>();

    public FuseBus(FuseProperties props, SingleKeyStrategy strategy) {
        this.props = props;
        this.strategy = strategy;
    }

    @SuppressWarnings("unchecked")
    public <R> void enqueue(String key, Object id, CompletableFuture<R> fut) {
        Bucket<R> bucket = (Bucket<R>) buckets.computeIfAbsent(key, Bucket::new);
        bucket.add(id, fut);

        // Ждем накопления элементов за промежуток времени
        if (bucket.getIds().size() == 1) {
            try {
                Thread.sleep(props.getWindowMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            flush(key);
        }
    }

    private void flush(String key) {
        Bucket<Object> bucket = buckets.remove(key);
        if (bucket != null) {
            strategy.batch(bucket);
        }
    }
}
