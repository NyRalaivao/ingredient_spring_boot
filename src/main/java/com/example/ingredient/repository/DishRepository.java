package com.example.ingredient.repository;


import com.example.ingredient.entity.*;
import org.springframework.stereotype.Repository;

import com.example.ingredient.datasource.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DishRepository {

    private final DataSource dataSource;

    public DishRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Dish> findAll() {
        List<Dish> dishes = new ArrayList<>();
        String sql = """
                SELECT id, name, selling_price, dish_type
                FROM dish
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Dish dish = mapRow(rs);
                dish.setDishIngredients(findIngredientsByDishId(dish.getId()));
                dishes.add(dish);
            }
            return dishes;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Dish findById(Integer id) {
        String sql = """
                SELECT id, name, selling_price, dish_type
                FROM dish WHERE id = ?
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Dish dish = mapRow(rs);
                    dish.setDishIngredients(findIngredientsByDishId(id));
                    return dish;
                }
            }
            throw new RuntimeException("Dish.id=" + id + " is not found");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Dish updateIngredients(Integer dishId, List<Ingredient> ingredients) {
        findById(dishId);
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            detachIngredients(conn, dishId);
            attachIngredients(conn, dishId, ingredients);
            conn.commit();
            return findById(dishId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void detachIngredients(Connection conn, Integer dishId) throws SQLException {
        String sql = "DELETE FROM dish_ingredient WHERE id_dish = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dishId);
            ps.executeUpdate();
        }
    }

    private void attachIngredients(Connection conn, Integer dishId,
                                   List<Ingredient> ingredients) throws SQLException {
        if (ingredients == null || ingredients.isEmpty()) return;
        String sql = """
                INSERT INTO dish_ingredient (id_ingredient, id_dish, required_quantity, unit)
                VALUES (?, ?, ?, ?::unit)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Ingredient ingredient : ingredients) {
                ps.setInt(1, ingredient.getId());
                ps.setInt(2, dishId);
                ps.setNull(3, Types.DOUBLE);
                ps.setString(4, Unit.KG.name());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private List<DishIngredient> findIngredientsByDishId(Integer dishId) {
        List<DishIngredient> list = new ArrayList<>();
        String sql = """
                SELECT i.id, i.name, i.price, i.category,
                       di.required_quantity, di.unit
                FROM ingredient i
                JOIN dish_ingredient di ON di.id_ingredient = i.id
                WHERE di.id_dish = ?
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dishId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setId(rs.getInt("id"));
                    ingredient.setName(rs.getString("name"));
                    ingredient.setPrice(rs.getDouble("price"));
                    ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));

                    DishIngredient di = new DishIngredient();
                    di.setIngredient(ingredient);
                    di.setQuantity(rs.getObject("required_quantity") == null
                            ? null : rs.getDouble("required_quantity"));
                    di.setUnit(Unit.valueOf(rs.getString("unit")));
                    list.add(di);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Dish mapRow(ResultSet rs) throws SQLException {
        Dish dish = new Dish();
        dish.setId(rs.getInt("id"));
        dish.setName(rs.getString("name"));
        dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
        dish.setSellingPrice(rs.getObject("selling_price") == null
                ? null : rs.getDouble("selling_price"));
        return dish;
    }
}