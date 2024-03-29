package pl.tmkd.serverz.sq;

import java.time.LocalTime;

public class Constants {
    private Constants(){}

    public static final byte ID_INFO_RESP = 0x49;
    public static final byte ID_PLAYER_RESP = 0x44;
    public static final byte ID_RULES_RESP = 0x45;
    public static final byte ID_INVALID_CHALLENGE = 0x41;
    public static final byte ID_INFO_REQ = 0x54;
    public static final byte ID_PLAYER_REQ = 0x55;
    public static final byte ID_RULES_REQ = 0x56;
    public static final byte[] STEAM_QUERY_HEADER = {(byte)255, (byte)255, (byte)255, (byte)255};
    public static final int TIMER_QUERY_GUARD = 5000;
    public static final int TIMER_QUERY_RETRY_SLOW = 20000;
    public static final int TIMER_QUERY_RETRY_FAST = 10000;
    public static final byte CHALLENGE_ID_LENGTH = 4;
    public static final String NO_3RD_CAMERA = "no3rd";
    public static final String TAG_SQ = "tmkd-sq";
    public static final String TAG_SERVER = "tmkd-server";
    public static final String TAG_MAIN = "tmkd-activity";
    public static final String TAG_SECOND = "tmkd-second";
    public static final String TAG_FRAGMENT = "tmkd-fragment";
    public static final String STEAM_MOD_URL = "https://steamcommunity.com/sharedfiles/filedetails/?id=";
    public static final LocalTime SUNRISE_TIME = LocalTime.of(5, 0, 0);
    public static final LocalTime SUNSET_TIME = LocalTime.of(19, 0, 0);
}