package pl.tmkd.serverz.sq.msg;

public class Utils {

    private Utils() {}

    public static byte[] merge(byte[] a, byte[] b) {
        byte [] newArray = new byte[a.length + b.length];
        System.arraycopy( a, 0, newArray, 0, a.length);
        System.arraycopy( b, 0, newArray, a.length, b.length );
        return newArray;
    }
}
