package com.example.fuse;

import com.example.fuse.strategy.SingleKeyStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FuseStarterApplicationTests {
    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();


    @MockitoSpyBean
    private SingleKeyStrategy strategy;

    @Test
    void testParallelRequests() {
        String baseUrl = "http://localhost:" + port + "/api/customers";

        CompletableFuture<Void> request1 = CompletableFuture.runAsync(() -> {
            ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl + "/1", Map.class);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().get("id"));
            assertEquals("Alice", response.getBody().get("name"));
            System.out.println("Request 1 completed: " + response.getBody());
        });

        CompletableFuture<Void> request2 = CompletableFuture.runAsync(() -> {
            ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl + "/2", Map.class);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().get("id"));
            assertEquals("Bob", response.getBody().get("name"));
            System.out.println("Request 2 completed: " + response.getBody());
        });

        // Ждём завершения обоих запросов
        CompletableFuture.allOf(request1, request2).join();
        //подтверждаем, что был совершен только один запрос к БД
        verify(strategy, times(1)).batch(any());
        System.out.println("Оба запроса завершены.");
    }
}

