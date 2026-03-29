package com.example.ingredient.controller;


import com.example.ingredient.entity.StockValue;
import com.example.ingredient.entity.Unit;
import com.example.ingredient.repository.IngredientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
public class IngredientController {

    private final IngredientRepository ingredientRepository;

    public IngredientController(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    @GetMapping("/ingredients")
    public ResponseEntity<?> getIngredients() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(ingredientRepository.findAll());
    }

    @GetMapping("/ingredients/{id}")
    public ResponseEntity<?> getIngredientById(@PathVariable Integer id) {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(ingredientRepository.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "text/plain")
                    .body("Ingredient.id=" + id + " is not found");
        }
    }

    @GetMapping("/ingredients/{id}/stock")
    public ResponseEntity<?> getIngredientStock(
            @PathVariable Integer id,
            @RequestParam(required = false) String at,
            @RequestParam(required = false) String unit) {

        if (at == null || unit == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "text/plain")
                    .body("Either mandatory query parameter `at` or `unit` is not provided.");
        }

        try {
            Instant instant = Instant.parse(at);
            Unit unitEnum = Unit.valueOf(unit.toUpperCase());
            StockValue stockValue = ingredientRepository.getStockValueAt(id, instant, unitEnum);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(stockValue);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("is not found")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "text/plain")
                        .body("Ingredient.id=" + id + " is not found");
            }
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "text/plain")
                    .body("Invalid parameter: " + e.getMessage());
        }
    }
}