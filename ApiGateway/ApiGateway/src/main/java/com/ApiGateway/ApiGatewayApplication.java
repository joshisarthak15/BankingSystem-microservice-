package com.ApiGateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;

import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

@SpringBootApplication(scanBasePackages = "com")
@EnableDiscoveryClient
public class ApiGatewayApplication {

	private static final Logger logger = LoggerFactory.getLogger(ApiGatewayApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
		logger.info("API Gateway Application UP!!");
	}

	// -------------------- GLOBAL REQUEST/RESPONSE LOGGING FILTER --------------------
	@Bean
	public GlobalFilter gatewayLoggingFilter() {
		return (exchange, chain) -> {

			ServerHttpRequest request = exchange.getRequest();

			logger.info("GATEWAY REQUEST: {} {}", request.getMethod(), request.getURI());

			return chain.filter(exchange).then(Mono.fromRunnable(() -> {
				logger.info("GATEWAY RESPONSE: {} -> {}",
						request.getURI(),
						exchange.getResponse().getStatusCode());
			}));
		};
	}
}
