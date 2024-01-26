package pl.tmkd.serverz.sq;

import static pl.tmkd.serverz.sq.Constants.SQ_TAG;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.tmkd.serverz.sq.msg.ServerInfoResponse;
import pl.tmkd.serverz.sq.msg.ServerPlayersResponse;
import pl.tmkd.serverz.sq.msg.ServerRulesResponse;

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
    private String time;
    private boolean isFirstPerson;
    private float dayTimeMult;
    private float nightTimeMult;
    private int queueSize;

    public Server(String ip, int port) {
        this.ip = ip;
        this.port = port;
        refreshServerDataTask = new SourceQueryTask(ip, port, false);
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

    private void store(ServerInfoResponse response) {
        name = response.getName();
        map = response.getMap();
        version = response.getVersion();
        playersNum = response.getPlayersNum();
        maxPlayers = response.getMaxPlayers();
        time = response.getTime();
        isFirstPerson = response.isFirstPerson();
        dayTimeMult = response.getDayTimeMult();
        nightTimeMult = response.getNightTimeMult();
        queueSize = response.getQueueSize();
    }

    private void store(ServerPlayersResponse playersResponse) {
    }

    public String getAddress() {
        return ip + ":" + port;
    }

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

    public String getTime() {
        return time;
    }

    public boolean isFirstPerson() {
        return isFirstPerson;
    }

    public float getDayTimeMult() {
        return dayTimeMult;
    }

    public float getNightTimeMult() {
        return nightTimeMult;
    }

    public int getQueueSize() {
        return queueSize;
    }
}