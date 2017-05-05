package pc.emil.coffeex.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pc.emil.coffeex.R;
import pc.emil.coffeex.adapters.ActiveSubscriptionsAdapter;
import pc.emil.coffeex.adapters.CoffeeShopAdapter;
import pc.emil.coffeex.models.ActiveSubscription;
import pc.emil.coffeex.models.CoffeeShop;

import static pc.emil.coffeex.activities.LoginActivity.SAVED_EMAIL;
import static pc.emil.coffeex.activities.LoginActivity.SAVED_ID;
import static pc.emil.coffeex.activities.LoginActivity.SAVED_LOGIN;
import static pc.emil.coffeex.activities.LoginActivity.SAVED_PASSWORD;
import static pc.emil.coffeex.activities.LoginActivity.globalUser;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView textView;
    private EditText login;
    private EditText password;
    private EditText email;
    private ProgressBar progressBar;
    private ListView listView;
    private ProgressBar smallProgressBar;


    @Override
    public void onClick(View view) {
        if (globalUser.getId() == -1) {
            Toast.makeText(this, "You should be signed in!", Toast.LENGTH_SHORT).show();
        } else {
            smallProgressBar.setVisibility(View.VISIBLE);
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            new UpdateUserProfile().execute(
                                    login.getText().toString(),
                                    password.getText().toString()
                            );
                        }
                    }
            ).start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        login = (EditText) findViewById(R.id.settings_login);
        password = (EditText) findViewById(R.id.settings_password);
        email = (EditText) findViewById(R.id.settings_email);
        textView = (TextView) findViewById(R.id.settings_textView);
        progressBar = (ProgressBar) findViewById(R.id.settings_progressBar);
        smallProgressBar = (ProgressBar) findViewById(R.id.settings_save_changes_progressBar);
        smallProgressBar.setVisibility(View.INVISIBLE);

        listView = (ListView) findViewById(R.id.lvSubscriptions);
        Button saveChanges = (Button) findViewById(R.id.settings_save_changes);
        saveChanges.setOnClickListener(this);

        if (globalUser.getId() == -1) {
            login.setEnabled(false);
            password.setEnabled(false);
            textView.setText("Nothing to show");
            progressBar.setVisibility(View.INVISIBLE);
        } else {
            login.setText(globalUser.getLogin());
            password.setText(globalUser.getPassword());
            email.setText(globalUser.getEmail());
            progressBar.setVisibility(View.VISIBLE);
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            new LoadSubscriptions().execute(globalUser.getId());
                        }
                    }
            ).start();
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
            if (globalUser.getId() != -1) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Are you sure to delete your profile?");
                builder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new Thread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                new RemoveUserProfile().execute(globalUser.getId());
                                            }
                                        }
                                ).start();
                            }
                        }
                );
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.setCancelable(true);
                builder.show();
            } else {
                Toast.makeText(
                        SettingsActivity.this,
                        "You should be signed in!",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private class LoadSubscriptions extends AsyncTask<Integer, Void, Void> {

        private Connection connection = null;
        private Statement statement = null;
        private ResultSet resultSet = null;

        private String password = "ROOTroot123;";
        private String userName = "Coffee@coffeenure;";
        private String dbName = "Coffee;";

        private ArrayList<ActiveSubscription> subscriptions = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            if (globalUser.getId() != -1) {
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
        }

        @Override
        protected Void doInBackground(Integer... data) {
            if (connection == null) {
                return null;
            } else {
                try {
                    statement = connection.createStatement();
                    resultSet = statement.executeQuery(
                            "SELECT Users.[Subscription].subscription_type_id, " +
                                    "Users.[Subscription_type].title, " +
                                    "Users.[Subscription].date_from, " +
                                    "Users.[Subscription_type].price, " +
                                    "DATEADD(MONTH, (SELECT Users.[Subscription_type].Duration " +
                                    "FROM Users.[Subscription_type] WHERE id = Users.[Subscription].subscription_type_id), " +
                                    "Users.[Subscription].date_from) AS until_date " +
                                    "FROM Users.[Subscription] JOIN Users.[Subscription_type] " +
                                    "ON Users.[Subscription].subscription_type_id = " +
                                    "Users.[Subscription_type].id " +
                                    "WHERE Users.[Subscription].user_id = " + data[0] +
                                    " ORDER BY until_date DESC"
                    );

                    while (resultSet.next()) {
                        ActiveSubscription subscription = new ActiveSubscription(
                                resultSet.getString(2),
                                resultSet.getString(5),
                                resultSet.getFloat(4)
                        );
                        subscriptions.add(subscription);
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

            if (subscriptions.size() == 0) {
                textView.setText("Nothing to show");
            } else {
                listView.setAdapter(new ActiveSubscriptionsAdapter(SettingsActivity.this,
                        subscriptions.toArray(new ActiveSubscription[subscriptions.size()])));
            }

            progressBar.setVisibility(ProgressBar.GONE);
        }
    }

    private class RemoveUserProfile extends AsyncTask<Integer, Void, Void> {

        private Connection connection = null;
        private Statement statement = null;
        private ResultSet resultSet = null;

        private String password = "ROOTroot123;";
        private String userName = "Coffee@coffeenure;";
        private String dbName = "Coffee;";

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
            try {
                statement = connection.createStatement();
                statement.execute(
                        "DELETE FROM Users.[User] WHERE id = " + data[0]
                );

            } catch (Exception e) {
                Log.e("Error", "Error Message: ", e);
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

            SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
            SharedPreferences.Editor ed = sPref.edit();
            ed.putInt(SAVED_ID, -1);
            ed.putString(SAVED_LOGIN, "");
            ed.putString(SAVED_PASSWORD, "");
            ed.putString(SAVED_EMAIL, "");
            ed.apply();

            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private class UpdateUserProfile extends AsyncTask<String, Void, Void> {

        private Connection connection = null;
        private Statement statement = null;
        private ResultSet resultSet = null;

        private String password = "ROOTroot123;";
        private String userName = "Coffee@coffeenure;";
        private String dbName = "Coffee;";

        private String login;
        private String pass;

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
        protected Void doInBackground(String... data) {
            try {
                statement = connection.createStatement();
                statement.execute(
                        "UPDATE Users.[User] SET login = '" + data[0] +
                                "', password = '" + data[1] +
                                "' WHERE id = " + globalUser.getId()
                );
                login = data[0];
                pass = data[1];

            } catch (Exception e) {
                Log.e("Error", "Error Message: ", e);
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

            SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
            SharedPreferences.Editor ed = sPref.edit();
            ed.putString(SAVED_LOGIN, login);
            ed.putString(SAVED_PASSWORD, pass);
            ed.apply();

            Toast.makeText(
                    SettingsActivity.this,
                    "Changes has been saved",
                    Toast.LENGTH_SHORT
            ).show();

            smallProgressBar.setVisibility(View.GONE);

            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
