package pl.tmkd.serverz.sq.msg;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static pl.tmkd.serverz.sq.Constants.SQ_TAG;
import static pl.tmkd.serverz.sq.msg.Utils.readString;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Vector;

public class ServerPlayersResponse extends ParsedResponse {
    private final Vector<Player> players;

    public ServerPlayersResponse(ByteBuffer payload) {
        super(payload);
        players = new Vector<Player>();

        this.payload.position(4);
        short numOfPlayers = (short) (this.payload.getShort() & 0xFF);

        Log.d(SQ_TAG, "numOfPlayers: " + numOfPlayers);
        for (short i = 0; i < numOfPlayers; ++i) {
            Player player = new Player();
            player.setId((short) (this.payload.get() & 0xFF));
            player.setName(readString(this.payload));
            player.setScore((int) (this.payload.getInt() & 0xFFFF));
            player.setDuration((float) (this.payload.order(LITTLE_ENDIAN).getFloat()));
            this.players.add(player);
        }
    }

    public Vector<Player> getPlayers() {
        return this.players;
    }
}
