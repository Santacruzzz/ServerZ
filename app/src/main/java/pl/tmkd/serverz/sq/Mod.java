package pl.tmkd.serverz.sq;

import static pl.tmkd.serverz.sq.Constants.STEAM_MOD_URL;

import androidx.annotation.NonNull;

public class Mod {
    private long steamId;
    private String name;

    public Mod(long steamId, String name) {
        this.steamId = steamId;
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public String getUrl()
    {
        return STEAM_MOD_URL + steamId;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
