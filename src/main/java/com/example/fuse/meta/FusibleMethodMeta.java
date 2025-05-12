package com.example.fuse.meta;

import java.lang.reflect.Method;

public class FusibleMethodMeta {
    private final String key;
    private final Class<?> repositoryClass;

    public FusibleMethodMeta(Method m) {
        this.repositoryClass = m.getDeclaringClass();
        this.key = repositoryClass.getName() + "#" + m.getName();
    }

    public String getKey() {
        return key;
    }

    public Class<?> getRepositoryClass() {
        return repositoryClass;
    }
}