package pl.tmkd.serverz.sq;

import pl.tmkd.serverz.sq.msg.ServerInfoResponse;
import pl.tmkd.serverz.sq.msg.ServerPlayersResponse;
import pl.tmkd.serverz.sq.msg.ServerRulesResponse;

public interface SqResponseListener {
    void onServerInfoResponse(ServerInfoResponse infoResponse, ServerPlayersResponse playersResponse, ServerRulesResponse rulesResponse);
    void onServerRetryLimitReached();
}
