package pl.tmkd.serverz.sq.msg;

import static pl.tmkd.serverz.sq.Constants.*;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class SourceQueryMessage {
    protected byte id = 0;
    protected byte[] payload = null;

    public SourceQueryMessage() {}

    public SourceQueryMessage(byte id) {
        this.id = id;
        this.payload = STEAM_QUERY_HEADER;
    }

    public SourceQueryMessage(byte[] payload) {
        storePayload(payload);
    }

    protected void storePayload(byte[] payload) {
        id = payload.length >= 4 ? payload[4] : 0;
        this.payload = payload;
    }

    public byte getId()
    {
        return id;
    }

    public byte[] getPayload() {
        return payload;
    }

    @NotNull
    public ByteBuffer getBuffer() {
        return ByteBuffer.wrap(payload);
    }

    public int getSize() {
        return payload.length;
    }

    public String getName() {
        switch (id) {
            case ID_INFO_REQ:
                return "INFO_REQ";
            case ID_PLAYER_REQ:
                return "PLAYER REQ";
            case ID_RULES_REQ:
                return "RULES REQ";
            case ID_INFO_RESP:
                return "INFO RESP";
            case ID_PLAYER_RESP:
                return "PLAYER RESP";
            case ID_INVALID_CHALLENGE:
                return "INVALID CHALLENGE";
            case ID_RULES_RESP:
                return "RULES RESP";
        }
        return "UNKNOWN";
    }
}
