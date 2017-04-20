package pc.emil.coffeex.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pc.emil.coffeex.R;
import pc.emil.coffeex.adapters.CoffeeShopAdapter;
import pc.emil.coffeex.models.CoffeeShop;

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

        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        String login = sPref.getString(LoginActivity.SAVED_LOGIN, "");
        String pass = sPref.getString(LoginActivity.SAVED_PASSWORD, "");

        if (!login.equals("") && !pass.equals("")) {
            View header = navigationView.getHeaderView(0);
            TextView userName = (TextView) header.findViewById(R.id.user_name);
            userName.setText(login);
            Toast.makeText(this,"fvfd",Toast.LENGTH_LONG).show();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.search_badge) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Search Coffee shop");

            final EditText editText = new EditText(this);
            editText.setHint("Enter address or name");
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
                                        "Coffee shop not found\nTry again",
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
                    CoffeeShop shop = new CoffeeShop(
                            resultSet.getInt(1),
                            resultSet.getString(2),
                            resultSet.getString(3),
                            resultSet.getString(4),
                            resultSet.getString(5),
                            resultSet.getString(6),
                            resultSet.getString(7),
                            resultSet.getString(8),
                            resultSet.getString(9),
                            resultSet.getString(10),
                            resultSet.getString(11));
                    shops.add(shop);
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

            MainActivity.this.progressBar.setVisibility(ProgressBar.GONE);
        }
    }
}
