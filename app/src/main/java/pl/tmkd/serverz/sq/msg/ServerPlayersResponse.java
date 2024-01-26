package pl.tmkd.serverz.sq.msg;

import java.nio.ByteBuffer;

public class ServerPlayersResponse extends ParsedResponse {
    public ServerPlayersResponse(ByteBuffer payload) {
        super(payload);
    }
}
