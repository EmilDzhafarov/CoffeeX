package pc.emil.coffeex.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Inet4Address;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pc.emil.coffeex.R;
import pc.emil.coffeex.models.User;

import static pc.emil.coffeex.activities.LoginActivity.SAVED_EMAIL;
import static pc.emil.coffeex.activities.LoginActivity.SAVED_ID;
import static pc.emil.coffeex.activities.LoginActivity.SAVED_LOGIN;
import static pc.emil.coffeex.activities.LoginActivity.SAVED_PASSWORD;
import static pc.emil.coffeex.activities.LoginActivity.globalUser;

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

        View headerView = navigationView.getHeaderView(0);
        TextView userLogin = (TextView) headerView.findViewById(R.id.user_name);
        TextView userEmail = (TextView) headerView.findViewById(R.id.user_email);

        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
        int id = sPref.getInt(SAVED_ID, -1);
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
            if (item.getTitle().equals("Sign out")) {
                SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor ed = sPref.edit();
                ed.putInt(SAVED_ID, -1);
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
                    startActivity(intent);
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

    private class RegisteringUser extends AsyncTask<String, Void, Void> {

        private int errorCode = 0;

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

            String loginRegExp = "^[a-zA-Z0-9]+([_ -]?[a-zA-Z0-9])*$";
            String emailRegExp = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";

            Pattern loginPattern = Pattern.compile(loginRegExp);
            Pattern emailPattern = Pattern.compile(emailRegExp, Pattern.CASE_INSENSITIVE);

            if (!loginPattern.matcher(data[0]).matches()) {
                errorCode = 1;
            } else if (!data[1].equals(data[2])) {
                errorCode = 2;
            } else if (!loginPattern.matcher(data[2]).matches()) {
                errorCode = 3;
            } else if (!emailPattern.matcher(data[3]).matches()) {
                errorCode = 4;
            }

            if (errorCode == 0) {
                try {
                    statement = connection.createStatement();
                    resultSet = statement.executeQuery(
                            "SELECT Users.[User].login, " +
                                    "Users.[User].password," +
                                    "Users.[User].email " +
                                    "FROM Users.[User]");

                    while (resultSet.next()) {
                        if (resultSet.getString(1).equals(data[0])) {
                            errorCode = 5;
                            return null;
                        } else if (resultSet.getString(3).equals(data[3])) {
                            errorCode = 6;
                            return null;
                        }
                    }

                    statement.execute("INSERT INTO Users.[User] VALUES ('" + data[1] + "', '"
                            + data[0] + "', '" + data[3] + "')");

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

            String message = "";

            switch (errorCode) {
                case 0:
                    break;
                case 1:
                    message = "Login contains invalid symbols";
                    break;
                case 2:
                    message = "Passwords are not equals";
                    break;
                case 3:
                    message = "Password should be consist from: a-z,A-Z,-,_,0-9";
                    break;
                case 4:
                    message = "Email consists from invalid symbols";
                    break;
                case 5:
                    message = "Login has already used";
                    break;
                case 6:
                    message = "Email has already used";
                    break;
            }

            if (!message.equals("")) {
                Toast.makeText(
                        RegisterUserActivity.this,
                        message,
                        Toast.LENGTH_LONG
                ).show();
            } else {
                Intent intent = new Intent(RegisterUserActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            RegisterUserActivity.this.registerProgressBar.setVisibility(ProgressBar.GONE);
        }
    }
}
