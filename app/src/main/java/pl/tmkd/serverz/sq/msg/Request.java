package pl.tmkd.serverz.sq.msg;

import static pl.tmkd.serverz.sq.msg.Utils.merge;

public class Request extends SourceQueryMessage {
    public Request(byte id) {
        super(id);
        payload = merge(payload, new byte[]{ id });
    }

    public Request(byte id, byte[] content) {
        this(id);
        payload = merge(payload, content);
    }
}