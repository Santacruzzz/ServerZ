package pl.tmkd.serverz.sq.msg;

import static pl.tmkd.serverz.sq.msg.Utils.merge;

public class Request extends Message {
    public Request(byte header) {
        super(header);
        payload = merge(payload, new byte[]{ header });
    }

    public Request(byte header, byte[] content) {
        super(header);
        payload = merge(payload, content);
    }
}