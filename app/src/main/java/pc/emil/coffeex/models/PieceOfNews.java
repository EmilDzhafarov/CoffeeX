package pc.emil.coffeex.models;

import java.io.Serializable;

public class PieceOfNews implements Serializable {
    private int id;
    private String title;
    private String dateTime;
    private String text;

    public PieceOfNews(int id, String title, String dateTime, String text) {
        this.id = id;
        this.title = title;
        this.dateTime = dateTime;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getText() {
        return text;
    }

    public String getTitle() {
        return title;
    }
}
