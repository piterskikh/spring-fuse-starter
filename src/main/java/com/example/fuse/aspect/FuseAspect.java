package com.example.fuse.aspect;

import com.example.fuse.FuseBus;
import com.example.fuse.meta.FuseMetaRegistry;
import com.example.fuse.meta.FusibleMethodMeta;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Aspect
@Component
public class FuseAspect {

    private final FuseBus fuseBus;
    private final FuseMetaRegistry metaRegistry;

    public FuseAspect(FuseBus fuseBus, FuseMetaRegistry metaRegistry) {
        this.fuseBus = fuseBus;
        this.metaRegistry = metaRegistry;
    }

    @Around("@annotation(com.example.fuse.annotation.Fusible)")
    public Object aroundFusible(ProceedingJoinPoint pjp) throws Throwable {

        // Если вызывающий поток НЕ виртуальный — просто выполнить исходный метод
        if (!Thread.currentThread().isVirtual()) {
            return pjp.proceed();
        }

        MethodSignature sig    = (MethodSignature) pjp.getSignature();
        Method          method = sig.getMethod();

        // 1) зарегистрировать метаданные этого метода
        FusibleMethodMeta meta = metaRegistry.computeIfAbsent(method);

        // 2) одноаргументный идентификатор
        Object id = pjp.getArgs()[0];

        // 3) поставить в очередь и заблокировать для получения объекта
        CompletableFuture<Object> fut = new CompletableFuture<>();
        fuseBus.enqueue(meta.getKey(), id, fut);
        Object entity = fut.get();  // carrier-thread освобождается

        // 4) если возвращаемый тип объявлен как Optional, обернуть объект
        if (Optional.class.equals(method.getReturnType())) {
            return Optional.ofNullable(entity);
        }
        return entity;
    }
}
