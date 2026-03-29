package com.example.ingredient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import javax.sql.DataSource;

@SpringBootApplication
public class IngredientApplication {

	public static void main(String[] args) {
		SpringApplication.run(IngredientApplication.class, args);
	}

	@Bean
	public DataSource dataSource() {
		return new DataSource();
	}
}