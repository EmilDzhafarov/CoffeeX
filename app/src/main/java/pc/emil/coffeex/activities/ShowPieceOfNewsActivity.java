package pc.emil.coffeex.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import pc.emil.coffeex.R;
import pc.emil.coffeex.models.PieceOfNews;

public class ShowPieceOfNewsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_piece_of_news);

        TextView title = (TextView) findViewById(R.id.title_of_new);
        TextView body = (TextView) findViewById(R.id.text_of_news);
        TextView date = (TextView) findViewById(R.id.date_time_of_news);

        PieceOfNews n = (PieceOfNews) getIntent().getExtras().getSerializable("piece_of_news");

        title.setText(n.getTitle());
        body.setText(n.getText());
        date.setText(n.getDateTime());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
