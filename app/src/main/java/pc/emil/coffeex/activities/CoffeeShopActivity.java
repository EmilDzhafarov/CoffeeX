package pc.emil.coffeex.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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

public class CoffeeShopActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {


    private LatLng getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);

            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (IOException ex) {
            Log.e("Error", ex.getMessage(), ex);
        }

        return p1;
    }

    private View.OnClickListener intentButtons = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();

            Intent result;

            switch (id) {
                case R.id.call_button:
                    result = new Intent(Intent.ACTION_DIAL);
                    result.setData(Uri.parse("tel:" + shop.getPhone()));
                    break;
                case R.id.map_button:
                    result = new Intent(Intent.ACTION_VIEW);
                    LatLng location = getLocationFromAddress(shop.getAddress());
                    String data = String.format("geo:0,0?z=10&q=%s,%s(%s)",
                            location.latitude, location.longitude, shop.getName());
                    result.setData(Uri.parse(data));
                    break;
                case R.id.view_site_button:
                    result = new Intent(Intent.ACTION_VIEW);
                    result.setData(Uri.parse(!shop.getSite().startsWith("http://", 0)
                            ? "http://" + shop.getSite() : shop.getSite()));
                    break;
                case R.id.show_coffee_button:
                    result = new Intent(CoffeeShopActivity.this, CoffeeActivity.class);
                    result.putExtra("coffee_shop", shop);
                    break;
                case R.id.show_comments:
                    result = new Intent(CoffeeShopActivity.this, CoffeeShopCommentsActivity.class);
                    result.putExtra("coffee_shop", shop);
                    break;
                default:
                    return;
            }

            startActivity(result);
        }
    };

    private CoffeeShop shop = null;
    private float startRating;
    private RatingBar ratingBar;
    private ProgressBar progressBar;
    private ProgressBar imageProgressBar;
    private ImageView imageView;

    @Override
    public void onClick(View view) {
        String url;

        switch (view.getId()) {
            case R.id.coffee_shop_fb:
                url = shop.getFb();
                break;
            case R.id.coffee_shop_vk:
                url = shop.getVk();
                break;
            case R.id.coffee_shop_insta:
                url = shop.getInstagram();
                break;
            default:
                url = "";
        }

        if (!url.equals("")) {

            if (!url.contains("http://")) {
                url = "http://" + url;
            }

            Uri webpage = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffee_shop);

        Button callButton = (Button) findViewById(R.id.call_button);
        Button mapButton = (Button) findViewById(R.id.map_button);
        Button openSiteButton = (Button) findViewById(R.id.view_site_button);
        Button showCoffeeButton = (Button) findViewById(R.id.show_coffee_button);
        Button showComments = (Button) findViewById(R.id.show_comments);

        callButton.setOnClickListener(intentButtons);
        mapButton.setOnClickListener(intentButtons);
        openSiteButton.setOnClickListener(intentButtons);
        showCoffeeButton.setOnClickListener(intentButtons);
        showComments.setOnClickListener(intentButtons);

        progressBar = (ProgressBar) findViewById(R.id.coffee_progressBar);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        imageProgressBar = (ProgressBar) findViewById(R.id.image_progressBar);

        imageView = (ImageView) findViewById(R.id.coffee_shop_imageView);

        Toolbar toolbar = (Toolbar) findViewById(R.id.coffee_shop_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.coffee_drawer_layout);
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

        Bundle extras = getIntent().getExtras();
        shop = (CoffeeShop) extras.getSerializable("coffee_shop");

        TextView address = (TextView) findViewById(R.id.coffee_shop_address);
        TextView phone = (TextView) findViewById(R.id.coffee_shop_phone);
        TextView site = (TextView) findViewById(R.id.coffee_shop_site);
        ratingBar = (RatingBar) findViewById(R.id.coffee_shop_ratingBar);

        ImageButton fbButton = (ImageButton) findViewById(R.id.coffee_shop_fb);
        ImageButton vkButton = (ImageButton) findViewById(R.id.coffee_shop_vk);
        ImageButton instaButton = (ImageButton) findViewById(R.id.coffee_shop_insta);

        if (shop != null) {
            address.setText(shop.getAddress());
            phone.setText(shop.getPhone());
            setTitle(shop.getName());
            site.setText(shop.getSite());

            fbButton.setOnClickListener(this);
            vkButton.setOnClickListener(this);
            instaButton.setOnClickListener(this);

            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            new LoadRating().execute();
                        }
                    }
            ).start();

            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            new LoadImage().execute(shop.getId());
                        }
                    }
            ).start();

            startRating = ratingBar.getRating();
            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, final float v, boolean b) {
                    if (globalUser.getId() == -1) {
                        ratingBar.setProgress(0);
                        Toast.makeText(CoffeeShopActivity.this,
                                "You should be signed in!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        ratingBar.setRating(v);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                new ChangingRating().execute(v);
                            }
                        }).start();
                    }
                }
            });
        }

        if (!login.equals("") && !email.equals("")) {
            Menu menu = navigationView.getMenu();
            MenuItem item = menu.findItem(R.id.sign_in_item);
            item.setTitle("Sign out");
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.coffee_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (startRating != ratingBar.getRating() && startRating != 0) {
            super.onBackPressed();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
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
                startActivity(intent);
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
                startActivity(intent);
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
                startActivity(intent);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.coffee_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class LoadImage extends AsyncTask<Integer, Void, Void> {
        private Connection connection = null;
        private Statement statement = null;
        private ResultSet resultSet = null;

        private String password = "ROOTroot123;";
        private String userName = "Coffee@coffeenure;";
        private String dbName = "Coffee;";

        private int errorCode = 0;
        private byte[] imageData;

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
                            "SELECT pic FROM dbo.Coffee_shop " +
                                    "WHERE id = " + data[0]
                    );

                    while (resultSet.next()) {
                        imageData = resultSet.getBytes(1);
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

            imageProgressBar.setVisibility(ProgressBar.GONE);

            if (errorCode == -5) {
                Toast.makeText(
                        CoffeeShopActivity.this,
                        "Check your Internet connection and try again",
                        Toast.LENGTH_LONG
                ).show();
            } else {
                imageView.setImageBitmap(
                        BitmapFactory.decodeStream(
                                new ByteArrayInputStream(imageData)
                        )
                );
            }
        }
    }

    private class LoadRating extends AsyncTask<Void, Void, Void> {
        private Connection connection = null;
        private Statement statement = null;
        private ResultSet resultSet = null;

        private String password = "ROOTroot123;";
        private String userName = "Coffee@coffeenure;";
        private String dbName = "Coffee;";

        private float rating;
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
        protected Void doInBackground(Void... data) {
            if (connection != null && globalUser.getId() != -1) {
                try {
                    statement = connection.createStatement();
                    resultSet = statement.executeQuery(
                            "SELECT dbo.Coffee_rating.rating FROM dbo.Coffee_rating " +
                                    "WHERE dbo.Coffee_rating.user_id = " + globalUser.getId() +
                                    " AND dbo.Coffee_rating.coffee_shop_id = " + shop.getId()
                    );

                    while (resultSet.next()) {
                        rating = resultSet.getFloat(1);
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
                        CoffeeShopActivity.this,
                        "Check your Internet connection and try again",
                        Toast.LENGTH_LONG
                ).show();
            } else {
                ratingBar.setRating(rating);
            }
        }
    }

    private class ChangingRating extends AsyncTask<Float, Void, Void> {

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
        protected Void doInBackground(Float... data) {
            if (connection != null) {
                try {
                    statement = connection.createStatement();
                    statement.execute("INSERT INTO dbo.Coffee_rating(" +
                            "coffee_shop_id, user_id, rating)" +
                            " VALUES (" + shop.getId() + ", " +
                            globalUser.getId() + ", " +
                            data[0] + ");");

                } catch (SQLException e) {
                    try {
                        statement.execute("UPDATE dbo.Coffee_rating SET dbo.Coffee_rating.rating = " + data[0] +
                                " WHERE dbo.Coffee_rating.coffee_shop_id = " + shop.getId() +
                                " AND dbo.Coffee_rating.user_id = " + globalUser.getId());
                    } catch (SQLException e1) {
                        Log.e("Error", "Error Message: ", e);
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

            if (errorCode == -5) {
                Toast.makeText(
                        CoffeeShopActivity.this,
                        "Check your Internet connection and try again",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }
}
