package pl.tmkd.serverz.sq.msg;

import static pl.tmkd.serverz.sq.Constants.*;

public class Message {
    protected byte header;
    protected byte[] payload;

    public Message(byte header) {
        this.header = header;
        this.payload = UNKNOWN_CHALLENGE_ID;
    }

    public Message(byte[] payload) {
        header = payload.length >= 4 ? payload[4] : 0;
        this.payload = payload;
    }

    public byte getHeader() {
        return header;
    }

    public byte[] getPayload() {
        return payload;
    }
}
