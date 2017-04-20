package pc.emil.coffeex.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.net.Inet4Address;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import pc.emil.coffeex.R;

public class RegisterUserActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener {

    private EditText registerLogin;
    private EditText registerPassword;
    private EditText checkRegisterPassword;
    private EditText registerEmail;
    private ProgressBar registerProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        Toolbar toolbar = (Toolbar) findViewById(R.id.register_user_toolbar);
        setSupportActionBar(toolbar);

        registerLogin = (EditText) findViewById(R.id.register_login);
        registerPassword = (EditText) findViewById(R.id.register_password);
        checkRegisterPassword = (EditText) findViewById(R.id.check_register_password);
        registerEmail = (EditText) findViewById(R.id.register_email);
        registerProgressBar = (ProgressBar) findViewById(R.id.register_progressBar);
        registerProgressBar.setIndeterminate(false);
        registerProgressBar.setVisibility(ProgressBar.INVISIBLE);
        Button registerBtn = (Button) findViewById(R.id.register_button);
        registerBtn.setOnClickListener(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.register_user_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onClick(View view) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        RegisteringUser check = new RegisteringUser();
                        check.execute(registerLogin.getText().toString(),
                                registerPassword.getText().toString(),
                                checkRegisterPassword.getText().toString(),
                                registerEmail.getText().toString());
                    }
                }
        ).start();
        registerProgressBar.setVisibility(ProgressBar.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.register_user_drawer_layout);
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
            Class dest = LoginActivity.class;
            if (this.getClass() != dest) {
                Intent intent = new Intent(this, dest);
                startActivity(intent);
            }
        } else if (id == R.id.nav_subscriptions) {
            Class dest = SubscriptionsActivity.class;
            if (this.getClass() != dest) {
                Intent intent = new Intent(this, dest);
                startActivity(intent);
            }
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_coffee_shops) {
            Class dest = MainActivity.class;
            if (this.getClass() != dest) {
                Intent intent = new Intent(this, dest);
                startActivity(intent);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.login_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class RegisteringUser extends AsyncTask<String, Void, Void> {

        private boolean existLogin = false;
        private boolean existEmail = false;
        private boolean passEquals = true;

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

            if (!data[1].equals(data[2])) {
                passEquals = false;
                return null;
            }

            try {
                statement = connection.createStatement();
                resultSet = statement.executeQuery(
                        "SELECT Users.[User].login, " +
                                "Users.[User].password," +
                                "Users.[User].email " +
                                "FROM Users.[User]");

                while (resultSet.next()) {
                    if (resultSet.getString(1).equals(data[0])) {
                        existLogin = true;
                        break;
                    } else if (resultSet.getString(3).equals(data[3])) {
                        existEmail = true;
                        break;
                    }
                }

                statement.execute("INSERT INTO Users.[User] VALUES ('" + data[1] + "', '"
                        + data[0] + "', '" + data[3] + "', '1111')");

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

            String message = "";

            if (existLogin) {
                message = "Login has already exist";
            } else if (!passEquals) {
                message = "Passwords are not equals";
            } else if (existEmail) {
                message = "Email has already used";
            }

            if (!message.equals("")) {
                Toast.makeText(
                        RegisterUserActivity.this,
                        message,
                        Toast.LENGTH_LONG
                ).show();
            } else {
                Intent intent = new Intent(RegisterUserActivity.this, LoginActivity.class);
                startActivity(intent);
            }

            RegisterUserActivity.this.registerProgressBar.setVisibility(ProgressBar.GONE);
        }
    }
}