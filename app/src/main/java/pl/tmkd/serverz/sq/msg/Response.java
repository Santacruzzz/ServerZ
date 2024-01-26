package pl.tmkd.serverz.sq.msg;

import static pl.tmkd.serverz.sq.Constants.ID_INVALID_CHALLENGE;

import java.net.DatagramPacket;

public class Response extends SourceQueryMessage {

    public Response() {
        super();
    }

    public Response(DatagramPacket packet) {
        byte[] receivedData = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), 0, receivedData, 0, packet.getLength());
        storePayload(receivedData);
    }

    public boolean isNewChallengeIdNeeded() {
        return id == ID_INVALID_CHALLENGE;
    }
}

