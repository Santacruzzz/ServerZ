package pl.tmkd.serverz.sq.msg;

import static pl.tmkd.serverz.sq.msg.Utils.formatDuration;

import androidx.annotation.NonNull;

import java.time.Duration;
import java.util.Locale;

public class Player {

    private short id;
    private String name;
    private long score;
    private String playtime;

    public void setId(short id) {
        this.id = id;
    }

    @NonNull
    @Override
    public String toString() {
        return this.getPlaytime();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public void setDuration(double duration) {
        this.playtime = formatDuration(Duration.ofSeconds((long) duration));
    }

    public short getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getScore() {
        return score;
    }

    public String getPlaytime() {
        return playtime;
    }
}
