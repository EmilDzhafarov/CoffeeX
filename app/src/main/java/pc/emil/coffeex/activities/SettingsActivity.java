package pc.emil.coffeex.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import java.util.regex.Pattern;

import pc.emil.coffeex.R;
import pc.emil.coffeex.adapters.ActiveSubscriptionsAdapter;
import pc.emil.coffeex.models.ActiveSubscription;
import pc.emil.coffeex.models.User;

import static pc.emil.coffeex.activities.LoginActivity.SAVED_EMAIL;
import static pc.emil.coffeex.activities.LoginActivity.SAVED_ID;
import static pc.emil.coffeex.activities.LoginActivity.SAVED_LOGIN;
import static pc.emil.coffeex.activities.LoginActivity.SAVED_PASSWORD;
import static pc.emil.coffeex.activities.LoginActivity.globalUser;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textView;
    private TextInputLayout loginInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout emailInputLayout;
    private ProgressBar progressBar;
    private ListView listView;
    private ProgressBar smallProgressBar;
    private ProgressBar removeUserProgressBar;

    private DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            removeUserProgressBar.setVisibility(View.VISIBLE);
            removeUserProgressBar.setProgress(40);
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            new RemoveUserProfile().execute(globalUser.getId());
                        }
                    }
            ).start();
        }
    };

    @Override
    public void onClick(View view) {
        if (globalUser.getId() == -1) {
            Toast.makeText(this, getResources().getString(R.string.should_sign_in), Toast.LENGTH_SHORT).show();
        } else {
            smallProgressBar.setVisibility(View.VISIBLE);
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            new UpdateUserProfile().execute(
                                    loginInputLayout.getEditText().getText().toString(),
                                    passwordInputLayout.getEditText().getText().toString(),
                                    emailInputLayout.getEditText().getText().toString()
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

        loginInputLayout = (TextInputLayout) findViewById(R.id.settings_login_inputLayout);
        passwordInputLayout = (TextInputLayout) findViewById(R.id.settings_password_inputLayout);
        emailInputLayout = (TextInputLayout) findViewById(R.id.settings_email_inputLayout);
        textView = (TextView) findViewById(R.id.settings_textView);
        progressBar = (ProgressBar) findViewById(R.id.settings_progressBar);
        smallProgressBar = (ProgressBar) findViewById(R.id.settings_save_changes_progressBar);
        smallProgressBar.setVisibility(View.INVISIBLE);
        removeUserProgressBar = (ProgressBar) findViewById(R.id.remove_user_progressBar);
        removeUserProgressBar.setVisibility(View.INVISIBLE);
        removeUserProgressBar.setMax(100);

        listView = (ListView) findViewById(R.id.lvSubscriptions);
        Button saveChanges = (Button) findViewById(R.id.settings_save_changes);
        saveChanges.setOnClickListener(this);

        if (globalUser.getId() == -1) {
            textView.setText(getResources().getString(R.string.nothing_to_show));
            progressBar.setVisibility(View.INVISIBLE);
        } else {
            loginInputLayout.getEditText().setText(globalUser.getLogin());
            passwordInputLayout.getEditText().setText(globalUser.getPassword());
            emailInputLayout.getEditText().setText(globalUser.getEmail());
            loginInputLayout.getEditText().setEnabled(true);
            passwordInputLayout.getEditText().setEnabled(true);
            emailInputLayout.getEditText().setEnabled(true);
            saveChanges.setEnabled(true);
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
                builder.setTitle(getResources().getString(R.string.delete_profile));
                builder.setPositiveButton("OK", listener);
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
                        getResources().getString(R.string.should_sign_in),
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

        private int errorCode = 0;
        private ArrayList<ActiveSubscription> subscriptions = new ArrayList<>();

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
        protected Void doInBackground(Integer... data) {
            if (connection != null) {
                try {
                    statement = connection.createStatement();
                    resultSet = statement.executeQuery(
                            "SELECT Users.[Subscription].subscription_type_id, " +
                                    "Users.[Subscription_type].title, " +
                                    "Users.[Subscription].date_from, " +
                                    "Users.[Subscription_type].price, " +
                                    "DATEADD(MONTH, (SELECT Users.[Subscription_type].Duration " +
                                    "FROM Users.[Subscription_type] WHERE id = " +
                                    "Users.[Subscription].subscription_type_id), " +
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

            if (errorCode == -5) {
                Toast.makeText(
                        SettingsActivity.this,
                        getResources().getString(R.string.check_internet),
                        Toast.LENGTH_LONG
                ).show();
            } else if (subscriptions.size() == 0) {
                textView.setText(getResources().getString(R.string.nothing_to_show));
            } else {
                listView.setAdapter(new ActiveSubscriptionsAdapter(SettingsActivity.this,
                        subscriptions.toArray(new ActiveSubscription[subscriptions.size()])));
            }
        }
    }

    private class RemoveUserProfile extends AsyncTask<Integer, Void, Void> {

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
                removeUserProgressBar.setProgress(60);
            } catch (InstantiationException | IllegalAccessException |
                    ClassNotFoundException | SQLException e) {
                errorCode = -5;
                Log.e("Error", "Error message", e);
            }
        }

        @Override
        protected Void doInBackground(Integer... data) {
            if (connection != null) {
                try {
                    statement = connection.createStatement();
                    statement.execute(
                            "DELETE FROM Users.[User] WHERE id = " + data[0]
                    );

                } catch (Exception e) {
                    Log.e("Error", "Error Message: ", e);
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setProgress(80);
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

            removeUserProgressBar.setProgress(100);
            removeUserProgressBar.setVisibility(View.GONE);

            if (errorCode == -5) {
                Toast.makeText(
                        SettingsActivity.this,
                        getResources().getString(R.string.check_internet),
                        Toast.LENGTH_LONG
                ).show();
            } else {
                SharedPreferences sPref =
                        PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                SharedPreferences.Editor ed = sPref.edit();
                ed.putInt(SAVED_ID, -1);
                ed.putString(SAVED_LOGIN, "");
                ed.putString(SAVED_PASSWORD, "");
                ed.putString(SAVED_EMAIL, "");
                ed.apply();

                Toast.makeText(
                        SettingsActivity.this,
                        getResources().getString(R.string.user_removed),
                        Toast.LENGTH_LONG
                ).show();

                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
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
        private String email;

        private int errorCode = 0;

        @Override
        protected void onPreExecute() {
            String connectionString =
                    "jdbc:jtds:sqlserver://coffeenure.database.windows.net:1433;"
                            + "databaseName=" + dbName
                            + "user=" + userName
                            + "password=" + password
                            + "encrypt=true;"
                            + "trustServerCertificate=true;"
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
            if (connection != null) {
                String loginRegExp = "^[a-zA-Z0-9]+([_ -]?[a-zA-Z0-9])*$";
                String emailRegExp = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";

                Pattern loginPattern = Pattern.compile(loginRegExp);
                Pattern emailPattern = Pattern.compile(emailRegExp, Pattern.CASE_INSENSITIVE);

                if (data[0].isEmpty()) {
                    errorCode = -3;
                } else if (data[1].isEmpty()) {
                    errorCode = -2;
                } else if (data[2].isEmpty()) {
                    errorCode = -1;
                } else if (!loginPattern.matcher(data[0]).matches()) {
                    errorCode = 1;
                } else if (!loginPattern.matcher(data[1]).matches()) {
                    errorCode = 2;
                } else if (data[1].length() < 6) {
                    errorCode = 3;
                } else if (!emailPattern.matcher(data[2]).matches()) {
                    errorCode = 4;
                }

                if (errorCode == 0 &&
                        (!data[0].equals(globalUser.getLogin())
                              || !data[2].equals(globalUser.getEmail())
                              || !data[1].equals(globalUser.getPassword())))
                {
                    try {
                        statement = connection.createStatement();

                        resultSet = statement.executeQuery(
                                "SELECT Users.[User].login, " +
                                        "Users.[User].email " +
                                        "FROM Users.[User]");

                        while (resultSet.next()) {
                            String l = resultSet.getString(1);
                            String e = resultSet.getString(2);

                            if (l.equals(data[0]) && !globalUser.getLogin().equals(l)) {
                                errorCode = 5;
                                return null;
                            } else if (e.equals(data[2]) && !globalUser.getEmail().equals(e)) {
                                errorCode = 6;
                                return null;
                            }
                        }

                        statement.execute(
                                "UPDATE Users.[User] SET login = '" + data[0] +
                                        "', password = '" + data[1] +
                                        "', email = '" + data[2] + "' " +
                                        "WHERE id = " + globalUser.getId()
                        );

                        login = data[0];
                        pass = data[1];
                        email = data[2];

                        globalUser.setLogin(login);
                        globalUser.setPassword(pass);
                        globalUser.setEmail(email);

                    } catch (Exception e) {
                        Log.e("Error", "Error Message: ", e);
                    }
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

            smallProgressBar.setVisibility(View.GONE);

            if (errorCode != 0) {
                switch (errorCode) {
                    case -5:
                        Toast.makeText(
                                SettingsActivity.this,
                                getResources().getString(R.string.check_internet),
                                Toast.LENGTH_LONG
                        ).show();
                        break;
                    case -3:
                        loginInputLayout.setError(getResources().getString(R.string.empty_login));
                        emailInputLayout.setError(null);
                        passwordInputLayout.setError(null);
                        break;
                    case -2:
                        loginInputLayout.setError(null);
                        emailInputLayout.setError(null);
                        passwordInputLayout.setError(getResources().getString(R.string.empty_pass));
                        break;
                    case -1:
                        loginInputLayout.setError(null);
                        emailInputLayout.setError(getResources().getString(R.string.empty_email));
                        passwordInputLayout.setError(null);
                        break;
                    case 1:
                        loginInputLayout.setError(getResources().getString(R.string.login_should));
                        emailInputLayout.setError(null);
                        passwordInputLayout.setError(null);
                        break;
                    case 2:
                        emailInputLayout.setError(null);
                        passwordInputLayout.setError(getResources().getString(R.string.pass_should));
                        loginInputLayout.setError(null);
                        break;
                    case 3:
                        emailInputLayout.setError(null);
                        loginInputLayout.setError(null);
                        passwordInputLayout.setError(getResources().getString(R.string.pass_made_up));
                        break;
                    case 4:
                        loginInputLayout.setError(null);
                        emailInputLayout.setError(getResources().getString(R.string.email_fail));
                        passwordInputLayout.setError(null);
                        break;
                    case 5:
                        emailInputLayout.setError(null);
                        loginInputLayout.setError(getResources().getString(R.string.login_used));
                        passwordInputLayout.setError(null);
                        break;
                    case 6:
                        emailInputLayout.setError(getResources().getString(R.string.email_used));
                        loginInputLayout.setError(null);
                        passwordInputLayout.setError(null);
                        break;
                }
            } else {
                loginInputLayout.setError(null);
                emailInputLayout.setError(null);
                passwordInputLayout.setError(null);

                SharedPreferences sPref =
                        PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                SharedPreferences.Editor ed = sPref.edit();
                ed.putString(SAVED_LOGIN, login);
                ed.putString(SAVED_PASSWORD, pass);
                ed.putString(SAVED_EMAIL, email);
                ed.apply();

                Toast.makeText(
                        SettingsActivity.this,
                        getResources().getString(R.string.saved_changes),
                        Toast.LENGTH_SHORT
                ).show();

                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }
    }
}
