package pl.tmkd.serverz;

import static pl.tmkd.serverz.sq.Constants.TAG_MAIN;
import static pl.tmkd.serverz.sq.msg.Utils.saveServerDataInIntent;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.Serializable;

import pl.tmkd.serverz.sq.RefreshType;
import pl.tmkd.serverz.sq.Server;
import pl.tmkd.serverz.sq.ServerListener;

public class SecondActivity extends AppCompatActivity implements ServerListener, Serializable {
    private LinearProgressIndicator progressBar;
    Server server;
    public TabLayout tabLayout;
    private ViewPagerAdapter viewPagerAdapter;
    ViewPager2 myViewPager2;
    PlayerFragment playerFragment;
    ModFragment modFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View activeServerView = getLayoutInflater().inflate(R.layout.active_server, null);
        setContentView(activeServerView);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        progressBar = findViewById(R.id.progressBar);
        readServerDataFromIntent();
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        initFragments();
        viewPagerAdapter.add(playerFragment, "Players");
        viewPagerAdapter.add(modFragment, "Modes");
        myViewPager2 = findViewById(R.id.viewpager);
        myViewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        myViewPager2.setAdapter(viewPagerAdapter);
        tabLayout = findViewById(R.id.tab_layout);
        initTabLayout();
    }

    public void initFragments() {
        if (null == playerFragment) {
            playerFragment = new PlayerFragment(getBaseContext(), server.getPlayers());
        } else {
            playerFragment.setPlayers(server.getPlayers());
        }
        if (null == modFragment) {
            modFragment = new ModFragment(getBaseContext(), server.getMods());
        } else {
            modFragment.setMods(server.getMods());
        }
    }

    public void initTabLayout() {
        new TabLayoutMediator(tabLayout, myViewPager2, (tab, position) -> {
            if (position == 0) {
                tab.setText("PLAYERS");
            } else {
                tab.setText("MODS");
            }
        }).attach();
    }

    public void readServerDataFromIntent() {
        Intent intent = getIntent();
        String ip = intent.getStringExtra("ip");
        int port = intent.getIntExtra("port", 0);
        int sunriseTime = intent.getIntExtra("sunrise", 0);
        int sunsetTime = intent.getIntExtra("sunset", 0);
        String serverName = intent.getStringExtra("name");
        String address = intent.getStringExtra("address");
        int numberOfPlayers = intent.getIntExtra("amountOfPlayers", 0);
        int maxPlayers = intent.getIntExtra("maxPlayersNum", 0);
        String serverTime = intent.getStringExtra("serverTime");
        String dayDuration = intent.getStringExtra("dayDuration");
        String nightDuration = intent.getStringExtra("nightDuration");
        String durationTillSunriseOrSunset = intent.getStringExtra("durationTillSunriseOrSunset");
        Boolean isDayTime = intent.getBooleanExtra("isDay", true);
        int dayOrNightProgress = intent.getIntExtra("dayOrNightProgress", 0);
        boolean hasRefreshSucceeded = intent.getBooleanExtra("hasRefreshSucceeded", false);

        server = new Server(ip, port, RefreshType.FULL);
        server.setSunriseTime(sunriseTime);
        server.setSunsetTime(sunsetTime);
        showServerData(serverName, address, numberOfPlayers, maxPlayers, serverTime, dayDuration, nightDuration,
                durationTillSunriseOrSunset, isDayTime, dayOrNightProgress, hasRefreshSucceeded);
        server.setListener(this);
    }

    public void showServerData(String serverName, String address, int numberOfPlayers, int maxPlayers,
                               String sTime, String dDuration, String nDuration, String dSunriseOrSunset,
                               Boolean isDayTime, int dayOrNightProgress, boolean hasRefreshSucceeded) {
        TextView name = findViewById(R.id.serverName);
        TextView serverAddress = findViewById(R.id.serverAddress);
        TextView amountOfPlayers = findViewById(R.id.amountOfPlayers);
        TextView serverTime = findViewById(R.id.serverTime);
        TextView dayDuration = findViewById(R.id.dayDuration);
        TextView nightDuration = findViewById(R.id.nightDuration);
        TextView durationTillSunriseOrSunset = findViewById(R.id.durationTillSunriseOrSunset);
        TextView progress = findViewById(R.id.progress);

        name.setText(serverName);
        serverAddress.setText(address);
        amountOfPlayers.setText(numberOfPlayers + "/" + maxPlayers);
        serverTime.setText(sTime);
        dayDuration.setText(dDuration);
        nightDuration.setText(nDuration);
        durationTillSunriseOrSunset.setText(dSunriseOrSunset + " till");
        changeIconWhenIsDayOrNightTime(isDayTime, durationTillSunriseOrSunset);
        if (hasRefreshSucceeded) {
            progressBar.setIndeterminate(false);
            progressBar.setProgress(dayOrNightProgress);
            setProgressTextAndColor(progress, dayOrNightProgress);
        }
    }

    public void updateIntentData() {
        Intent intent = getIntent();
        saveServerDataInIntent(server, intent);
    }

    public void updatePlayersFragment(PlayerFragment newPlayersFragment) {
        this.playerFragment = newPlayersFragment;
    }

    public void updateModsFragment(ModFragment newModFragment) {
        this.modFragment = newModFragment;
    }

    public void updateServerData() {
        showServerData(server.getName(), server.getAddress(), server.getPlayersNum(), server.getMaxPlayers(),
                server.getServerTime(), server.getDayDuration(), server.getNightDuration(),
                server.getDurationTillSunriseOrSunset(), server.isDaytime(), server.getDayOrNightProgress(), server.hasRefreshSucceeded());
        progressBar.setIndeterminate(false);
    }

    public void setProgressTextAndColor(TextView progress, int dayOrNightProgress) {
        if (dayOrNightProgress < 50) {
            progress.setText(dayOrNightProgress + "%");
            progress.setTextColor(Color.WHITE);
        } else {
            progress.setText(dayOrNightProgress + "%");
            progress.setTextColor(Color.BLACK);
        }
    }

    public void changeIconWhenIsDayOrNightTime(Boolean isDayTime, TextView durationTillSunriseOrSunset) {
        Resources res = getResources();
        Drawable iconNight = ResourcesCompat.getDrawable(res, R.drawable.baseline_nightlight_24, null);
        Drawable iconDay = ResourcesCompat.getDrawable(res, R.drawable.baseline_sunny_24, null);
        if (isDayTime) {
            durationTillSunriseOrSunset.setCompoundDrawablesWithIntrinsicBounds(null, null, iconNight, null);
        } else {
            durationTillSunriseOrSunset.setCompoundDrawablesWithIntrinsicBounds(null, null, iconDay,null);
        }
    }

    @Override
    public void onServerInfoRefreshed() {
        runOnUiThread(() -> {
            updateServerData();
            playerFragment.update();
            modFragment.update();
        });
    }

    @Override
    public void onServerInfoRefreshFailed(Server server) {
        runOnUiThread(()-> {
            // TODO handling for unsuccessful refresh
            Log.e(TAG_MAIN, "Refresh failed!");
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
        updateIntentData();
        server.stop();
    }
}
