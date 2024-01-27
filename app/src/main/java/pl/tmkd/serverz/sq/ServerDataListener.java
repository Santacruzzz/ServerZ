package pl.tmkd.serverz.sq;

import pl.tmkd.serverz.sq.msg.ServerInfoResponse;
import pl.tmkd.serverz.sq.msg.ServerPlayersResponse;
import pl.tmkd.serverz.sq.msg.ServerRulesResponse;

public interface ServerDataListener {
    void onServerInfoResponse(ServerInfoResponse response);
    void onServerPlayerResponse(ServerPlayersResponse response);
    void onServerRulesResponse(ServerRulesResponse response);
}
