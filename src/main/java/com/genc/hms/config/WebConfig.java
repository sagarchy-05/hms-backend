package com.genc.hms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	// Configures Cross-Origin Resource Sharing (CORS) for frontend-backend
	// interaction.
	// This allows the frontend (running on a different origin like localhost:5500)
	// to send API requests to the Spring Boot backend without being blocked by
	// browser CORS policy.
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**") // Apply CORS policy to all API endpoints
				.allowedOrigins("http://localhost:5500", "https://jeevan-hms.vercel.app/") // Permitted frontend origins during development
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // HTTP methods allowed for cross-origin
																			// requests
				.allowedHeaders("*").allowCredentials(true); // Enables cookies or authentication headers to be sent
																// with requests
	}
}
