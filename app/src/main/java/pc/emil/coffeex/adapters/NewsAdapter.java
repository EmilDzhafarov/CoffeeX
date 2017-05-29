package pc.emil.coffeex.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.widget.DrawerLayout;
import android.text.style.TextAppearanceSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import pc.emil.coffeex.R;
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
    public View getView(final int position, final View convertView, ViewGroup parent) {
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        LinearLayout layout = new LinearLayout(context);
                        layout.setOrientation(LinearLayout.VERTICAL);

                        TextView title = new TextView(context);
                        title.setPadding(40,40,40,40);
                        title.setTextSize(24);
                        title.setText(data[position].getTitle());

                        View v = new View(context);
                        v.setLayoutParams(new LinearLayout.LayoutParams(
                                DrawerLayout.LayoutParams.MATCH_PARENT, 5
                        ));
                        v.setBackgroundColor(Color.parseColor("#B3B3B3"));

                        TextView text = new TextView(context);
                        text.setPadding(40,40,40,40);
                        text.setTextSize(20);
                        text.setText(data[position].getText());
                        layout.setLayoutParams(new DrawerLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));
                        layout.addView(title);
                        layout.addView(v);
                        layout.addView(text);

                        ScrollView scrollView = new ScrollView(context);
                        scrollView.addView(layout);
                        builder.setView(scrollView);
                        builder.setCancelable(true);
                        builder.show();
                    }
                }
        );

        return vi;
    }
}
