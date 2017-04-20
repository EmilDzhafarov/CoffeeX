package pc.emil.coffeex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import pc.emil.coffeex.R;
import pc.emil.coffeex.models.CoffeeShop;
import pc.emil.coffeex.models.Subscription;

public class SubscriptionAdapter extends BaseAdapter {
    private Context context;
    private Subscription[] data;
    private static LayoutInflater inflater = null;

    public SubscriptionAdapter(Context context, Subscription[] data) {
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (vi == null) {
            vi = inflater.inflate(R.layout.subscription, null);
        }

        TextView title = (TextView) vi.findViewById(R.id.subscription_title);
        TextView duration = (TextView) vi.findViewById(R.id.subscription_duration);
        TextView price = (TextView) vi.findViewById(R.id.subscription_price);
        Button buyBtn = (Button) vi.findViewById(R.id.buy_button);

        title.setText(data[position].getTitle());
        duration.setText(String.valueOf(data[position].getDuration()));
        price.setText(String.valueOf(data[position].getPrice()));

        buyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return vi;
    }
}
