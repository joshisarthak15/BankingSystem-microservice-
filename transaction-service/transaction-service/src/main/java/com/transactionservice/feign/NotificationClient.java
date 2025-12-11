package com.transactionservice.feign;

import com.transactionservice.util.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/api/notifications/send")
    ApiResponse sendNotification(@RequestBody Map<String, Object> body);
}
