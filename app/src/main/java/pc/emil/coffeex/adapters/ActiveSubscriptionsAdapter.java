package pc.emil.coffeex.adapters;

import android.content.Context;
import android.renderscript.Float2;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Locale;

import pc.emil.coffeex.R;
import pc.emil.coffeex.models.ActiveSubscription;

public class ActiveSubscriptionsAdapter extends BaseAdapter {
    private Context context;
    private ActiveSubscription[] data;
    private static LayoutInflater inflater = null;

    public ActiveSubscriptionsAdapter(Context context, ActiveSubscription[] data) {
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
            vi = inflater.inflate(R.layout.active_subscription, null);
        }

        TextView title = (TextView) vi.findViewById(R.id.active_subscription_title);
        TextView price = (TextView) vi.findViewById(R.id.active_subscription_price);
        TextView untilDate = (TextView) vi.findViewById(R.id.active_subscription_until_date);

        title.setText(data[position].getTitle());
        price.setText(String.format(Locale.ENGLISH, "Price: %.2f",data[position].getPrice()));
        untilDate.setText(String.format(Locale.ENGLISH, "Active until: %s", data[position].getUntilDate()));

        return vi;
    }
}
