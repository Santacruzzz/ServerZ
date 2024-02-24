package pl.tmkd.serverz.sq;

public interface ServerListener {
    void onServerInfoRefreshed();
    void onServerInfoRefreshFailed(Server server);
}
