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
import android.widget.AdapterView;
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
import pc.emil.coffeex.adapters.CoffeeShopAdapter;
import pc.emil.coffeex.adapters.NewsAdapter;
import pc.emil.coffeex.models.Coffee;
import pc.emil.coffeex.models.CoffeeShop;
import pc.emil.coffeex.models.PieceOfNews;
import pc.emil.coffeex.models.User;

import static pc.emil.coffeex.activities.LoginActivity.SAVED_EMAIL;
import static pc.emil.coffeex.activities.LoginActivity.SAVED_ID;
import static pc.emil.coffeex.activities.LoginActivity.SAVED_LOGIN;
import static pc.emil.coffeex.activities.LoginActivity.SAVED_PASSWORD;
import static pc.emil.coffeex.activities.LoginActivity.globalUser;

public class NewsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private ListView listView;
    private ArrayList<PieceOfNews> news = new ArrayList<>();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        listView = (ListView) findViewById(R.id.lvNews);
        progressBar = (ProgressBar) findViewById(R.id.news_progressBar);

        final AsyncRetrieveNews asyncRetrieveData = new AsyncRetrieveNews();
        new Thread(new Runnable() {
            @Override
            public void run() {
                asyncRetrieveData.execute();
            }
        }).start();
        progressBar.setVisibility(ProgressBar.VISIBLE);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        TextView userLogin = (TextView) headerView.findViewById(R.id.user_name);
        TextView userEmail = (TextView) headerView.findViewById(R.id.user_email);

        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
        int id = sPref.getInt(SAVED_ID, -1);
        String login = sPref.getString(SAVED_LOGIN, "");
        String pass = sPref.getString(SAVED_PASSWORD, "");
        String email = sPref.getString(SAVED_EMAIL, "");

        globalUser = new User(id, login, pass, email);

        userLogin.setText(login);
        userEmail.setText(email);

        Toolbar toolbar = (Toolbar) findViewById(R.id.news_toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.news_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        if (!login.equals("") &&!email.equals("")) {
            Menu menu = navigationView.getMenu();
            MenuItem item = menu.findItem(R.id.sign_in_item);
            item.setTitle(getResources().getString(R.string.sign_out));
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.news_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private class AsyncRetrieveNews extends AsyncTask<Void, Void, Void> {

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
        protected Void doInBackground(Void... voids) {
            if (connection != null) {
                try {
                    statement = connection.createStatement();
                    resultSet = statement.executeQuery(
                            "SELECT dbo.News.id," +
                                    "dbo.News.title," +
                                    "dbo.News.date_time, " +
                                    "dbo.News.text " +
                                    "FROM dbo.News ORDER BY date_time DESC");

                    while (resultSet.next()) {
                        PieceOfNews n = new PieceOfNews(
                                resultSet.getInt(1),
                                resultSet.getString(2),
                                resultSet.getString(3).split("\\.")[0],
                                resultSet.getString(4)
                        );

                        news.add(n);
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
                         NewsActivity.this,
                        getResources().getString(R.string.check_internet),
                        Toast.LENGTH_LONG
                ).show();
            } else {
                listView.setAdapter(new NewsAdapter(NewsActivity.this,
                        news.toArray(new PieceOfNews[news.size()])));
            }
        }
    }
}
