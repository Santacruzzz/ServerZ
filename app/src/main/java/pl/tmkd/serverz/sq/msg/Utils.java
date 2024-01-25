package pl.tmkd.serverz.sq.msg;

public class Utils {

    private Utils() {}

    public static byte[] merge(byte[] a, byte[] b) {
        byte[] newArray = new byte[a.length + b.length];
        System.arraycopy(a, 0, newArray, 0, a.length);
        System.arraycopy(b, 0, newArray, a.length, b.length );
        return newArray;
    }

    public static byte[] right(byte[] inputList, int numOfElements) {
        byte[] newArray = new byte[numOfElements];
        int srcPos = inputList.length - numOfElements;
        System.arraycopy(inputList, srcPos, newArray, 0, numOfElements);
        return newArray;
    }
}
