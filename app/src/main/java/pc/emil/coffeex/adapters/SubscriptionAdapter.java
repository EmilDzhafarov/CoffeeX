package pc.emil.coffeex.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import pc.emil.coffeex.R;
import pc.emil.coffeex.activities.SubscriptionsActivity;
import pc.emil.coffeex.models.Subscription;

import static pc.emil.coffeex.activities.LoginActivity.globalUser;

public class SubscriptionAdapter extends BaseAdapter {
    private Context context;
    private Subscription[] data;
    private static LayoutInflater inflater = null;
    private ProgressBar progressBar = SubscriptionsActivity.progressBar;

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
    public View getView(final int position, final View convertView, ViewGroup parent) {
        final View vi;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.subscription, null);
        } else {
            vi = convertView;
        }

        TextView title = (TextView) vi.findViewById(R.id.subscription_title);
        TextView duration = (TextView) vi.findViewById(R.id.subscription_duration);
        TextView price = (TextView) vi.findViewById(R.id.subscription_price);
        final Button buyBtn = (Button) vi.findViewById(R.id.buy_button);
        final Button infoBtn = (Button) vi.findViewById(R.id.details_button);

        title.setText(data[position].getTitle());
        duration.setText(String.valueOf(data[position].getDuration()));
        price.setText(String.format(Locale.ENGLISH, "%.2f", data[position].getPrice()));

        if (data[position].isBuyed()) {
            buyBtn.setEnabled(false);
        } else {
            buyBtn.setEnabled(true);
        }

        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                TextView title = new TextView(context);
                title.setPadding(40,40,40,40);
                title.setTextSize(24);
                title.setText(data[position].getTitle());

                TextView textView = new TextView(context);
                textView.setPadding(40,40,40,40);
                textView.setTextSize(20);
                textView.setText(data[position].getDescription());

                View v = new View(context);
                v.setLayoutParams(new LinearLayout.LayoutParams(
                        DrawerLayout.LayoutParams.MATCH_PARENT, 5
                ));
                v.setBackgroundColor(Color.parseColor("#B3B3B3"));

                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(title);
                layout.addView(v);
                layout.addView(textView);

                ScrollView scrollView = new ScrollView(context);
                scrollView.addView(layout);
                builder.setView(scrollView);
                builder.setCancelable(true);
                builder.show();
            }
        });

        buyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (globalUser.getId() == -1) {
                    Toast.makeText(
                            vi.getContext(),
                            vi.getResources().getString(R.string.should_sign_in),
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    new Thread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    new BuySubscription().execute(globalUser.getId(), data[position].getId());
                                }
                            }
                    ).start();
                    buyBtn.setEnabled(false);
                }
            }
        });

        return vi;
    }

    private class BuySubscription extends AsyncTask<Integer, Void, Void> {
        private Connection connection = null;
        private Statement statement = null;
        private ResultSet resultSet = null;

        private String password = "ROOTroot123;";
        private String userName = "Coffee@coffeenure;";
        private String dbName = "Coffee;";

        @Override
        protected void onPreExecute() {
            if (globalUser.getId() != - 1) {
                String connectionString =
                        "jdbc:jtds:sqlserver://coffeenure.database.windows.net:1433;"
                                + "databaseName=" + dbName
                                + "user=" + userName
                                + "password=" + password
                                + "encrypt=true;"
                                + "trustServerCertificate=false;"
                                + "hostNameInCertificate=*.database.windows.net;"
                                + "loginTimeout=30;";

                try {
                    Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
                    connection = DriverManager.getConnection(connectionString);
                } catch (InstantiationException | IllegalAccessException |
                        ClassNotFoundException | SQLException e) {
                    Log.e("Error", "Error message", e);
                }
            }
        }

        @Override
        protected Void doInBackground(Integer... data) {
            if (connection == null) {
                return null;
            } else {
                try {
                    statement = connection.createStatement();
                    statement.execute(
                            "INSERT INTO Users.[Subscription] (user_id, subscription_type_id, date_from) " +
                                    "VALUES(" + data[0] + ", " + data[1] + ", DATEADD(HOUR, 3, GETDATE())) "
                    );
                } catch (Exception e) {
                    Log.e("Error", "Error Message: ", e);
                }

                return null;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                if (connection != null) {
                    connection.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException ex) {
                Log.e("Error", "Error message", ex);
            }

            progressBar.setVisibility(ProgressBar.GONE);
        }
    }
}
