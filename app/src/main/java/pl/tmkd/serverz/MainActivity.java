package pl.tmkd.serverz;

import static pl.tmkd.serverz.sq.Constants.SQ_TAG;
import static pl.tmkd.serverz.sq.msg.Utils.isServerNotInList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Objects;

import pl.tmkd.serverz.sq.Server;
import pl.tmkd.serverz.sq.ServerListener;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener {
    private ListView listView;
    private MyAdapter adapter;
    private ArrayList<Server> arrayList;
    private EditText editTextIp;
    private EditText editTextPort;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View mainView = getLayoutInflater().inflate(R.layout.layout_main, null);
        setContentView(mainView);
        arrayList = new ArrayList<>();
        adapter = new MyAdapter(this, arrayList);
        Button button = (Button) findViewById(R.id.button);
        listView = findViewById(R.id.idListView);
        editTextIp = findViewById(R.id.editTextIp);
        editTextPort = findViewById(R.id.editTextPort);
        listView.setOnItemClickListener(this);
        button.setOnClickListener(this);
        listView.setAdapter(adapter);

        addTestServers();
    }

    private void addTestServers() {
        Server testServer = new Server("138.201.226.81", 27026);
        testServer.setListener(adapter);
        arrayList.add(testServer);
        testServer.start();

        Server testServer2 = new Server("185.207.214.32", 2307);
        testServer2.setListener(adapter);
        arrayList.add(testServer2);
        testServer2.start();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(MainActivity.this, SecondActivity.class);
        intent.putExtra("ip", arrayList.get(position).getIp());
        intent.putExtra("port", arrayList.get(position).getPort());
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        try {
            Server server = new Server(editTextIp.getText().toString(), Integer.parseInt(editTextPort.getText().toString()));
            if (isServerNotInList(arrayList, server)) {
                server.setListener(adapter);
                arrayList.add(server);
                adapter.notifyDataSetChanged();
            }
        }
        catch (NumberFormatException e) {
            Log.e(SQ_TAG, "Incorrect data");
        }
    }
}
