package com.taskflow.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TaskFlowApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskFlowApiApplication.class, args);
	}

}

/**
 * WHAT HAPPENS WHEN THIS RUNS:
 *
 * 1. Spring Boot scans for @Component, @Service, @Repository, @Controller annotations
 * 2. Auto-configuration kicks in based on dependencies in classpath
 * 3. Database connection is established using application.yml properties
 * 4. JPA repositories are created automatically
 * 5. Web server (Tomcat) starts on port 8080
 * 6. Security filter chain is initialized
 * 7. Application is ready to receive HTTP requests
 */