package pl.tmkd.serverz.sq;

import java.io.Serializable;

public class ServerAddress implements Serializable {
    private int port;
    private String ip;

    public ServerAddress(int port, String ip) {
        this.port = port;
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }
}
