package pl.tmkd.serverz.sq.msg;

import java.nio.ByteBuffer;

public class ParsedResponse {
    protected final ByteBuffer payload;

    public ParsedResponse(ByteBuffer payload) {
        this.payload = payload;
    }
}
