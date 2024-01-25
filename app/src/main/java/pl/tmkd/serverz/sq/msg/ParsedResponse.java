package pl.tmkd.serverz.sq.msg;

public class ParsedResponse {
    protected final byte[] payload;

    public ParsedResponse(byte[] payload) {
        this.payload = payload;
    }
}
