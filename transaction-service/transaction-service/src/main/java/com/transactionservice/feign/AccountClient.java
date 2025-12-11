package com.transactionservice.feign;

import com.transactionservice.util.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "account-service")
public interface AccountClient {

    @PutMapping("/api/accounts/{accountNumber}/balance")
    ApiResponse updateBalance(@PathVariable String accountNumber,
                              @RequestBody Map<String, Object> body);

    @GetMapping("/api/accounts/{accountNumber}")
    ApiResponse getAccount(@PathVariable String accountNumber);
}
