package pl.tmkd.serverz.sq;

import static pl.tmkd.serverz.sq.Constants.*;
import static pl.tmkd.serverz.sq.msg.Utils.merge;
import static pl.tmkd.serverz.sq.msg.Utils.right;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import pl.tmkd.serverz.sq.msg.ParsedResponse;
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
    private final DatagramPacket receivedPacket;
    private SqResponseListener listener;
    private DatagramSocket socket;
    private byte[] challengeId;
    private int retries;
    private final RefreshType refreshType;
    private final Response emptyResponse;
    private boolean hasRefreshedMods;

    public SourceQueryTask(String ip, int port, RefreshType refreshType) {
        a2sInfo = new Request(ID_INFO_REQ, "Source Engine Query\0".getBytes());
        a2sPlayer = new Request(ID_PLAYER_REQ);
        a2sRules = new Request(ID_RULES_REQ);
        address = new InetSocketAddress(ip, port);
        challengeId = null;
        retries = 0;
        this.refreshType = refreshType;
        socket = null;
        emptyResponse = new Response();
        byte[] buffer = new byte[2048];
        receivedPacket = new DatagramPacket(buffer, 2048);
        hasRefreshedMods = false;
    }

    public void setListener(SqResponseListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        retries = 0;
        try {
            if (null == socket)
                socket = new DatagramSocket();

            ServerInfoResponse infoResp = (ServerInfoResponse) handleResponse(sendRequest(a2sInfo));
            ServerPlayersResponse playerResp = null;
            ServerRulesResponse rulesResponse = null;

            if (refreshType.equals(RefreshType.FULL)) {
                playerResp = (ServerPlayersResponse) handleResponse(sendRequest(a2sPlayer));
                if (!hasRefreshedMods)
                {
                    rulesResponse = (ServerRulesResponse) handleResponse(sendRequest(a2sRules));
                    hasRefreshedMods = true;
                }
            }
            if (null != listener)
                listener.onServerInfoResponse(infoResp, playerResp, rulesResponse);
        } catch (Exception e) {
            Log.e(TAG_SQ, e + " : " + Arrays.toString(e.getStackTrace()));
        }
    }

    private ParsedResponse handleResponse(@NonNull Response response) throws Exception {
        switch (response.getId()) {
            case ID_INFO_RESP:
                return new ServerInfoResponse(response.getBuffer());
            case ID_PLAYER_RESP:
                return new ServerPlayersResponse(response.getBuffer());
            case ID_RULES_RESP:
                return new ServerRulesResponse(response.getBuffer());
        }
        throw new Exception("Unknown response ID: " + String.format("0x%02X", response.getId()));
    }

    @NonNull
    private Response sendRequest(@NonNull Request request) throws IOException {
        if (null == socket) {
            Log.e(TAG_SQ, address + " :: " + "Socket not initialized. Returning default Response()");
            return emptyResponse;
        }
        if (retries >= 3) {
            Log.e(TAG_SQ, address + " :: " + "Max retries limit reached.");
            listener.onServerRetryLimitReached();
            return emptyResponse;
        }
        DatagramPacket packetToSend = new DatagramPacket(request.getPayload(), request.getSize(), address);

        if (challengeId != null) {
            packetToSend.setData(merge(request.getPayload(), challengeId), 0, request.getSize() + 4);
        }

        socket.send(packetToSend);
        Log.d(TAG_SQ, address + " :: Request " + request.getName() + " sent");

        socket.setSoTimeout(TIMER_QUERY_GUARD);
        try {
            socket.receive(receivedPacket);
        } catch (SocketTimeoutException e) {
            retries ++;
            Log.w(TAG_SQ, address + " :: Timeout, retrying... (" + retries + ")");
            return sendRequest(request);
        }
        Response response = new Response(receivedPacket);
        Log.d(TAG_SQ, address + " :: " + response.getName() + " received, size: " + receivedPacket.getLength() + " bytes");

        if (response.isNewChallengeIdNeeded()) {
            retries ++;
            Log.w(TAG_SQ, address + " :: New challenge needed. Updating new ID and resending the " + request.getName());
            challengeId = right(response.getPayload(), CHALLENGE_ID_LENGTH);
            return sendRequest(request);
        }
        retries = 0;
        return response;
    }
}
