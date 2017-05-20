package pc.emil.coffeex.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pc.emil.coffeex.R;
import pc.emil.coffeex.adapters.CoffeeShopAdapter;
import pc.emil.coffeex.models.Coffee;
import pc.emil.coffeex.models.CoffeeShop;
import pc.emil.coffeex.models.User;

import static pc.emil.coffeex.activities.LoginActivity.SAVED_EMAIL;
import static pc.emil.coffeex.activities.LoginActivity.SAVED_ID;
import static pc.emil.coffeex.activities.LoginActivity.SAVED_LOGIN;
import static pc.emil.coffeex.activities.LoginActivity.SAVED_PASSWORD;
import static pc.emil.coffeex.activities.LoginActivity.globalUser;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private ListView listView;
    private ArrayList<CoffeeShop> shops = new ArrayList<>();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.ivMain);
        progressBar = (ProgressBar) findViewById(R.id.main_progressBar);
        final AsyncRetrieveData asyncRetrieveData = new AsyncRetrieveData();

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (listView.getCount() != shops.size()){
            listView.setAdapter(new CoffeeShopAdapter(this, shops.toArray(new CoffeeShop[shops.size()])));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Intent i = new Intent(MainActivity.this, CoffeeShopActivity.class);
                    i.putExtra("coffee_shop", shops.get(position));
                    startActivity(i);
                }
            });

        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.search_badge) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.search_title));

            final EditText editText = new EditText(this);
            editText.setHint(getResources().getString(R.string.search_hint));
            builder.setView(editText);
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final String query = editText.getText().toString().toLowerCase();

                            final ArrayList<CoffeeShop> result = new ArrayList<>();
                            for (CoffeeShop shop : shops) {
                                if (shop.getName().toLowerCase().contains(query)
                                        || shop.getAddress().toLowerCase().contains(query)) {
                                    result.add(shop);
                                }
                            }

                            if (result.size() == 0) {
                                Toast.makeText(MainActivity.this,
                                        getResources().getString(R.string.coffee_shop_not_found),
                                        Toast.LENGTH_LONG)
                                        .show();
                            } else {
                                listView.setAdapter(new CoffeeShopAdapter(MainActivity.this,
                                        result.toArray(new CoffeeShop[result.size()])));
                                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        Intent i = new Intent(MainActivity.this, CoffeeShopActivity.class);
                                        i.putExtra("coffee_shop", result.get(position));
                                        startActivity(i);
                                    }
                                });
                            }
                        }
                    });

            builder.setCancelable(true);
            builder.show();
        }

        return super.onOptionsItemSelected(item);
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRestart() {
        super.onRestart();
        shops.clear();
        listView.setAdapter(null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                new AsyncRetrieveData().execute();
            }
        }).start();
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    private class AsyncRetrieveData extends AsyncTask<Void, Void, Void> {

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
                            "SELECT dbo.Coffee_shop.id," +
                                    "dbo.Coffee_shop.Name," +
                                    "dbo.Coffee_shop.Phone, " +
                                    "dbo.Coffee_shop.Website, " +
                                    "dbo.Coffee_shop.Facebook," +
                                    "dbo.Coffee_shop.Instagram," +
                                    "dbo.Coffee_shop.Vkontakte," +
                                    "dbo.Address.Country, " +
                                    "dbo.Address.City, " +
                                    "dbo.Address.Street, " +
                                    "dbo.Address.Building " +
                                    "FROM dbo.Coffee_shop INNER JOIN dbo.Address " +
                                    "ON dbo.Coffee_shop.address_id=dbo.Address.id");

                    while (resultSet.next()) {
                        int id = resultSet.getInt(1);
                        ResultSet getCoffees = connection.createStatement().executeQuery(
                                "SELECT dbo.Coffee.Name," +
                                        "dbo.Coffee.Price," +
                                        "dbo.Coffee.Description " +
                                        "FROM dbo.Coffee " +
                                        "WHERE coffee_shop_id=" + id
                        );
                        ArrayList<Coffee> coffees = new ArrayList<>();

                        while (getCoffees.next()) {
                            Coffee coffee = new Coffee(
                                    getCoffees.getString(1),
                                    getCoffees.getDouble(2),
                                    getCoffees.getString(3)
                            );
                            coffees.add(coffee);
                        }

                        ResultSet getRating = connection.createStatement().executeQuery(
                                "SELECT dbo.Coffee_rating.rating FROM dbo.Coffee_rating " +
                                        "WHERE coffee_shop_id = " + id
                        );

                        float rating = 0;
                        int count= 0;

                        while (getRating.next()) {
                            rating += getRating.getFloat(1);
                            count++;
                        }

                        rating /= count;

                        CoffeeShop shop = new CoffeeShop(
                                id,
                                resultSet.getString(2),
                                resultSet.getString(3),
                                resultSet.getString(4),
                                resultSet.getString(5),
                                resultSet.getString(6),
                                resultSet.getString(7),
                                resultSet.getString(8),
                                resultSet.getString(9),
                                resultSet.getString(10),
                                resultSet.getString(11),
                                coffees,
                                rating);
                        shops.add(shop);
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
                        MainActivity.this,
                        getResources().getString(R.string.check_internet),
                        Toast.LENGTH_LONG
                ).show();
            } else {
                listView.setAdapter(new CoffeeShopAdapter(MainActivity.this,
                        shops.toArray(new CoffeeShop[shops.size()])));
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent i = new Intent(MainActivity.this, CoffeeShopActivity.class);
                        i.putExtra("coffee_shop", shops.get(position));
                        startActivity(i);
                    }
                });
            }
        }
    }
}
