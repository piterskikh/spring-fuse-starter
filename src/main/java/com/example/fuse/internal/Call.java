package com.example.fuse.internal;

import java.util.concurrent.CompletableFuture;

public class Call<R> {
    public final Object id;
    public final CompletableFuture<R> future;
    public Call(Object id, CompletableFuture<R> future) {
        this.id = id;
        this.future = future;
    }
}
