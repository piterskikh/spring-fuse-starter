package com.example.fuse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Bucket<R> {
    private final String key;
    private final List<Object> ids = new ArrayList<>();
    private final List<CompletableFuture<R>> futures = new ArrayList<>();

    public Bucket(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public List<Object> getIds() {
        return ids;
    }

    public List<CompletableFuture<R>> getFutures() {
        return futures;
    }

    public void add(Object id, CompletableFuture<R> fut) {
        ids.add(id);
        futures.add(fut);
    }
}
