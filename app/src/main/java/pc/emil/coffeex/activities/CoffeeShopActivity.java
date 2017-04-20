package pc.emil.coffeex.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;

import pc.emil.coffeex.R;
import pc.emil.coffeex.models.CoffeeShop;

public class CoffeeShopActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private CoffeeShop shop = null;

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.coffee_shop_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.coffee_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Bundle extras = getIntent().getExtras();
        shop = (CoffeeShop) extras.getSerializable("coffee_shop");

        TextView title = (TextView) findViewById(R.id.coffee_shop_title);
        TextView address = (TextView) findViewById(R.id.coffee_shop_address);
        TextView phone = (TextView) findViewById(R.id.coffee_shop_phone);

        ImageButton fbButton = (ImageButton) findViewById(R.id.coffee_shop_fb);
        ImageButton vkButton = (ImageButton) findViewById(R.id.coffee_shop_vk);
        ImageButton instaButton = (ImageButton) findViewById(R.id.coffee_shop_insta);

        if (shop != null) {

            title.setText(shop.getName());
            address.setText(shop.getAddress());
            phone.setText(shop.getPhone());

            fbButton.setOnClickListener(this);
            vkButton.setOnClickListener(this);
            instaButton.setOnClickListener(this);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.coffee_drawer_layout);
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.coffee_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
