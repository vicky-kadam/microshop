package com.example.order;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    // product-service ke JSON se sirf price chahiye
    record ProductInfo(Long id, String name, BigDecimal price) {
    }

    private final OrderRepository repository;
    private final RestClient userClient;
    private final RestClient productClient;

    public OrderController(OrderRepository repository,
                           @Value("${services.user-url:http://localhost:8081}") String userUrl,
                           @Value("${services.product-url:http://localhost:8082}") String productUrl) {
        this.repository = repository;
        this.userClient = RestClient.create(userUrl);
        this.productClient = RestClient.create(productUrl);
    }

    @GetMapping
    public List<Order> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOne(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody OrderRequest request) {
        try {
            Map<?, ?> user = get(userClient, "/api/users/" + request.userId(), Map.class);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "user nahi mila"));
            }
            ProductInfo product = get(productClient, "/api/products/" + request.productId(), ProductInfo.class);
            if (product == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "product nahi mila"));
            }

            Order order = new Order();
            order.setUserId(request.userId());
            order.setProductId(request.productId());
            order.setQuantity(request.quantity());
            order.setTotal(product.price().multiply(BigDecimal.valueOf(request.quantity())));
            order.setCreatedAt(LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(order));
        } catch (RestClientException e) {
            // user-service ya product-service tak pahunch nahi paye
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "dusri service abhi available nahi hai"));
        }
    }

    private <T> T get(RestClient client, String path, Class<T> type) {
        try {
            return client.get().uri(path).retrieve().body(type);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }
}
