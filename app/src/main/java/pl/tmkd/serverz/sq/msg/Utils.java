package pl.tmkd.serverz.sq.msg;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import pl.tmkd.serverz.sq.Server;

public class Utils {

    private Utils() {}

    @NonNull
    public static byte[] merge(@NonNull byte[] a, @NonNull byte[] b) {
        byte[] newArray = new byte[a.length + b.length];
        System.arraycopy(a, 0, newArray, 0, a.length);
        System.arraycopy(b, 0, newArray, a.length, b.length );
        return newArray;
    }

    @NonNull
    public static byte[] right(@NonNull byte[] inputList, int numOfElements) {
        assert(inputList.length >= numOfElements);

        byte[] newArray = new byte[numOfElements];
        int srcPos = inputList.length - numOfElements;
        System.arraycopy(inputList, srcPos, newArray, 0, numOfElements);
        return newArray;
    }

    @NonNull
    public static String readString(@NonNull ByteBuffer buffer) {
        StringBuilder stringBuilder = new StringBuilder();

        while (buffer.hasRemaining()) {
            byte currentByte = buffer.get();
            if (currentByte == 0) {
                break;
            }
            stringBuilder.append((char) currentByte);
        }
        return stringBuilder.toString();
    }

    @NonNull
    public static String readSizedString(@NonNull ByteBuffer buffer) {
        StringBuilder stringBuilder = new StringBuilder();
        int stringSize = buffer.get() & 0xFF;
        for (int i = 0; i < stringSize; i++) {
            stringBuilder.append((char) buffer.get());
        }
        return stringBuilder.toString();
    }

    @NonNull
    public static String getExtraValue(@NonNull List<String> extras, String name)
    {
        List<String> filtered = extras.stream().filter(s -> s.contains(name)).collect(Collectors.toList());
        if (filtered.size() == 1)
        {
            return filtered.get(0).replace(name, "");
        }
        return "";
    }

    @NonNull
    public static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = (duration.toMinutes() % 60);
        long seconds = (duration.getSeconds() % 60);

        ArrayList<String> result = new ArrayList<>();
        if (hours > 0) {
            result.add(String.format(Locale.getDefault(), "%dh", hours));
        }
        if (minutes > 0) {
            result.add(String.format(Locale.getDefault(), "%dm", minutes));
        }
        if (seconds > 0) {
            result.add(String.format(Locale.getDefault(), "%ds", seconds));
        }
        if (result.size() == 0) {
            return "<1m";
        }

        return String.join(" ", result);
    }

    public static boolean isServerInList(ArrayList<Server> servers, Server server) {
        for (Server item : servers) {
            if (item.equals(server)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isIpAndPortInList(ArrayList<Server> servers, String ip, int port) {
        for (Server item : servers) {
            if ((item.getIp()).equals(ip) && item.getPort() == port) {
                return true;
            }
        }
        return false;
    }

    public static int getIndexOfByte(ByteBuffer where, byte what) {
        int padding = where.position();
        ByteBuffer readOnlyBuffer = where.asReadOnlyBuffer();
        for (int i = 0; i < where.remaining(); i++) {
            if (readOnlyBuffer.get(padding + i) == what) {
                return i;
            }
        }
        return where.remaining();
    }

    public static long getBytes(ByteBuffer bytes, int num) {
        long result = 0;
        byte[] byteArray = new byte[num];
        bytes.get(byteArray);
        for (int i = byteArray.length - 1; i >= 0; i--) {
            result = (result << 8) | (byteArray[i] & 0xFF);
        }
        return result;
    }
}
