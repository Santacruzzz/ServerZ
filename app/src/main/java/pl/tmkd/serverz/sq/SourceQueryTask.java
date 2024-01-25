package pl.tmkd.serverz.sq;

import static pl.tmkd.serverz.sq.Constants.*;
import static pl.tmkd.serverz.sq.msg.Utils.merge;
import static pl.tmkd.serverz.sq.msg.Utils.right;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import pl.tmkd.serverz.sq.msg.Request;
import pl.tmkd.serverz.sq.msg.Response;
import pl.tmkd.serverz.sq.msg.ServerInfoResponse;
import pl.tmkd.serverz.sq.msg.ServerPlayersResponse;
import pl.tmkd.serverz.sq.msg.ServerRulesResponse;

public class SourceQueryTask implements Runnable {
    private final Request a2sInfo;
    private final Request a2sPlayer;
    private final Request a2sRules;
    private final InetSocketAddress address;
    private ServerDataListener listener;
    private DatagramSocket socket;
    private byte[] challengeId;

    public SourceQueryTask(String ip, int port) {
        a2sInfo = new Request(ID_INFO_REQ, "Source Engine Query\0".getBytes());
        a2sPlayer = new Request(ID_PLAYER_REQ);
        a2sRules = new Request(ID_RULES_REQ);
        address = new InetSocketAddress(ip, port);
        challengeId = null;
    }

    public void setListener(ServerDataListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            socket = new DatagramSocket();
            handleValidResponse(sendRequest(a2sInfo));
            handleValidResponse(sendRequest(a2sPlayer));
            handleValidResponse(sendRequest(a2sRules));
        } catch (IOException e) {
            Log.d(SQ_TAG, e.toString());
        }
    }

    private void handleValidResponse(Response response) {
        switch (response.getId()) {
            case ID_INFO_RESP:
                listener.onServerInfoResponse(new ServerInfoResponse(response.getPayload()));
                break;
            case ID_PLAYER_RESP:
                listener.onServerPlayerResponse(new ServerPlayersResponse(response.getPayload()));
                break;
            case ID_RULES_RESP:
                listener.onServerRulesResponse(new ServerRulesResponse(response.getPayload()));
                break;
            default:
                Log.e(SQ_TAG, "Invalid ID in response: " + String.format("%02x", response.getId()));
        }
    }

    private Response sendRequest(Request request) throws IOException {
        DatagramPacket packetToSend = new DatagramPacket(request.getPayload(), request.getSize(), address);

        if (challengeId != null) {
            packetToSend.setData(merge(request.getPayload(), challengeId), 0, request.getSize() + 4);
        }

        socket.send(packetToSend);
        Log.d(SQ_TAG, "Request " + request.getName() + " sent");

        byte[] buf = new byte[1024];
        DatagramPacket receivedPacket = new DatagramPacket(buf, 1024);
        socket.receive(receivedPacket);
        Response response = new Response(receivedPacket);
        Log.d(SQ_TAG, response.getName() + " received, size: " + receivedPacket.getLength() + " bytes");

        if (response.isNewChallengeIdNeeded()) {
            Log.w(SQ_TAG, "New challenge needed. Updating new ID and resending the " + request.getName());
            challengeId = right(response.getPayload(), 4);
            return sendRequest(request);
        }
        return response;
    }
}
