package com.example.fuse.meta;

import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class FuseMetaRegistry {

    private final ConcurrentMap<String, FusibleMethodMeta> cache = new ConcurrentHashMap<>();

    /** called from your Aspect to register the Method’s metadata */
    public FusibleMethodMeta computeIfAbsent(Method m) {
        String key = m.getDeclaringClass().getName() + "#" + m.getName();
        return cache.computeIfAbsent(key, k -> new FusibleMethodMeta(m));
    }

    /** lookup later in your batching strategy by the same key */
    public FusibleMethodMeta getByKey(String key) {
        return cache.get(key);
    }
}