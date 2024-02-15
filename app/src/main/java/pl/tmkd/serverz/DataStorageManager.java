package pl.tmkd.serverz;


import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import pl.tmkd.serverz.sq.ServerAddress;

public class DataStorageManager {

    public static void saveData(ArrayList<ServerAddress> addressList, File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(addressList);
        oos.close();
    }

    public static ArrayList<ServerAddress> readData(File file) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        ArrayList<ServerAddress> addresses = (ArrayList<ServerAddress>) ois.readObject();
        ois.close();
        return addresses;
    }
}
