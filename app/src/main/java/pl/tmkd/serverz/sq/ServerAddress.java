package pl.tmkd.serverz.sq;

import java.io.Serializable;
import java.time.LocalTime;

public class ServerAddress implements Serializable {
    private int port;
    private String ip;
    private int sunriseTime;
    private int sunsetTime;

    public ServerAddress(int port, String ip, int sunriseTime, int sunsetTime) {
        this.port = port;
        this.ip = ip;
        this.sunriseTime = sunriseTime;
        this.sunsetTime = sunsetTime;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setSunriseTime(int sunriseTime) { this.sunriseTime = sunriseTime; }

    public void setSunsetTime(int sunsetTime) { this.sunsetTime = sunsetTime; }

    public int getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }

    public int getSunriseTime() { return sunriseTime ; }

    public int getSunsetTime() { return sunsetTime; }
}
