package test.boxtest;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MainActivity self;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        self = this;

        BoxClient.SignOut(this);

        new BoxClient(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView view = (TextView) findViewById(R.id.output);
        view.setText("Press the sync button to get started");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                doCloudSync();

            }
        });
    };

    private void doCloudSync()
    {
        Toast.makeText(this, "Download from Box started...", Toast.LENGTH_LONG).show();
        new CloudSynchronizer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public class CloudSynchronizer extends AsyncTask<Void,String, Integer>
    {
        private int id;

        @Override
        protected Integer doInBackground(Void... params) {

            BoxClient client = new BoxClient(self);
            ArrayList<BoxItemInfo> items = client.GetItemList();
            for (int x = 0; x < items.size(); x++) {
                JSONObject json = client.getData(items.get(x));
                publishProgress("Downloaded item " + (x + 1) + " of " + items.size() + ". (File ID : " + items.get(x).getId() + ")");

                // todo: line of business stuff with this JSON data
            }
            return items.size();
        }


        @Override
        protected void onProgressUpdate(String... values)
        {
            super.onProgressUpdate(values);

            TextView view = (TextView) findViewById(R.id.output);
            view.setText(values[0]);
        }


        @Override
        protected void onPostExecute(Integer status) {

        }
    }


}
