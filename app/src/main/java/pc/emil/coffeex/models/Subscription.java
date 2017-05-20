package pc.emil.coffeex.models;

import java.io.Serializable;

public class Subscription implements Serializable {

    private int id;
    private String title;
    private int duration;
    private double price;
    private String description;
    private boolean isBuyed;

    public Subscription(int id, String title, int duration, double price, String description) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.price = price;
        this.description = description;
    }

    public void setBuyed(boolean buyed) {
        isBuyed = buyed;
    }

    public boolean isBuyed() {
        return isBuyed;
    }

    public int getId() {
        return id;
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

    public String getDescription() {
        return description;
    }
}
