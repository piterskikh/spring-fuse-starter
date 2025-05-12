package com.example.fuse.strategy;

import com.example.fuse.ApplicationContextProvider;
import com.example.fuse.Bucket;
import com.example.fuse.meta.FuseMetaRegistry;
import com.example.fuse.meta.FusibleMethodMeta;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class SingleKeyStrategy {

    private final FuseMetaRegistry metaRegistry;

    public SingleKeyStrategy(FuseMetaRegistry metaRegistry) {
        this.metaRegistry = metaRegistry;
    }

    @SuppressWarnings("unchecked")
    public <R> void batch(Bucket<R> bucket) {
        // 1) ищем метаданные, которые мы сохранили в аспекте
        FusibleMethodMeta meta = metaRegistry.getByKey(bucket.getKey());
        if (meta == null) {
            throw new IllegalStateException("meta not found for " + bucket.getKey());
        }

        // 2) получаем класс интерфейса репозитория
        Class<?> repoClass = meta.getRepositoryClass();

        // 3) получаем Spring-бин и приводим его к типам R как сущности и Object как идентификатора
        CrudRepository<R, Object> repo =
                (CrudRepository<R, Object>) ApplicationContextProvider.getBean(repoClass);

        // 4) собираем все идентификаторы
        List<Object> ids = new ArrayList<>(bucket.getIds());

        // 5) получаем их пакетно
        Iterable<R> entities = repo.findAllById(ids);

        // 6) сопоставляем id→сущность через рефлексию
        Map<Object, R> byId = new HashMap<>();
        try {
            Method getId = null;
            for (R e : entities) {
                if (getId == null) {
                    getId = e.getClass().getMethod("getId");
                }
                Object idVal = getId.invoke(e);
                byId.put(idVal, e);
            }
        } catch (Exception ex) {
            // если происходит ошибка, завершаем каждый future с исключением
            bucket.getFutures().forEach(f -> f.completeExceptionally(ex));
            return;
        }

        // 7) завершаем каждый future соответствующей сущностью
        List<CompletableFuture<R>> futs = bucket.getFutures();
        for (int i = 0; i < ids.size(); i++) {
            futs.get(i).complete(byId.get(ids.get(i)));
        }
    }
}
