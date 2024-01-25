package pl.tmkd.serverz.sq;

import pl.tmkd.serverz.sq.msg.ServerInfoResponse;
import pl.tmkd.serverz.sq.msg.ServerPlayersResponse;
import pl.tmkd.serverz.sq.msg.ServerRulesResponse;

public interface ServerDataListener {
    public void onServerInfoResponse(ServerInfoResponse response);
    public void onServerPlayerResponse(ServerPlayersResponse response);
    public void onServerRulesResponse(ServerRulesResponse response);
}
