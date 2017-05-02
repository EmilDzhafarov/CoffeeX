package pc.emil.coffeex.models;

import java.util.Calendar;

public class Comment {
    private String text;
    private String dateTime;
    private String userLogin;


    public Comment(String date, String text, String userLogin) {
        this.text = text;
        this.dateTime = date;
        this.userLogin = userLogin;
    }

    public String getText() {
        return text;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getUserLogin() {
        return userLogin;
    }
}
