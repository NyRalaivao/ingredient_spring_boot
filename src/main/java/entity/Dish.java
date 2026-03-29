package entity;

import java.util.List;

public class Dish {
    private Integer id;
    private String name;
    private Double sellingPrice;
    private DishTypeEnum dishType;
    private List<DishIngredient> dishIngredients;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(Double sellingPrice) { this.sellingPrice = sellingPrice; }

    public DishTypeEnum getDishType() { return dishType; }
    public void setDishType(DishTypeEnum dishType) { this.dishType = dishType; }

    public List<DishIngredient> getDishIngredients() { return dishIngredients; }
    public void setDishIngredients(List<DishIngredient> dishIngredients) { this.dishIngredients = dishIngredients; }
}
