package pc.emil.coffeex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import pc.emil.coffeex.R;
import pc.emil.coffeex.models.Comment;


public class CommentsAdapter extends BaseAdapter {
    private Context context;
    private Comment[] data;
    private static LayoutInflater inflater = null;

    public CommentsAdapter(Context context, Comment[] data) {
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
            vi = inflater.inflate(R.layout.comment, null);
        }

        TextView text = (TextView) vi.findViewById(R.id.comment_text);
        TextView dateTime = (TextView) vi.findViewById(R.id.comment_date_time);
        TextView user = (TextView) vi.findViewById(R.id.comment_user);

        text.setText(data[position].getText());
        dateTime.setText(data[position].getDateTime());
        user.setText(data[position].getUserLogin());

        return vi;
    }
}
