package com.example.ingredient.controller;

import com.example.ingredient.entity.Ingredient;
import com.example.ingredient.repository.DishRepository;
import com.example.ingredient.repository.IngredientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DishController {

    private final DishRepository dishRepository;
    private final IngredientRepository ingredientRepository;

    public DishController(DishRepository dishRepository,
                          IngredientRepository ingredientRepository) {
        this.dishRepository = dishRepository;
        this.ingredientRepository = ingredientRepository;
    }

    @GetMapping("/dishes")
    public ResponseEntity<?> getDishes() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(dishRepository.findAll());
    }

    @PutMapping("/dishes/{id}/ingredients")
    public ResponseEntity<?> updateDishIngredients(
            @PathVariable Integer id,
            @RequestBody(required = false) List<Ingredient> ingredients) {

        if (ingredients == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "text/plain")
                    .body("Request body is required and must contain a list of ingredients.");
        }

        List<Ingredient> existingIngredients = ingredients.stream()
                .filter(ingredient -> {
                    try {
                        ingredientRepository.findById(ingredient.getId());
                        return true;
                    } catch (RuntimeException e) {
                        return false;
                    }
                })
                .toList();

        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(dishRepository.updateIngredients(id, existingIngredients));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "text/plain")
                    .body("Dish.id=" + id + " is not found");
        }
    }

    @GetMapping("/dishes/{id}/ingredients")
    public ResponseEntity<?> getDishIngredients(
            @PathVariable Integer id,
            @RequestParam(required = false) String ingredientName,
            @RequestParam(required = false) Double ingredientPriceAround) {

        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(dishRepository.findIngredientsByDishIdWithFilters(
                            id, ingredientName, ingredientPriceAround));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "text/plain")
                    .body("Dish.id=" + id + " is not found");
        }
    }
}
