package pl.tmkd.serverz.sq.msg;

import static pl.tmkd.serverz.sq.msg.Utils.getExtraValue;
import static pl.tmkd.serverz.sq.msg.Utils.readString;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import pl.tmkd.serverz.sq.Constants;

public class ServerInfoResponse extends ParsedResponse {
    private final short protocol;
    private final String name;
    private final String map;
    private final String game;
    private final byte type;
    private final byte env;
    private final String version;
    private final String gameFolder;
    private final int gameId;
    private final short playersNum;
    private final short maxPlayers;
    private final short bots;
    private final boolean havePassword;
    private final boolean haveVac;
    private String time;
    private boolean isFirstPerson;
    private float dayTimeMult;
    private float nightTimeMult;
    private int queueSize;

    public ServerInfoResponse(ByteBuffer payload) {
        super(payload);

        this.payload.position(5);
        protocol = (short) (this.payload.get() & 0xFF);
        name = readString(this.payload);
        map = readString(this.payload);
        gameFolder = readString(this.payload);
        game = readString(this.payload);
        gameId = this.payload.getShort() & 0xFFFF;
        playersNum = (short) (this.payload.get() & 0xFF);
        maxPlayers = (short) (this.payload.get() & 0xFF);
        bots = (short) (this.payload.get() & 0xFF);
        type = this.payload.get();
        env = this.payload.get();
        havePassword = this.payload.get() == 1;
        haveVac = this.payload.get() == 1;
        version = readString(this.payload);

        final short dayzFlag = 0xb1;
        short extraDataFlag = (short) (this.payload.get() & 0xFF);
        if (dayzFlag == extraDataFlag) {
            this.payload.position(this.payload.position() + 10);
            List<String> extraData = Arrays.asList(readString(this.payload).split(","));

            time = extraData.get(extraData.size() - 1);
            isFirstPerson = extraData.contains(Constants.NO_3RD_CAMERA);
            dayTimeMult = Float.parseFloat(getExtraValue(extraData, "etm"));
            nightTimeMult = Float.parseFloat(getExtraValue(extraData, "entm"));
            queueSize = Integer.parseInt(getExtraValue(extraData, "lqs"));
        }
    }

    public String getName() {
        return name;
    }

    public String getMap() {
        return map;
    }

    public String getVersion() {
        return version;
    }

    public int getPlayersNum() {
        return playersNum;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public String getTime() {
        return time;
    }

    public boolean isFirstPerson() {
        return isFirstPerson;
    }

    public float getDayTimeMult() {
        return dayTimeMult;
    }

    public float getNightTimeMult() {
        return nightTimeMult;
    }

    public int getQueueSize() {
        return queueSize;
    }
}
