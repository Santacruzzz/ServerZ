package pl.tmkd.serverz;

import static pl.tmkd.serverz.sq.Constants.TAG_MAIN;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.Serializable;

import pl.tmkd.serverz.sq.RefreshType;
import pl.tmkd.serverz.sq.Server;
import pl.tmkd.serverz.sq.ServerListener;

public class SecondActivity extends AppCompatActivity implements ServerListener, Serializable {
    private View view;
    private TextView textView;
    private ProgressBar progressBar;
    Server server;
    public TabLayout tabLayout;
    private ViewPagerAdapter viewPagerAdapter;
    ViewPager2 myViewPager2;
    PlayerFragment playerFragment;
    ModFragment modFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.active_server, null);
        setContentView(view);

        Intent intent = getIntent();
        String ip = intent.getStringExtra("ip");
        int port = intent.getIntExtra("port", 0);
        server = new Server(ip, port, RefreshType.FULL);
        server.setListener(this);

        progressBar = findViewById(R.id.progressBar);
        playerFragment = new PlayerFragment(getBaseContext(), server.getPlayers());
        modFragment = new ModFragment(getBaseContext(), server.getMods());
        myViewPager2 = findViewById(R.id.viewpager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        viewPagerAdapter.add(playerFragment, "Players");
        viewPagerAdapter.add(modFragment, "Modes");

        tabLayout = findViewById(R.id.tab_layout);
        myViewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        myViewPager2.setAdapter(viewPagerAdapter);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, myViewPager2, (tab, position) -> {
            if (position == 0) {
                tab.setText("PLAYERS");
            } else {
                tab.setText("MODS");
            }
        }).attach();
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
    public void onServerInfoRefreshed() {
        runOnUiThread(() -> {
            updateServerInfo();
            playerFragment.update();
            modFragment.update();
        });
    }

    @Override
    public void onServerInfoRefreshFailed(Server server) {
        String text = "Refresh failed!";
        runOnUiThread(()-> {
//            Toast.makeText(itemsAdapter.getContext(), text, LENGTH_SHORT).show();
            Log.e(TAG_MAIN, text);
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
