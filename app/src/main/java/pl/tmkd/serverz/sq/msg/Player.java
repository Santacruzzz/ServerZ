package pl.tmkd.serverz.sq.msg;

import static pl.tmkd.serverz.sq.msg.Utils.formatDuration;

import androidx.annotation.NonNull;

import java.time.Duration;
import java.util.Locale;

public class Player {

    private short id;
    private String name;
    private int score;
    private String playtime;

    public void setId(short id) {
        this.id = id;
    }

    @NonNull
    public String toString() {
        return String.format(
                Locale.getDefault(),
                "Id: %d, name: %s, score: %d, durationHms: %s",
                this.getId(), this.getName(), this.getScore(), this.getPlaytime());
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScore(int score) {
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

    public int getScore() {
        return score;
    }

    public String getPlaytime() {
        return playtime;
    }
}
