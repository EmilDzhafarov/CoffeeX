package pc.emil.coffeex.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
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
import java.util.Set;
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

    private ProgressBar registerProgressBar;
    private TextInputLayout emailInputLayout;
    private TextInputLayout loginInputLayout;
    private TextInputLayout passInputLayout;
    private TextInputLayout checkPassInputLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        Toolbar toolbar = (Toolbar) findViewById(R.id.register_user_toolbar);
        setSupportActionBar(toolbar);

        registerProgressBar = (ProgressBar) findViewById(R.id.register_progressBar);
        registerProgressBar.setIndeterminate(false);
        registerProgressBar.setVisibility(ProgressBar.INVISIBLE);

        emailInputLayout = (TextInputLayout) findViewById(R.id.email_inputLayout);
        loginInputLayout = (TextInputLayout) findViewById(R.id.login_inputLayout);
        passInputLayout = (TextInputLayout) findViewById(R.id.pass_inputLayout);
        checkPassInputLayout = (TextInputLayout) findViewById(R.id.check_pass_inputLayout);

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

        if (!login.equals("") && !email.equals("")) {
            Menu menu = navigationView.getMenu();
            MenuItem item = menu.findItem(R.id.sign_in_item);
            item.setTitle(getResources().getString(R.string.sign_out));
        }
    }

    public static int validateData(String login, String password, String password2, String email) {
        String loginRegExp = "^[a-zA-Z0-9]+([_ -]?[a-zA-Z0-9])*$";
        String emailRegExp = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";

        Pattern loginPattern = Pattern.compile(loginRegExp);
        Pattern emailPattern = Pattern.compile(emailRegExp, Pattern.CASE_INSENSITIVE);

        if (login.isEmpty()) {
            return 1;
        } else if (!loginPattern.matcher(login).matches()) {
            return 2;
        } else if (password.isEmpty()) {
            return 3;
        } else if (!loginPattern.matcher(password).matches()) {
            return 4;
        } else if (password.length() < 6) {
            return 5;
        } else if (!password.equals(password2)) {
            return 6;
        } else if (email.isEmpty()) {
            return 7;
        } else if (!emailPattern.matcher(email).matches()) {
            return 8;
        } else {
            return 0;
        }
    }

    @Override
    public void onClick(View view) {
        final String login = loginInputLayout.getEditText().getText().toString();
        final String password = passInputLayout.getEditText().getText().toString();
        final String password2 = checkPassInputLayout.getEditText().getText().toString();
        final String email = emailInputLayout.getEditText().getText().toString();
        int resultCode = validateData(login, password, password2, email);

        if (resultCode == 0) {
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            RegisteringUser check = new RegisteringUser();
                            check.execute(login, password, password2, email);
                        }
                    }
            ).start();
            registerProgressBar.setVisibility(View.VISIBLE);
        } else {
            switch (resultCode) {
                case 1:
                    loginInputLayout.setError(getResources().getString(R.string.empty_login));
                    emailInputLayout.setError(null);
                    passInputLayout.setError(null);
                    checkPassInputLayout.setError(null);
                    break;
                case 2:
                    loginInputLayout.setError(getResources().getString(R.string.login_should));
                    emailInputLayout.setError(null);
                    passInputLayout.setError(null);
                    checkPassInputLayout.setError(null);
                    break;
                case 3:
                    loginInputLayout.setError(null);
                    emailInputLayout.setError(null);
                    passInputLayout.setError(getResources().getString(R.string.empty_pass));
                    checkPassInputLayout.setError(null);
                    break;
                case 4:
                    loginInputLayout.setError(null);
                    emailInputLayout.setError(null);
                    passInputLayout.setError(getResources().getString(R.string.pass_should));
                    checkPassInputLayout.setError(null);
                    break;
                case 5:
                    checkPassInputLayout.setError(null);
                    emailInputLayout.setError(null);
                    passInputLayout.setError(getResources().getString(R.string.pass_made_up));
                    loginInputLayout.setError(null);
                    break;
                case 6:
                    emailInputLayout.setError(null);
                    loginInputLayout.setError(null);
                    passInputLayout.setError(null);
                    checkPassInputLayout.setError(getResources().getString(R.string.pass_not_eq));
                    break;
                case 7:
                    emailInputLayout.setError(getResources().getString(R.string.empty_email));
                    loginInputLayout.setError(null);
                    passInputLayout.setError(null);
                    checkPassInputLayout.setError(null);
                    break;
                case 8:
                    loginInputLayout.setError(null);
                    emailInputLayout.setError(getResources().getString(R.string.email_fail));
                    passInputLayout.setError(null);
                    checkPassInputLayout.setError(null);
                    break;
                default:
                    loginInputLayout.setError(null);
                    emailInputLayout.setError(null);
                    passInputLayout.setError(null);
                    checkPassInputLayout.setError(null);
            }
        }
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
            if (item.getTitle().equals(getResources().getString(R.string.sign_out))) {
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
            Class dest = SettingsActivity.class;
            if (this.getClass() != dest) {
                Intent intent = new Intent(this, dest);
                startActivity(intent);
            }
        } else if (id == R.id.nav_coffee_shops) {
            Class dest = MainActivity.class;
            if (this.getClass() != dest) {
                Intent intent = new Intent(this, dest);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        } else if (id == R.id.nav_news) {
            Class dest = NewsActivity.class;
            if (this.getClass() != dest) {
                Intent intent = new Intent(this, dest);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.register_user_drawer_layout);
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
                errorCode = -5;
                Log.e("Error", "Error message", e);
            }
        }

        @Override
        protected Void doInBackground(String... data) {
            if (connection != null) {
                try {
                    statement = connection.createStatement();
                    resultSet = statement.executeQuery(
                            "SELECT Users.[User].login, " +
                                    "Users.[User].password," +
                                    "Users.[User].email " +
                                    "FROM Users.[User]");

                    while (resultSet.next()) {
                        if (resultSet.getString(1).equals(data[0])) {
                            errorCode = 9;
                            return null;
                        } else if (resultSet.getString(3).equals(data[3])) {
                            errorCode = 10;
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

            RegisterUserActivity.this.registerProgressBar.setVisibility(ProgressBar.GONE);

            if (errorCode != 0) {
                switch (errorCode) {
                    case -5:
                        Toast.makeText(
                                RegisterUserActivity.this,
                                getResources().getString(R.string.check_internet),
                                Toast.LENGTH_LONG
                        ).show();
                        break;
                    case 9:
                        emailInputLayout.setError(null);
                        loginInputLayout.setError(getResources().getString(R.string.login_used));
                        passInputLayout.setError(null);
                        checkPassInputLayout.setError(null);
                        break;
                    case 10:
                        emailInputLayout.setError(getResources().getString(R.string.email_used));
                        loginInputLayout.setError(null);
                        passInputLayout.setError(null);
                        checkPassInputLayout.setError(null);
                        break;
                }
            } else {
                loginInputLayout.setError(null);
                emailInputLayout.setError(null);
                passInputLayout.setError(null);
                checkPassInputLayout.setError(null);

                Intent intent = new Intent(RegisterUserActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }
    }
}
