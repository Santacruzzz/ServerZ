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
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.tmkd.serverz.sq.msg.Player;
import pl.tmkd.serverz.sq.msg.ServerInfoResponse;
import pl.tmkd.serverz.sq.msg.ServerPlayersResponse;
import pl.tmkd.serverz.sq.msg.ServerRulesResponse;

public class Server implements SqResponseListener, Runnable{
    private String ip;
    private int port;
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
    private ArrayList<Player> players;
    private final Handler refreshHandler;
    private boolean started;
    private boolean refreshFailed;
    private final int refreshTimer;
    private long ingameMinutesToSunriseOrSunset;
    private ArrayList<Mod> mods;
    private int retries;

    public Server(String ip, int port, RefreshType refreshType) {
        this.ip = ip;
        this.port = port;
        players = new ArrayList<>();
        mods = new ArrayList<>();
        refreshServerDataTask = new SourceQueryTask(ip, port, refreshType);
        refreshServerDataTask.setListener(this);
        executor = Executors.newSingleThreadExecutor();
        refreshHandler = new Handler(Looper.getMainLooper());
        started = false;
        refreshFailed = false;
        refreshTimer = refreshType.equals(RefreshType.FULL) ? TIMER_QUERY_RETRY_FAST : TIMER_QUERY_RETRY_SLOW;
        serverTime = LocalTime.parse("00:00");
        name = getAddress();
        retries = 0;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        Server that = (Server) other;
        return ip.equals(that.getIp()) && port == that.getPort();
    }

    public void start() {
        if (started)
        {
            Log.w(TAG_SERVER, getAddress() + " :: Server already started!");
            return;
        }

        started = true;
        Log.d(TAG_SERVER, getAddress() + " :: Started, refreshTimer: " + refreshTimer);
        refreshServerData();
    }

    public void stop() {
        started = false;
        Log.d(TAG_SERVER, getAddress() + " :: Removing callbacks (has one? " + refreshHandler.hasCallbacks(this) + ")");
        refreshHandler.removeCallbacks(this);
        resetRetries();
    }

    public void setListener(ServerListener listener) {
        this.listener = listener;
    }

    private void refreshServerData() {
        executor.execute(refreshServerDataTask);
    }

    private void resetRetries() {
        retries = 0;
    }

    private void incrementRetry() throws Exception {
        retries ++;
        if (retries >= 4) {
            throw new Exception("Max retries limit reached");
        }
    }

    @Override
    public void onServerInfoResponse(ServerInfoResponse infoResponse, ServerPlayersResponse playersResponse, ServerRulesResponse rulesResponse) {
        Log.d(TAG_SERVER, getAddress() + " :: onServerInfoResponse: " + infoResponse + ", " + playersResponse + ", " + rulesResponse);
        resetRetries();
        store(infoResponse);
        if (null != playersResponse)
            store(playersResponse);
        if (null != rulesResponse)
            store(rulesResponse);
        refreshHandler.postDelayed(this, refreshTimer);

        if (null != listener) {
            listener.onServerInfoRefreshed();
        }
    }

    @Override
    public void onServerRefreshFailed() {
        refreshFailed = true;
        try {
            if (started) {
                incrementRetry();
                Log.w(TAG_SERVER, getAddress() + " :: onServerRefreshFailed, retrying #:" + retries);
                refreshHandler.postDelayed(this, 0);
            }
        } catch (Exception e) {
            if (null != listener) {
                listener.onServerInfoRefreshFailed(this);
            }
            resetRetries();
            Log.w(TAG_SERVER, getAddress() + " :: onServerRefreshFailed, msg: " + e.getMessage() + ", retrying in: " + TIMER_QUERY_RETRY_FAST + "ms");
            refreshHandler.postDelayed(this, TIMER_QUERY_RETRY_FAST);
        }
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
        players.clear();
        players.addAll(playersResponse.getPlayers());
        Log.d(TAG_SERVER, getAddress() + " :: Number of loaded players: " + players.size());
    }

    private void store(@NonNull ServerRulesResponse rulesResponse) {
        mods.clear();
        mods.addAll(rulesResponse.getMods());
        Log.d(TAG_SERVER, getAddress() + " :: Number of loaded mods: " + mods.size());
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
                + ", real: " + tillSunsetOrSunrise + " (" + getDayOrNightProgress() + "%)");
    }

    private void calculateMinutesTillSunsetOrSunrise() {
        ingameMinutesToSunriseOrSunset = ChronoUnit.MINUTES.between(serverTime, SUNSET_TIME);
        tillSunsetOrSunrise = formatDuration(Duration.ofMinutes((long) (ingameMinutesToSunriseOrSunset / dayTimeMult)));
        if (!isDaytime()) {
            ingameMinutesToSunriseOrSunset = 0;
            LocalTime oneSecondToMidnight = LocalTime.MIDNIGHT.minus(Duration.ofSeconds(1));
            if (serverTime.isBefore(oneSecondToMidnight)) {
                ingameMinutesToSunriseOrSunset += ChronoUnit.MINUTES.between(serverTime, oneSecondToMidnight);
                ingameMinutesToSunriseOrSunset += ChronoUnit.MINUTES.between(LocalTime.MIDNIGHT, SUNRISE_TIME);
            } else {
                ingameMinutesToSunriseOrSunset += ChronoUnit.MINUTES.between(serverTime, SUNRISE_TIME);
            }
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

    public ArrayList<Player> getPlayers() { return players; }

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

    public ArrayList<Mod> getMods() { return this.mods; }

    public String getDurationTillSunriseOrSunset() {
        return tillSunsetOrSunrise;
    }

    public void setIp(String ip) {
        this.name = "";
        this.playersNum = 0;
        this.maxPlayers = 0;
        this.ip = ip;
        this.refreshServerDataTask.setAddress(this.ip, this.port);
    }

    public void setPort(int port) {
        this.name = "";
        this.playersNum = 0;
        this.maxPlayers = 0;
        this.port = port;
        this.refreshServerDataTask.setAddress(this.ip, this.port);
    }

    public int getDayOrNightProgress() {
        double minutesInDay = ChronoUnit.MINUTES.between(SUNRISE_TIME, SUNSET_TIME);
        double minutesInNight = 24 * 60 - minutesInDay;

        if (isDaytime()) {
            return (int) (100 - (ingameMinutesToSunriseOrSunset / minutesInDay) * 100);
        }
        return (int) (100 - (ingameMinutesToSunriseOrSunset / minutesInNight) * 100);
    }

    public boolean hasRefreshFailed() {
        return refreshFailed;
    }
}