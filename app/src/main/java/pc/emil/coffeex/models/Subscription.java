package pc.emil.coffeex.models;

import java.io.Serializable;

public class Subscription implements Serializable {

    private String title;
    private int duration;
    private double price;

    public Subscription(String title, int duration, double price) {
        this.title = title;
        this.duration = duration;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public int getDuration() {
        return duration;
    }

    public double getPrice() {
        return price;
    }
}
