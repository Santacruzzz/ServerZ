package pl.tmkd.serverz.sq;

public interface ServerListener {
    void onServerInfoRefreshed(Server server);
    void onServerInfoRefreshFailed(Server server);
}
