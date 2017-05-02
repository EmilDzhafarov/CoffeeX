package pc.emil.coffeex.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;

import pc.emil.coffeex.R;
import pc.emil.coffeex.adapters.CoffeeAdapter;
import pc.emil.coffeex.adapters.CoffeeShopAdapter;
import pc.emil.coffeex.models.Coffee;
import pc.emil.coffeex.models.CoffeeShop;

public class CoffeeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffee);

        Bundle extras = getIntent().getExtras();
        CoffeeShop shop = (CoffeeShop) extras.getSerializable("coffee_shop");
        ListView listView = (ListView) findViewById(R.id.lvCoffees);
        listView.setAdapter(new CoffeeAdapter(this,
                shop.getCoffees().toArray(new Coffee[shop.getCoffees().size()])));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
