package pl.tmkd.serverz.sq;

import static pl.tmkd.serverz.sq.Constants.SQ_TAG;
import static pl.tmkd.serverz.sq.Constants.SUNRISE_TIME;
import static pl.tmkd.serverz.sq.Constants.SUNSET_TIME;
import static pl.tmkd.serverz.sq.msg.Utils.formatDuration;

import android.util.Log;

import androidx.annotation.NonNull;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.tmkd.serverz.sq.msg.Player;
import pl.tmkd.serverz.sq.msg.ServerInfoResponse;
import pl.tmkd.serverz.sq.msg.ServerPlayersResponse;

public class Server implements SqResponseListener {
    private final String ip;
    private final int port;
    private final ExecutorService executor;
    private final SourceQueryTask refreshServerDataTask;
    private ServerListener listener;

    private String name;
    private String map;
    private String version;
    private int playersNum;
    private int maxPlayers;
    private String serverTime;
    private boolean isFirstPerson;
    private float dayTimeMult;
    private float nightTimeMult;
    private int queueSize;
    private String tillDayOrNightDuration;
    private String dayDuration;
    private String nightDuration;
    private boolean isDaytime;
    private Vector<Player> players;

    public Server(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.players = new Vector<Player>();
        refreshServerDataTask = new SourceQueryTask(ip, port, true);
        refreshServerDataTask.setListener(this);
        executor = Executors.newSingleThreadExecutor();
        refreshServerData();
    }

    public void setListener(ServerListener listener) {
        this.listener = listener;
    }

    private void refreshServerData() {
        executor.execute(refreshServerDataTask);
        executor.shutdown();
    }

    @Override
    public void onServerInfoResponse(ServerInfoResponse response) {
        Log.d(SQ_TAG, "onServerInfoResponse");
        store(response);
        if (null != listener)
            listener.onServerInfoRefreshed(this);
    }

    @Override
    public void onServerInfoAndPlayersResponse(ServerInfoResponse infoResponse, ServerPlayersResponse playersResponse) {
        Log.d(SQ_TAG, "onServerInfoAndPlayersResponse");
        store(infoResponse);
        store(playersResponse);
        if (null != listener)
            listener.onServerInfoRefreshed(this);
    }

    private void store(@NonNull ServerInfoResponse response) {
        name = response.getName();
        map = response.getMap();
        version = response.getVersion();
        playersNum = response.getPlayersNum();
        maxPlayers = response.getMaxPlayers();
        serverTime = response.getTime();
        isFirstPerson = response.isFirstPerson();
        dayTimeMult = response.getDayTimeMult();
        nightTimeMult = response.getNightTimeMult();
        queueSize = response.getQueueSize();
        calculateTimeRelatedValues();
    }

    private void store(@NonNull ServerPlayersResponse playersResponse) {
        this.players = playersResponse.getPlayers();
    }

    private void calculateTimeRelatedValues() {
        if (!(dayTimeMult + nightTimeMult > 0)) {
            Log.e(SQ_TAG, "Time factors are not valid, dayTimeMult: " + dayTimeMult + ", nightTimeMult: " + nightTimeMult);
            return;
        }

        calculateDayAndNightDuration();

        LocalTime serverTimeValue = LocalTime.parse(serverTime);
        isDaytime = (serverTimeValue.isAfter(SUNRISE_TIME) || serverTimeValue == SUNRISE_TIME) && serverTimeValue.isBefore(SUNSET_TIME);
    }

    private void calculateDayAndNightDuration() {
        long daytimeMinutes = ChronoUnit.MINUTES.between(SUNRISE_TIME, SUNSET_TIME);
        long nighttimeMinutes = 24 * 60 - daytimeMinutes;

        dayDuration = formatDuration(Duration.ofMinutes((long) (daytimeMinutes / dayTimeMult)));
        nightDuration = formatDuration(Duration.ofMinutes((long) (nighttimeMinutes / (dayTimeMult * nightTimeMult))));
    }

    public boolean isDaytime() {
        return isDaytime;
    }

    public Vector<Player> getPlayers() {
        return players;
    }

    public String getAddress() {
        return ip + ":" + port;
    }

    public String getIp() { return ip; }

    public int getPort() { return port; }

    public String getName() {
        return name;
    }

    public String getMap() {
        return map;
    }

    public String getVersion() {
        return version;
    }

    public int getPlayersNum() {
        return playersNum;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public String getServerTime() {
        return serverTime;
    }

    public boolean isFirstPerson() {
        return isFirstPerson;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public String getDayDuration() {
        return dayDuration;
    }

    public String getNightDuration() {
        return nightDuration;
    }
}