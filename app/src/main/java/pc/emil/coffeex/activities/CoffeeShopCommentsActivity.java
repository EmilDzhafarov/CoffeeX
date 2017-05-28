package pc.emil.coffeex.activities;

import android.os.AsyncTask;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pc.emil.coffeex.R;
import pc.emil.coffeex.adapters.CommentsAdapter;
import pc.emil.coffeex.models.CoffeeShop;
import pc.emil.coffeex.models.Comment;

import static pc.emil.coffeex.activities.LoginActivity.globalUser;

public class CoffeeShopCommentsActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView listView;
    private ArrayList<Comment> comments = new ArrayList<>();
    private CoffeeShop shop;
    private ProgressBar progressBar;
    private ProgressBar mainProgressBar;
    private TextInputLayout commentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffee_shop_comments);

        Button sendButton = (Button) findViewById(R.id.send_comment_button);
        commentText = (TextInputLayout) findViewById(R.id.comment_text_inputLayout);

        listView = (ListView) findViewById(R.id.lvComments);
        progressBar = (ProgressBar) findViewById(R.id.comment_progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        mainProgressBar = (ProgressBar) findViewById(R.id.comment_main_progressBar);
        Bundle extras = getIntent().getExtras();
        shop = (CoffeeShop) extras.getSerializable("coffee_shop");

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        new LoadComments().execute(shop.getId());
                    }
                }
        ).start();

        sendButton.setOnClickListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send_comment_button:
                if (globalUser.getId() != -1) {
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                    new Thread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    new SendComment().execute(commentText.getEditText().getText().toString());
                                }
                            }
                    ).start();
                } else {
                    Toast.makeText(CoffeeShopCommentsActivity.this,
                           getResources().getString(R.string.should_sign_in),
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        mainProgressBar.setVisibility(ProgressBar.VISIBLE);
        comments.clear();
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        new LoadComments().execute(shop.getId());
                    }
                }
        ).start();
    }

    private class LoadComments extends AsyncTask<Integer, Void, Void> {
        private Connection connection = null;
        private Statement statement = null;
        private ResultSet resultSet = null;

        private String password = "ROOTroot123;";
        private String userName = "Coffee@coffeenure;";
        private String dbName = "Coffee;";

        private ArrayList<Comment> comments = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            String connectionString =
                    "jdbc:jtds:sqlserver://coffeenure.database.windows.net:1433;"
                            + "databaseName=" + dbName
                            + "user=" + userName
                            + "password=" + password
                            + "encrypt=true;"
                            + "trustServerCertificate=false;"
                            + "hostNameInCertificate=*.database.windows.net;"
                            + "loginTimeout=30;";

            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
                connection = DriverManager.getConnection(connectionString);
            } catch (InstantiationException | IllegalAccessException |
                    ClassNotFoundException | SQLException e) {
                Log.e("Error", "Error message", e);
            }
        }

        @Override
        protected Void doInBackground(Integer... data) {
            if (connection == null) {
                return null;
            } else {
                try {
                    statement = connection.createStatement();
                    resultSet = statement.executeQuery(
                            "SELECT Users.[User].login," +
                                    "dbo.Coffee_shop_comments.date_time," +
                                    "dbo.Coffee_shop_comments.text " +
                                    "FROM dbo.Coffee_shop_comments JOIN Users.[User] " +
                                    "ON dbo.Coffee_shop_comments.user_id=Users.[User].id " +
                                    "WHERE dbo.Coffee_shop_comments.coffee_shop_id = " + data[0] +
                                    "ORDER BY dbo.Coffee_shop_comments.date_time DESC"
                    );

                    while (resultSet.next()) {
                        Comment comment = new Comment(
                                resultSet.getString(2).split("\\.")[0],
                                resultSet.getString(3),
                                resultSet.getString(1)
                        );
                        comments.add(comment);
                    }

                } catch (Exception e) {
                    Log.e("Error", "Error Message: ", e);
                }

                return null;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                if (connection != null) {
                    connection.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException ex) {
                Log.e("Error", "Error message", ex);
            }

            listView.setAdapter(new CommentsAdapter(CoffeeShopCommentsActivity.this,
                    comments.toArray(new Comment[comments.size()])));
            listView.setVisibility(View.VISIBLE);
            mainProgressBar.setVisibility(ProgressBar.GONE);

            if (comments.size() == 0) {
                Toast.makeText(CoffeeShopCommentsActivity.this,
                        getResources().getString(R.string.nothing_to_show), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class SendComment extends AsyncTask<String, Void, Void> {
        private Connection connection = null;
        private Statement statement = null;
        private ResultSet resultSet = null;

        private String password = "ROOTroot123;";
        private String userName = "Coffee@coffeenure;";
        private String dbName = "Coffee;";

        private int errorCode = 0;

        @Override
        protected void onPreExecute() {
                String connectionString =
                        "jdbc:jtds:sqlserver://coffeenure.database.windows.net:1433;"
                                + "databaseName=" + dbName
                                + "user=" + userName
                                + "password=" + password
                                + "encrypt=true;"
                                + "trustServerCertificate=false;"
                                + "hostNameInCertificate=*.database.windows.net;"
                                + "loginTimeout=30;";

                try {
                    Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
                    connection = DriverManager.getConnection(connectionString);
                } catch (InstantiationException | IllegalAccessException |
                        ClassNotFoundException | SQLException e) {
                    errorCode = -5;
                    Log.e("Error", "Error message", e);
                }
        }

        @Override
        protected Void doInBackground(String... data) {
            if (data[0].isEmpty()) {
                errorCode = 1;
            } else if (data[0].contains("'")) {
                errorCode = 2;
            } else {
                try {
                    statement = connection.createStatement();
                    statement.execute(
                            "INSERT INTO dbo.Coffee_shop_comments(date_time, user_id, coffee_shop_id, text) " +
                                    "VALUES (DATEADD(HOUR, 3, GETDATE()), " + globalUser.getId() + ", " + shop.getId() +
                                    ", N'" + data[0] + "');"
                    );
                } catch (Exception e) {
                    Log.e("Error", "Error Message: ", e);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                if (connection != null) {
                    connection.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException ex) {
                Log.e("Error", "Error message", ex);
            }

            progressBar.setVisibility(ProgressBar.GONE);

            if (errorCode != 0) {
                switch (errorCode) {
                    case -5:
                        Toast.makeText(
                                CoffeeShopCommentsActivity.this,
                                getResources().getString(R.string.check_internet),
                                Toast.LENGTH_LONG
                        ).show();
                        break;
                    case 1:
                        commentText.setError(getResources().getString(R.string.comment_empty));
                        break;
                    case 2:
                        commentText.setError(getResources().getString(R.string.comment_failure));
                        break;
                }
            } else {
                commentText.setError(null);
                commentText.getEditText().setText("");
                CoffeeShopCommentsActivity.this.onRestart();
            }
        }
    }
}
