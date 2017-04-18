package pc.emil.coffeex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import pc.emil.coffeex.R;
import pc.emil.coffeex.models.CoffeeShop;

public class CoffeeShopAdapter extends BaseAdapter {

    private Context context;
    private CoffeeShop[] data;
    private static LayoutInflater inflater = null;

    public CoffeeShopAdapter(Context context, CoffeeShop[] data) {
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
            vi = inflater.inflate(R.layout.coffee_shop, null);
        }

        TextView title = (TextView) vi.findViewById(R.id.title);
        TextView address = (TextView) vi.findViewById(R.id.address);
        TextView phoneNumber = (TextView) vi.findViewById(R.id.phone_number);

        title.setText(data[position].getName());
        address.setText(data[position].getAddress());
        phoneNumber.setText(data[position].getPhone());

        return vi;
    }
}
