package pc.emil.coffeex.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import pc.emil.coffeex.R;
import pc.emil.coffeex.models.Subscription;

public class SubscriptionInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_info);

        TextView textView = (TextView) findViewById(R.id.description_of_subscription);
        Subscription subscription = (Subscription) getIntent().getExtras().getSerializable("subscription_info");
        textView.setText(subscription.getDescription());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
