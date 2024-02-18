package pl.tmkd.serverz.sq.msg;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static pl.tmkd.serverz.sq.msg.Utils.getBytes;
import static pl.tmkd.serverz.sq.msg.Utils.readString;

import java.nio.ByteBuffer;
import java.util.Vector;

public class ServerPlayersResponse extends ParsedResponse {
    private final Vector<Player> players;

    public ServerPlayersResponse(ByteBuffer payload) {
        players = new Vector<>();

        payload.position(4);
        short numOfPlayers = (short) (payload.getShort() & 0xFF);

        for (short i = 0; i < numOfPlayers; ++i) {
            Player player = new Player();
            player.setId((short) (payload.get() & 0xFF));
            player.setName(readString(payload));
            player.setScore(getBytes(payload, 4));
            player.setDuration(payload.order(LITTLE_ENDIAN).getFloat());
            this.players.add(player);
        }
    }

    public Vector<Player> getPlayers() {
        return this.players;
    }
}
