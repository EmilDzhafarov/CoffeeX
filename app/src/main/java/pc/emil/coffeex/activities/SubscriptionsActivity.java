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
import android.widget.AdapterView;
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
import pc.emil.coffeex.adapters.SubscriptionAdapter;
import pc.emil.coffeex.models.Subscription;

public class SubscriptionsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private ListView listView;
    private ArrayList<Subscription> subscriptions = new ArrayList<>();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscriptions);

        listView = (ListView) findViewById(R.id.subscriptions_list);
        progressBar = (ProgressBar) findViewById(R.id.subscriptions_progressBar);

        final AsyncRetrieveData asyncRetrieveData = new AsyncRetrieveData();

        new Thread(new Runnable() {
            @Override
            public void run() {
                asyncRetrieveData.execute();
            }
        }).start();
        progressBar.setVisibility(ProgressBar.VISIBLE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.subscriptions_toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.subscriptions_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.subscriptions_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }  else {
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.subscriptions_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class AsyncRetrieveData extends AsyncTask<Void, Void, Void> {

        private Connection connection = null;
        private Statement statement = null;
        private ResultSet resultSet = null;

        private String password = "ROOTroot123;";
        private String userName = "Coffee@coffeenure;";
        private String dbName = "Coffee;";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

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
        protected Void doInBackground(Void... voids) {
            try {
                statement = connection.createStatement();
                resultSet = statement.executeQuery(
                        "SELECT Users.[Subscription_type].title," +
                                "Users.[Subscription_type].Duration," +
                                "Users.[Subscription_type].price " +
                                "FROM Users.[Subscription_type]");

                while (resultSet.next()) {
                    Subscription sub = new Subscription(resultSet.getString(1),
                            resultSet.getInt(2),
                            resultSet.getDouble(3));
                    subscriptions.add(sub);
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

            listView.setAdapter(new SubscriptionAdapter(SubscriptionsActivity.this,
                    subscriptions.toArray(new Subscription[subscriptions.size()])));
            SubscriptionsActivity.this.progressBar.setVisibility(ProgressBar.GONE);
        }
    }
}
