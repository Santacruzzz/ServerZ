package pl.tmkd.serverz.sq;

import static pl.tmkd.serverz.sq.Constants.SUNRISE_TIME;
import static pl.tmkd.serverz.sq.Constants.SUNSET_TIME;
import static pl.tmkd.serverz.sq.Constants.TAG_SERVER;
import static pl.tmkd.serverz.sq.Constants.TIMER_QUERY_RETRY_FAST;
import static pl.tmkd.serverz.sq.Constants.TIMER_QUERY_RETRY_SLOW;
import static pl.tmkd.serverz.sq.msg.Utils.formatDuration;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import pl.tmkd.serverz.sq.msg.Player;
import pl.tmkd.serverz.sq.msg.ServerInfoResponse;
import pl.tmkd.serverz.sq.msg.ServerPlayersResponse;

public class Server implements SqResponseListener, Runnable{
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
    private LocalTime serverTime;
    private boolean isFirstPerson;
    private float dayTimeMult;
    private float nightTimeMult;
    private int queueSize;
    private String tillSunsetOrSunrise;
    private String dayDuration;
    private String nightDuration;
    private boolean isDaytime;
    private Vector<Player> players;
    private final Handler refreshHandler;
    private boolean started;
    private boolean refreshFailed;
    private final int refreshTimer;
    private long ingameMinutesToSunriseOrSunset;

    public Server(String ip, int port, RefreshType refreshType) {
        this.ip = ip;
        this.port = port;
        players = new Vector<>();
        refreshServerDataTask = new SourceQueryTask(ip, port, refreshType);
        refreshServerDataTask.setListener(this);
        executor = Executors.newSingleThreadExecutor();
        refreshHandler = new Handler(Looper.getMainLooper());
        started = false;
        refreshFailed = false;
        refreshTimer = refreshType.equals(RefreshType.FULL) ? TIMER_QUERY_RETRY_FAST : TIMER_QUERY_RETRY_SLOW;
    }

    public void start() {
        started = true;
        Log.d(TAG_SERVER, getAddress() + " :: Started, refreshTimer: " + refreshTimer);
        refreshServerData();
    }

    public void stop() {
        started = false;
        Log.d(TAG_SERVER, getAddress() + " :: Removing callbacks (has one? " + refreshHandler.hasCallbacks(this) + ")");
        refreshHandler.removeCallbacks(this);
    }

    public void setListener(ServerListener listener) {
        this.listener = listener;
    }

    private void refreshServerData() {
        executor.execute(refreshServerDataTask);
    }

    @Override
    public void onServerInfoResponse(ServerInfoResponse response) {
        Log.d(TAG_SERVER, getAddress() + " :: onServerInfoResponse");
        store(response);
        refreshHandler.postDelayed(this, refreshTimer);

        if (null != listener)
            listener.onServerInfoRefreshed(this);
    }

    @Override
    public void onServerInfoAndPlayersResponse(ServerInfoResponse infoResponse, ServerPlayersResponse playersResponse) {
        Log.d(TAG_SERVER, getAddress() + " :: onServerInfoAndPlayersResponse");
        store(infoResponse);
        store(playersResponse);
        refreshHandler.postDelayed(this, refreshTimer);

        if (null != listener)
            listener.onServerInfoRefreshed(this);
    }

    @Override
    public void onServerRetryLimitReached() {
        refreshFailed = true;
        Log.d(TAG_SERVER, getAddress() + " :: onServerRetryLimitReached");
        stop();
        if (null != listener)
            listener.onServerInfoRefreshFailed(this);
    }

    @Override
    public void run() {
        if (started) {
            Log.d(TAG_SERVER, getAddress() + " :: run() : Refreshing server data");
            refreshServerData();
        } else {
            Log.w(TAG_SERVER, getAddress() + " :: run() prevented, server stopped");
        }
    }

    private void store(@NonNull ServerInfoResponse response) {
        refreshFailed = false;

        name = response.getName();
        map = response.getMap();
        version = response.getVersion();
        playersNum = response.getPlayersNum();
        maxPlayers = response.getMaxPlayers();
        serverTime = LocalTime.parse(response.getTime());
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
        if (dayTimeMult * nightTimeMult == 0) {
            Log.e(TAG_SERVER, getAddress() + " :: Time factors are not valid, dayTimeMult: " + dayTimeMult + ", nightTimeMult: " + nightTimeMult);
            return;
        }

        setDayOrNightTime();
        calculateDayAndNightDuration();
        calculateMinutesTillSunsetOrSunrise();

        Log.d(TAG_SERVER, getAddress() + " :: Server time: " + serverTime
                + ", in game till day/night: " + formatDuration(Duration.ofMinutes(ingameMinutesToSunriseOrSunset))
                + ", real: " + tillSunsetOrSunrise);

        Log.d(TAG_SERVER, getAddress() + " :: progress: " + getDayOrNightProgress());
    }

    private void calculateMinutesTillSunsetOrSunrise() {
        ingameMinutesToSunriseOrSunset = ChronoUnit.MINUTES.between(serverTime, SUNSET_TIME);
        tillSunsetOrSunrise = formatDuration(Duration.ofMinutes((long) (ingameMinutesToSunriseOrSunset / dayTimeMult)));
        if (!isDaytime()) {
            ingameMinutesToSunriseOrSunset = 0;
            if (serverTime.isBefore(LocalTime.MIDNIGHT)) {
                ingameMinutesToSunriseOrSunset += ChronoUnit.MINUTES.between(serverTime, LocalTime.MIDNIGHT);
            }
            ingameMinutesToSunriseOrSunset += ChronoUnit.MINUTES.between(serverTime, SUNRISE_TIME);
            tillSunsetOrSunrise = formatDuration(Duration.ofMinutes((long) (ingameMinutesToSunriseOrSunset / (dayTimeMult * nightTimeMult))));
        }
    }

    private void setDayOrNightTime() {
        isDaytime = (serverTime.isAfter(SUNRISE_TIME) || serverTime == SUNRISE_TIME) && serverTime.isBefore(SUNSET_TIME);
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

    public List<String> getPlayers() {
        return players.stream().map(Player::getPlaytime).collect(Collectors.toList());
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
        return serverTime.toString();
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

    public String getDurationTillSunriseOrSunset() {
        return tillSunsetOrSunrise;
    }

    public int getDayOrNightProgress() {
        double minutesInDay = ChronoUnit.MINUTES.between(SUNRISE_TIME, SUNSET_TIME);
        double minutesInNight = 24 * 60 - minutesInDay;

        if (isDaytime()) {
            return (int) (100 - (ingameMinutesToSunriseOrSunset / minutesInDay) * 100);
        }
        return (int) (100 - (ingameMinutesToSunriseOrSunset / minutesInNight) * 100);
    }

    public boolean isRefreshFailed() {
        return refreshFailed;
    }
}