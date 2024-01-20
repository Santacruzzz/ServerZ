package pl.tmkd.serverz.sq;

import static pl.tmkd.serverz.sq.Constants.*;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.tmkd.serverz.sq.msg.Request;

public class Server {
    private final String ip;
    private final int port;
    private final Request a2sInfo = new Request(ID_INFO_REQ, "Source Engine Query\0".getBytes());
    private final Request a2sPlayer = new Request(ID_PLAYER_REQ);
    private final Request a2sRules = new Request(ID_RULES_REQ);
    private Socket socket;
    private ExecutorService executor;

    public Server(String ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;
        executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                socket = new Socket(ip, port);
            } catch (IOException e) {

            }
        });
        executor.shutdown();
    }

    public String getAddress() {
        return ip + ":" + port;
    }
}

