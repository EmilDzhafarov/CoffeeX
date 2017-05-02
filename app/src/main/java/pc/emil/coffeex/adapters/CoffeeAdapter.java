package pc.emil.coffeex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Locale;

import pc.emil.coffeex.R;
import pc.emil.coffeex.models.Coffee;

public class CoffeeAdapter extends BaseAdapter {

    private Context context;
    private Coffee[] data;
    private static LayoutInflater inflater = null;

    public CoffeeAdapter(Context context, Coffee[] data) {
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
            vi = inflater.inflate(R.layout.coffee, null);
        }

        TextView name = (TextView) vi.findViewById(R.id.coffee_name);
        TextView price = (TextView) vi.findViewById(R.id.coffee_price);
        TextView description = (TextView) vi.findViewById(R.id.coffee_description);

        name.setText(data[position].getName());
        price.setText(String.format(Locale.ENGLISH, "%.2f", data[position].getPrice()));
        description.setText(data[position].getDescription());

        return vi;
    }
}
