package com.notificationservice.controller;

import com.notificationservice.util.ApiResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @PostMapping(value = "/send", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse sendNotification(@RequestBody Map<String, Object> body) {

        String message = body.get("message").toString();

        // Replaced println with proper logging
        logger.info("📩 Notification Received: {}", message);

        return new ApiResponse("Notification Sent", message, true);
    }
}
