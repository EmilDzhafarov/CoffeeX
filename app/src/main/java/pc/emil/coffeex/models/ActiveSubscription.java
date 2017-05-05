package pc.emil.coffeex.models;

public class ActiveSubscription {
    private String title;
    private String untilDate;
    private float price;


    public ActiveSubscription(String title, String untilDate, float price) {
        this.title = title;
        this.untilDate = untilDate;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public String getUntilDate() {
        return untilDate;
    }

    public float getPrice() {
        return price;
    }
}
