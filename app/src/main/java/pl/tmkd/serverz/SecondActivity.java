package pl.tmkd.serverz;

import static android.widget.Toast.LENGTH_SHORT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.io.Serializable;
import java.util.ArrayList;

import pl.tmkd.serverz.sq.RefreshType;
import pl.tmkd.serverz.sq.Server;
import pl.tmkd.serverz.sq.ServerListener;

public class SecondActivity extends Activity implements ServerListener, Serializable {
    private View view;
    private TextView textView;
    private ProgressBar progressBar;
    Server server;
    ArrayAdapter<String> itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.active_server, null);
        setContentView(view);
        progressBar = findViewById(R.id.progressBar);
        Intent intent = getIntent();
        String ip = intent.getStringExtra("ip");
        int port = intent.getIntExtra("port", 0);

        createPlayersAdapter();
        server = new Server(ip, port, RefreshType.FULL);
        server.setListener(this);
    }

    public void createPlayersAdapter() {
        ArrayList<String> items = new ArrayList<>();

        itemsAdapter = new ArrayAdapter<String>(this, R.layout.player_item, items);
        ListView listView = findViewById(R.id.listOfPlayers);
        listView.setAdapter(itemsAdapter);
    }

    @SuppressLint("SetTextI18n")
    public void updateServerInfo() {
        TextView name = findViewById(R.id.serverName);
        TextView amountOfPlayers = findViewById(R.id.amountOfPlayers);
        TextView serverTime = findViewById(R.id.serverTime);
        TextView dayDuration = findViewById(R.id.dayDuration);
        TextView nightDuration = findViewById(R.id.nightDuration);
        TextView durationTillSunriseOrSunset = findViewById(R.id.durationTillSunriseOrSunset);
        progressBar.findViewById(R.id.progressBar);

        name.setText(server.getName());
        amountOfPlayers.setText((server.getPlayersNum()) + "/" + (server.getMaxPlayers()));
        serverTime.setText(server.getServerTime());
        dayDuration.setText(server.getDayDuration());
        nightDuration.setText(server.getNightDuration());
        durationTillSunriseOrSunset.setText(server.getDurationTillSunriseOrSunset());
        progressBar.setProgress(server.getDayOrNightProgress());
    }

    @Override
    public void onServerInfoRefreshed(Server server2) {
        runOnUiThread(() -> {
            updateServerInfo();
            itemsAdapter.clear();
            itemsAdapter.addAll(server.getPlayers());
        });
    }

    @Override
    public void onServerInfoRefreshFailed(Server server) {
        String text = "Refresh failed!";
        runOnUiThread(()-> {
            Toast.makeText(itemsAdapter.getContext(), text, LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        server.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        server.stop();
    }
}
