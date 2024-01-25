package pl.tmkd.serverz.sq.msg;

import java.util.ArrayList;

public class ServerInfoResponse extends ParsedResponse {
    private int protocol;   // 8
    private String name;
    private String map;
    private String game;
    private char type;      // 1
    private char env;       // 1
    private String version;
    private String gameFolder;
    private int port;       // 8
    private int gameId;     // 16
    private int players;    // 8
    private int maxPlayers; // 8
    private int bots;       // 8
    private boolean havePassword;
    private boolean haveVac;
    private ArrayList<String> extraData;


    public ServerInfoResponse(byte[] payload) {
        super(payload);
    }
}
