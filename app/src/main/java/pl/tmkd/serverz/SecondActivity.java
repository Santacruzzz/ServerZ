package pl.tmkd.serverz;

import static pl.tmkd.serverz.sq.Constants.SQ_TAG;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import pl.tmkd.serverz.sq.Server;
import pl.tmkd.serverz.sq.ServerListener;
import pl.tmkd.serverz.sq.msg.Player;

public class SecondActivity extends Activity implements ServerListener {
    private View view;
    private TextView textView;
    Server server;
    ArrayAdapter<String> itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.active_server, null);
        setContentView(view);
        Intent intent = getIntent();
        String ip = intent.getStringExtra("ip");
        int port = intent.getIntExtra("port", 0);

        createPlayersAdapter();
        server = new Server(ip, port);
        server.setListener(this);
        server.start();
    }
    public void createPlayersAdapter() {
        ArrayList<String> items = new ArrayList<>();

        itemsAdapter =
                new ArrayAdapter<String>(this, R.layout.player_item, items);
        ListView listView = findViewById(R.id.listOfPlayers);
        listView.setAdapter(itemsAdapter);
    }

    public void updateServerInfo() {
        TextView name = findViewById(R.id.serverName);
        TextView amountOfPlayers = findViewById(R.id.amountOfPlayers);
        TextView serverTime = findViewById(R.id.serverTime);
        TextView dayDuration = findViewById(R.id.dayDuration);
        TextView nightDuration = findViewById(R.id.nightDuration);

        name.setText(server.getName());
        amountOfPlayers.setText(String.valueOf(server.getPlayersNum()));
        serverTime.setText(server.getServerTime());
        dayDuration.setText(server.getDayDuration());
        nightDuration.setText(server.getNightDuration());

    }

    @Override
    public void onServerInfoRefreshed(Server server2) {
        updateServerInfo();
        ArrayList<String> items = new ArrayList<>();
        for (Player player : server.getPlayers()) {
            items.add(player.getPlaytime());
        }
        itemsAdapter.addAll(items);
    }
}
