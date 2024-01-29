package pl.tmkd.serverz;

import static pl.tmkd.serverz.sq.Constants.SQ_TAG;
import static pl.tmkd.serverz.sq.msg.Utils.isServerNotInList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import pl.tmkd.serverz.sq.Server;
import pl.tmkd.serverz.sq.ServerListener;

public class MainActivity extends Activity implements View.OnClickListener {
    private ListView listView;
    private MyAdapter adapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View mainView = getLayoutInflater().inflate(R.layout.layout_main, null);
        setContentView(mainView);
        ArrayList<Server> arrayList = new ArrayList<>();
        adapter = new MyAdapter(this, arrayList);
        Button button = (Button) findViewById(R.id.button);
        listView = findViewById(R.id.idListView);
        EditText editTextIp = findViewById(R.id.editTextIp);
        EditText editTextPort = findViewById(R.id.editTextPort);

       button.setOnClickListener(v -> {
           Server server = new Server(editTextIp.getText().toString(), Integer.parseInt(editTextPort.getText().toString()));
           if (isServerNotInList(arrayList, server)) {
               server.setListener(adapter);
               arrayList.add(server);
               listView.setAdapter(adapter);
               adapter.notifyDataSetChanged();
           }
       });

        try {
            Server server = new Server("138.201.226.81", 27026);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public void onClick(View v) {

    }
}
