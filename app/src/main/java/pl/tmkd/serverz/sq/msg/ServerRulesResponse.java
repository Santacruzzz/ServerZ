package pl.tmkd.serverz.sq.msg;

import static java.lang.Math.min;
import static pl.tmkd.serverz.sq.Constants.TAG_SERVER;
import static pl.tmkd.serverz.sq.Constants.TAG_SQ;
import static pl.tmkd.serverz.sq.msg.Utils.getBytes;
import static pl.tmkd.serverz.sq.msg.Utils.getIndexOfByte;
import static pl.tmkd.serverz.sq.msg.Utils.readSizedString;

import android.util.Log;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Objects;

import pl.tmkd.serverz.sq.Mod;

public class ServerRulesResponse extends ParsedResponse {
    private final ArrayList<Mod> mods;

    public ServerRulesResponse(ByteBuffer payload) {

        mods = new ArrayList<>();

        payload.position(5);
        short numOfRules = (short) (payload.get() & 0xFF);

        ByteBuffer filteredModsBuffer = replaceEscapeBytes(readModsPayload(payload));
        short protocolVersion = (short) (filteredModsBuffer.get() & 0xFF);
        filteredModsBuffer.position(filteredModsBuffer.position() + 1);

        long dlcFlags = getBytes(filteredModsBuffer, 2);
        BitSet dlcBits = BitSet.valueOf(new long[]{dlcFlags});
        int bitsToSkipDlcHashes = dlcBits.cardinality() * 4;

        filteredModsBuffer.position(filteredModsBuffer.position() + bitsToSkipDlcHashes);
        short numOfMods = (short) (filteredModsBuffer.get() & 0xFF);

        Log.d(TAG_SERVER, "numOfMods: " + numOfMods);
        Log.d(TAG_SERVER, "numOfRules: " + numOfRules);
        Log.d(TAG_SERVER, "protocolVersion: " + protocolVersion);
        Log.d(TAG_SERVER, "dlcBits.cardinality(): " + dlcBits.cardinality());
        Log.d(TAG_SERVER, "bitsToSkipDlcHashes: " + bitsToSkipDlcHashes);

        for (int i = 0; i < numOfMods; i++) {
            long hash = filteredModsBuffer.getInt();
            short steamIdSize = (short) (filteredModsBuffer.get() & 0xFF);
            long steamId = getBytes(filteredModsBuffer, steamIdSize);
            String name = readSizedString(filteredModsBuffer);
            Log.i(TAG_SERVER, "mod name: " + name);
            mods.add(new Mod(steamId, name));
        }
    }

    public ArrayList<Mod> getMods() {
        return mods;
    }

    @NonNull
    private static ByteBuffer readModsPayload(ByteBuffer payload) {
        short maxPartSize = 127;
        byte numOfParts = (byte) (payload.asReadOnlyBuffer().get(payload.position() + 2) & 0xFF);
        ByteBuffer modsBuffer = ByteBuffer.allocate(numOfParts * maxPartSize);
        for (byte part = 0; part < numOfParts; part++)
        {
            payload.position(payload.position() + 4);
            short partSize = (short) min(maxPartSize, getIndexOfByte(payload, (byte) 0));
            byte[] partBuffer = new byte[partSize];
            payload.get(partBuffer);
            modsBuffer.put(partBuffer);
        }
        modsBuffer.flip();
        return modsBuffer;
    }

    private static ByteBuffer replaceEscapeBytes(ByteBuffer payload) {
        ByteBuffer result = ByteBuffer.allocate(payload.capacity());
        while (payload.remaining() >= 2) {
            byte byte1 = payload.get();
            byte byte2 = payload.get();

            if (byte1 == 0x1) {
                if (byte2 == 0x1) {
                    result.put((byte) 0x1);
                } else if (byte2 == 0x2) {
                    result.put((byte) 0x0);
                } else if (byte2 == 0x3) {
                    result.put((byte) 0xFF);
                } else {
                    result.put(byte1);
                    payload.position(payload.position() - 1);
                }
            } else {
                result.put(byte1);
                payload.position(payload.position() - 1);
            }
        }
        Log.i(TAG_SERVER, "payload remaining: " + payload.remaining());
        if (payload.hasRemaining())
        {
            result.put(payload.get());
        }
        result.flip();
        return result;
    }
}
