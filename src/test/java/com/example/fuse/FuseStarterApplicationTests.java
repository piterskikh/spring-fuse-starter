package com.example.fuse;

import com.example.fuse.strategy.SingleKeyStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FuseStarterApplicationTests {
    @Autowired
    private CustomerRepository repo;

    @MockitoSpyBean
    private SingleKeyStrategy strategy;

    @Test
    void testBatchingFindById() throws Exception {
        // создаем ExecutorService, который на каждый таск порождает новую виртуальную нить
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Callable<Customer> task1 = () -> repo.findById(1L).get();
            Callable<Customer> task2 = () -> repo.findById(2L).get();

            // invokeAll запустит оба таска одновременно (каждый — в своей виртуальной нити)
            List<Future<Customer>> futures = executor.invokeAll(List.of(task1, task2));
            Customer c1 = futures.get(0).get();
            Customer c2 = futures.get(1).get();

            assertNotNull(c1);
            assertEquals("Alice", c1.getName());
            assertNotNull(c2);
            assertEquals("Bob", c2.getName());

            // даем время на flush
            Thread.sleep(200);

            verify(strategy, times(1)).batch(any());
        }
    }
}
