package pl.tmkd.serverz.sq;

import static pl.tmkd.serverz.sq.Constants.SQ_TAG;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.tmkd.serverz.sq.msg.ServerInfoResponse;
import pl.tmkd.serverz.sq.msg.ServerPlayersResponse;
import pl.tmkd.serverz.sq.msg.ServerRulesResponse;

public class Server implements ServerDataListener {
    private final String ip;
    private final int port;
    private final ExecutorService executor;
    private final SourceQueryTask refreshServerDataTask;

    public Server(String ip, int port) {
        this.ip = ip;
        this.port = port;
        refreshServerDataTask = new SourceQueryTask(ip, port);
        refreshServerDataTask.setListener(this);
        executor = Executors.newSingleThreadExecutor();
        refreshServerData();
    }

    private void refreshServerData() {
        executor.execute(refreshServerDataTask);
        executor.shutdown();
    }

    public String getAddress() {
        return ip + ":" + port;
    }

    @Override
    public void onServerInfoResponse(ServerInfoResponse response) {
        Log.d(SQ_TAG, "onServerInfoResponse");
    }

    @Override
    public void onServerPlayerResponse(ServerPlayersResponse response) {
        Log.d(SQ_TAG, "onServerPlayerResponse");
    }

    @Override
    public void onServerRulesResponse(ServerRulesResponse response) {
        Log.d(SQ_TAG, "onServerRulesResponse");
    }
}