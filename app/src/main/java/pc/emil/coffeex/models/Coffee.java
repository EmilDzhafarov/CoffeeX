package pc.emil.coffeex.models;

import java.io.Serializable;

public class Coffee implements Serializable {
    private String name;
    private double price;
    private String description;

    public Coffee(String name, double price, String description) {
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }
}
