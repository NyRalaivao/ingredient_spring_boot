package entity;
import java.time.Instant;

public class StockMovement {
    private Integer id;
    private MovementTypeEnum type;
    private Instant creationDatetime;
    private StockValue value;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public MovementTypeEnum getType() { return type; }
    public void setType(MovementTypeEnum type) { this.type = type; }

    public Instant getCreationDatetime() { return creationDatetime; }
    public void setCreationDatetime(Instant creationDatetime) { this.creationDatetime = creationDatetime; }

    public StockValue getValue() { return value; }
    public void setValue(StockValue value) { this.value = value; }
}