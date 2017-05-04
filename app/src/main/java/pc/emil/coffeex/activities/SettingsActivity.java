package pc.emil.coffeex.activities;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pc.emil.coffeex.R;
import pc.emil.coffeex.adapters.CommentsAdapter;
import pc.emil.coffeex.models.Comment;
import pc.emil.coffeex.models.Subscription;

import static pc.emil.coffeex.activities.LoginActivity.globalUser;

public class SettingsActivity extends AppCompatActivity {

    private TextView textView;
    private EditText login;
    private EditText password;
    private EditText email;
    private ProgressBar progressBar;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        login = (EditText) findViewById(R.id.settings_login);
        password = (EditText) findViewById(R.id.settings_password);
        email = (EditText) findViewById(R.id.settings_email);
        textView = (TextView) findViewById(R.id.settings_textView);
        progressBar = (ProgressBar) findViewById(R.id.settings_progressBar);
        listView = (ListView) findViewById(R.id.lvSubscriptions);

        if (globalUser.getId() == -1) {
            login.setEnabled(false);
            password.setEnabled(false);
        } else {
            login.setText(globalUser.getLogin());
            password.setText(globalUser.getPassword());
            email.setText(globalUser.getEmail());

//            new LoadSubscriptions().execute(globalUser.getId());
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.remove_user_badge) {

        }

        return super.onOptionsItemSelected(item);
    }

//    private class LoadSubscriptions extends AsyncTask<Integer, Void, Void> {
//        private Connection connection = null;
//        private Statement statement = null;
//        private ResultSet resultSet = null;
//
//        private String password = "ROOTroot123;";
//        private String userName = "Coffee@coffeenure;";
//        private String dbName = "Coffee;";
//
//        private ArrayList<Subscription> subscriptions = new ArrayList<>();
//
//        @Override
//        protected void onPreExecute() {
//            String connectionString =
//                    "jdbc:jtds:sqlserver://coffeenure.database.windows.net:1433;"
//                            + "databaseName=" + dbName
//                            + "user=" + userName
//                            + "password=" + password
//                            + "encrypt=true;"
//                            + "trustServerCertificate=false;"
//                            + "hostNameInCertificate=*.database.windows.net;"
//                            + "loginTimeout=30;";
//
//            try {
//                Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
//                connection = DriverManager.getConnection(connectionString);
//            } catch (InstantiationException | IllegalAccessException |
//                    ClassNotFoundException | SQLException e) {
//                Log.e("Error", "Error message", e);
//            }
//        }
//
//        @Override
//        protected Void doInBackground(Integer... data) {
//            if (connection == null) {
//                return null;
//            } else {
//                try {
//                    statement = connection.createStatement();
//                    resultSet = statement.executeQuery(
//                            "SELECT Users.[User].login," +
//                                    "dbo.Coffee_shop_comments.date_time," +
//                                    "dbo.Coffee_shop_comments.text " +
//                                    "FROM dbo.Coffee_shop_comments JOIN Users.[User] " +
//                                    "ON dbo.Coffee_shop_comments.user_id=Users.[User].id " +
//                                    "WHERE dbo.Coffee_shop_comments.coffee_shop_id = " + data[0] +
//                                    "ORDER BY dbo.Coffee_shop_comments.date_time DESC"
//                    );
//
//                    while (resultSet.next()) {
//                        Subscription subscription = new Subscription(
//                                resultSet.getString(2).split("\\.")[0],
//                                resultSet.getString(3),
//                                resultSet.getString(1)
//                        );
//                        subscriptions.add(subscription);
//                    }
//
//                } catch (Exception e) {
//                    Log.e("Error", "Error Message: ", e);
//                }
//
//                return null;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            try {
//                if (connection != null) {
//                    connection.close();
//                }
//                if (statement != null) {
//                    statement.close();
//                }
//                if (resultSet != null) {
//                    resultSet.close();
//                }
//            } catch (SQLException ex) {
//                Log.e("Error", "Error message", ex);
//            }
//
//            listView.setAdapter(new CommentsAdapter(CoffeeShopCommentsActivity.this,
//                    subscriptions.toArray(new Comment[subscriptions.size()])));
//            progressBar.setVisibility(ProgressBar.GONE);
//
//            textView.setText("Nothing to show");
//        }
//    }
}
