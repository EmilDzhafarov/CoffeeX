package pc.emil.coffeex.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import pc.emil.coffeex.R;
import pc.emil.coffeex.activities.CoffeeShopActivity;
import pc.emil.coffeex.activities.MainActivity;
import pc.emil.coffeex.activities.ShowPieceOfNewsActivity;
import pc.emil.coffeex.models.PieceOfNews;

public class NewsAdapter extends BaseAdapter {
    private Context context;
    private PieceOfNews[] data;
    private static LayoutInflater inflater = null;

    public NewsAdapter(Context context, PieceOfNews[] data) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (vi == null) {
            vi = inflater.inflate(R.layout.piece_of_news, null);
        }

        final TextView text = (TextView) vi.findViewById(R.id.news_text);
        TextView dateTime = (TextView) vi.findViewById(R.id.news_date_time);
        TextView title = (TextView) vi.findViewById(R.id.news_title);
        Button btn = (Button) vi.findViewById(R.id.see_all_btn);

        text.setText(data[position].getText());
        dateTime.setText(data[position].getDateTime());
        title.setText(data[position].getTitle());
        btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(context, ShowPieceOfNewsActivity.class);
                        i.putExtra("piece_of_news", data[position]);
                        context.startActivity(i);
                    }
                }
        );

        return vi;
    }
}
