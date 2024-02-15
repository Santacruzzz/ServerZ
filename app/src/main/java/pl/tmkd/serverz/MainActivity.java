package pl.tmkd.serverz;

import static pl.tmkd.serverz.sq.Constants.TAG_MAIN;
import static pl.tmkd.serverz.sq.msg.Utils.isServerNotInList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.Nullable;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.tmkd.serverz.sq.RefreshType;
import pl.tmkd.serverz.sq.Server;
import pl.tmkd.serverz.sq.ServerAddress;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener, AdapterView.OnItemLongClickListener {
    private ListView listView;
    private MyAdapter adapter;
    private ArrayList<Server> arrayList;
    private EditText editTextIp;
    private EditText editTextPort;
    private  File file;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View mainView = getLayoutInflater().inflate(R.layout.layout_main, null);
        setContentView(mainView);
        ArrayList<ServerAddress> addressList = new ArrayList<>();
        file = new File(this.getFileStreamPath("servers.txt").toURI());
        arrayList = new ArrayList<>();
        adapter = new MyAdapter(this, arrayList);
        Button button = (Button) findViewById(R.id.button);
        listView = findViewById(R.id.idListView);
        editTextIp = findViewById(R.id.editTextIp);
        editTextPort = findViewById(R.id.editTextPort);
        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);
        button.setOnClickListener(this);
        listView.setAdapter(adapter);
        loadServers();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(MainActivity.this, SecondActivity.class);
        intent.putExtra("ip", arrayList.get(position).getIp());
        intent.putExtra("port", arrayList.get(position).getPort());
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Do you want to remove " + arrayList.get(position) + "from list?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    arrayList.get(position).stop();
                    arrayList.remove(position);
                    adapter.notifyDataSetChanged();
                }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).create().show();
        return true;
    }

    @Override
    public void onClick(View v) {
        try {
            Server server = new Server(editTextIp.getText().toString(), Integer.parseInt(editTextPort.getText().toString()), RefreshType.INFO_ONLY);
            if (isServerNotInList(arrayList, server)) {
                server.setListener(adapter);
                server.start();
                arrayList.add(server);
                adapter.notifyDataSetChanged();
                editTextIp.setText("");
                editTextPort.setText("");
            }
        } catch (NumberFormatException e) {
            Log.e(TAG_MAIN, "Wrong data" + e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.startServers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        adapter.stopServers();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ArrayList<ServerAddress> addressList = new ArrayList<>();
        for (Server server : arrayList) {
            addressList.add(new ServerAddress(server.getPort(), server.getIp()));
        }
        try {
            DataStorageManager.saveData(addressList, file);
        } catch (IOException e) {
            Log.e(TAG_MAIN, "Exception while saving file: " + e);
        }
    }

    public void loadServers() {
        try {
            ArrayList<ServerAddress> addresses = DataStorageManager.readData(file);

            for (ServerAddress address : addresses) {
                Server server = new Server(address.getIp(), address.getPort(), RefreshType.INFO_ONLY);
                server.setListener(adapter);
                arrayList.add(server);
            }
        } catch (Exception e) {
            Log.e(TAG_MAIN, String.valueOf(e));
        }
    }
}
