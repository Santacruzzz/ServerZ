package pl.tmkd.serverz.sq.msg;

import java.nio.ByteBuffer;

public class ServerRulesResponse extends ParsedResponse {
    public ServerRulesResponse(ByteBuffer payload) {
        super(payload);
    }
}
