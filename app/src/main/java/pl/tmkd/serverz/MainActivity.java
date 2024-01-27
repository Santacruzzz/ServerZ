package pl.tmkd.serverz;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import pl.tmkd.serverz.sq.Server;

public class MainActivity extends Activity implements View.OnClickListener {
    private TextView textView;
    private View mainView;
    private ListView listView;
    ArrayList<MyData> arrayList = new ArrayList<>();
    private MyAdapter adapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainView = getLayoutInflater().inflate(R.layout.layout_main, null);
        setContentView(mainView);
        listView = findViewById(R.id.idListView);

        arrayList.add(new MyData("1", "Siemano"));
        arrayList.add(new MyData("2", "Eluwina"));
        arrayList.add(new MyData("3", "Siemanderkoo"));

        adapter = new MyAdapter(this, arrayList);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

//        button.setOnClickListener(v -> {
//
//        });

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
