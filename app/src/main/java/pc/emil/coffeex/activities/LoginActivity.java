package pc.emil.coffeex.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import pc.emil.coffeex.R;
import pc.emil.coffeex.models.User;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener{

    private EditText logEd;
    private EditText passEd;
    private ProgressBar progressBar;
    public static final String SAVED_LOGIN = "saved_login";
    public static final String SAVED_PASSWORD = "saved_password";
    public static final String SAVED_EMAIL = "saved_email";
    public static final String SAVED_ID = "saved_id";
    public static User globalUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button signIn = (Button) findViewById(R.id.sign_in_button);
        signIn.setOnClickListener(this);

        Button signUp = (Button) findViewById(R.id.sign_up_button);
        signUp.setOnClickListener(this);

        logEd = (EditText) findViewById(R.id.login);
        passEd = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(ProgressBar.INVISIBLE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.login_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        TextView userLogin = (TextView) headerView.findViewById(R.id.user_name);
        TextView userEmail = (TextView) headerView.findViewById(R.id.user_email);

        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
        int id = sPref.getInt(LoginActivity.SAVED_ID, -1);
        String login = sPref.getString(LoginActivity.SAVED_LOGIN, "");
        String pass = sPref.getString(LoginActivity.SAVED_PASSWORD, "");
        String email = sPref.getString(LoginActivity.SAVED_EMAIL, "");

        globalUser = new User(id, login, pass, email);

        userLogin.setText(login);
        userEmail.setText(email);

        if (!login.equals("") &&!email.equals("")) {
            Menu menu = navigationView.getMenu();
            MenuItem item = menu.findItem(R.id.sign_in_item);
            item.setTitle("Sign out");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button :
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                CheckingUser check = new CheckingUser();
                                check.execute(logEd.getText().toString(), passEd.getText().toString());
                            }
                        }
                ).start();
                progressBar.setVisibility(ProgressBar.VISIBLE);
                break;
            case R.id.sign_up_button :
                Intent intent = new Intent(this, RegisterUserActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.login_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.sign_in_item) {
            if (item.getTitle().equals("Sign out")) {
                SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor ed = sPref.edit();
                ed.putString(SAVED_LOGIN, "");
                ed.putString(SAVED_PASSWORD, "");
                ed.putString(SAVED_EMAIL, "");
                ed.apply();
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Class dest = LoginActivity.class;
                if (this.getClass() != dest) {
                    Intent intent = new Intent(this, dest);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        } else if (id == R.id.nav_subscriptions) {
            Class dest = SubscriptionsActivity.class;
            if (this.getClass() != dest) {
                Intent intent = new Intent(this, dest);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_coffee_shops) {
            Class dest = MainActivity.class;
            if (this.getClass() != dest) {
                Intent intent = new Intent(this, dest);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.login_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class CheckingUser extends AsyncTask<String, Void, Void> {

        private boolean find = false;

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
        protected Void doInBackground(String... data) {
            try {
                statement = connection.createStatement();
                resultSet = statement.executeQuery(
                        "SELECT Users.[User].id, " +
                                "Users.[User].login, " +
                                "Users.[User].password," +
                                "Users.[User].email " +
                                "FROM Users.[User]");

                while (resultSet.next()) {
                    if (resultSet.getString(2).equals(data[0]) &&
                            resultSet.getString(3).equals(data[1])) {
                        find = true;
                        globalUser = new User(
                                resultSet.getInt(1),
                                resultSet.getString(2),
                                resultSet.getString(3),
                                resultSet.getString(4)
                        );
                        return null;
                    }
                }
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

            if (find) {
                SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                SharedPreferences.Editor ed = sPref.edit();
                ed.putInt(SAVED_ID, globalUser.getId());
                ed.putString(SAVED_LOGIN, globalUser.getLogin());
                ed.putString(SAVED_PASSWORD, globalUser.getPassword());
                ed.putString(SAVED_EMAIL, globalUser.getEmail());
                ed.apply();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(LoginActivity.this,
                        "User not found! Please sign up",
                        Toast.LENGTH_LONG).show();
            }

            LoginActivity.this.progressBar.setVisibility(ProgressBar.GONE);
        }
    }
}
