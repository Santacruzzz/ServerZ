package pl.tmkd.serverz.sq;

import static pl.tmkd.serverz.sq.Constants.*;
import static pl.tmkd.serverz.sq.msg.Utils.merge;
import static pl.tmkd.serverz.sq.msg.Utils.payloadToString;
import static pl.tmkd.serverz.sq.msg.Utils.right;

import android.util.Log;

import androidx.annotation.NonNull;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;

import pl.tmkd.serverz.sq.msg.Request;
import pl.tmkd.serverz.sq.msg.Response;
import pl.tmkd.serverz.sq.msg.ServerInfoResponse;
import pl.tmkd.serverz.sq.msg.ServerPlayersResponse;
import pl.tmkd.serverz.sq.msg.ServerRulesResponse;

public class SourceQueryTask implements Runnable {
    private final Request a2sInfo;
    private final Request a2sPlayer;
    private final Request a2sRules;
    private InetSocketAddress address;
    private final DatagramPacket receivedPacket;
    private SqResponseListener listener;
    private DatagramSocket socket;
    private byte[] challengeId;
    private final RefreshType refreshType;
    private boolean hasRefreshedMods;
    private ServerInfoResponse infoResp;
    private ServerPlayersResponse playerResp;
    private ServerRulesResponse rulesResponse;

    public SourceQueryTask(String ip, int port, RefreshType refreshType) {
        a2sInfo = new Request(ID_INFO_REQ, "Source Engine Query\0".getBytes());
        a2sPlayer = new Request(ID_PLAYER_REQ);
        a2sRules = new Request(ID_RULES_REQ);
        address = new InetSocketAddress(ip, port);
        challengeId = new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
        this.refreshType = refreshType;
        socket = null;
        byte[] buffer = new byte[2048];
        receivedPacket = new DatagramPacket(buffer, 2048);
        hasRefreshedMods = false;
        resetResponses();
    }

    private void resetResponses() {
        infoResp = null;
        playerResp = null;
        rulesResponse = null;
    }

    public void setListener(SqResponseListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            if (null == socket) {
                socket = new DatagramSocket();
            }

            handleResponse(sendRequest(a2sInfo));

            if (refreshType.equals(RefreshType.FULL)) {
                handleResponse(sendRequest(a2sPlayer));
                if (!hasRefreshedMods)
                {
                    handleResponse(sendRequest(a2sRules));
                }
            }
            if (null != listener)
                listener.onServerInfoResponse(infoResp, playerResp, rulesResponse);
            resetResponses();
        } catch (Exception e) {
            Log.e(TAG_SQ, address + " :: Task failed, msg: " + e.getMessage());
            Log.e(TAG_SQ, Arrays.toString(e.getStackTrace()));
            listener.onServerRefreshFailed();
        }
    }

    private void handleResponse(@NonNull Response response) throws Exception {
        switch (response.getId()) {
            case ID_INFO_RESP:
                infoResp = new ServerInfoResponse(response.getBuffer());
                return;
            case ID_PLAYER_RESP:
                playerResp = new ServerPlayersResponse(response.getBuffer());
                return;
            case ID_RULES_RESP:
                rulesResponse = new ServerRulesResponse(response.getBuffer());
                hasRefreshedMods = true;
                return;
            default:
                throw new Exception("Unknown response ID: " + String.format("0x%02X", response.getId()));
        }
    }

    @NonNull
    private Response sendRequest(@NonNull Request request) throws Exception {
        if (null == socket) {
            throw new Exception("Socket not initialized");
        }
        DatagramPacket packetToSend = new DatagramPacket(request.getPayload(), request.getSize(), address);
        packetToSend.setData(merge(request.getPayload(), challengeId), 0, request.getSize() + 4);
        socket.send(packetToSend);
        Log.d(TAG_SQ, address + " :: Request " + request.getName() + " sent, challengeId: " + payloadToString(challengeId));
        socket.setSoTimeout(TIMER_QUERY_GUARD);
        socket.receive(receivedPacket);
        Response response = new Response(receivedPacket);
        Log.d(TAG_SQ, address + " :: " + response.getName() + " received, size: " + receivedPacket.getLength() + " bytes");

        if (response.isNewChallengeIdNeeded()) {
            Log.w(TAG_SQ, address + " :: New challenge needed. Updating new ID and resending the " + request.getName());
            challengeId = right(response.getPayload(), CHALLENGE_ID_LENGTH);
            return sendRequest(request);
        }
        return response;
    }

    public void setAddress(String ip, int port) {
        address = new InetSocketAddress(ip, port);
    }
}
