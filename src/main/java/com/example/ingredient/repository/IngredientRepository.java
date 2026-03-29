package com.example.ingredient.repository;

import com.example.ingredient.entity.CategoryEnum;
import com.example.ingredient.entity.Ingredient;
import com.example.ingredient.entity.StockValue;
import com.example.ingredient.entity.Unit;
import org.springframework.stereotype.Repository;

import com.example.ingredient.datasource.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class IngredientRepository {

    private final DataSource dataSource;

    public IngredientRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Ingredient> findAll() {
        List<Ingredient> ingredients = new ArrayList<>();
        String sql = "SELECT id, name, price, category FROM ingredient";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ingredients.add(mapRow(rs));
            }
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Ingredient findById(Integer id) {
        String sql = "SELECT id, name, price, category FROM ingredient WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            throw new RuntimeException("Ingredient.id=" + id + " is not found");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public StockValue getStockValueAt(Integer id, Instant at, Unit unit) {
        findById(id);
        String sql = """
                SELECT unit,
                       SUM(CASE
                           WHEN type = 'IN'  THEN quantity
                           WHEN type = 'OUT' THEN -quantity
                           ELSE 0 END) AS actual_quantity
                FROM stock_movement
                WHERE id_ingredient = ?
                  AND unit = ?::unit
                  AND creation_datetime <= ?
                GROUP BY unit
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, unit.name());
            ps.setTimestamp(3, Timestamp.from(at));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StockValue stockValue = new StockValue();
                    stockValue.setQuantity(rs.getDouble("actual_quantity"));
                    stockValue.setUnit(Unit.valueOf(rs.getString("unit")));
                    return stockValue;
                }
            }

            StockValue empty = new StockValue();
            empty.setQuantity(0.0);
            empty.setUnit(unit);
            return empty;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Ingredient mapRow(ResultSet rs) throws SQLException {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(rs.getInt("id"));
        ingredient.setName(rs.getString("name"));
        ingredient.setPrice(rs.getDouble("price"));
        ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
        return ingredient;
    }
}
