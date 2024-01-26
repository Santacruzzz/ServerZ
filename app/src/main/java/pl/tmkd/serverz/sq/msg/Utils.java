package pl.tmkd.serverz.sq.msg;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

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
    public static String getExtraValue(@NonNull List<String> extras, String name)
    {
        List<String> filtered = extras.stream().filter(s -> s.contains(name)).collect(Collectors.toList());
        if (filtered.size() == 1)
        {
            return filtered.get(0).replace(name, "");
        }
        return "";
    }
}
