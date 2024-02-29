package pl.tmkd.serverz;

import static android.widget.Toast.LENGTH_SHORT;
import static pl.tmkd.serverz.sq.Constants.TAG_MAIN;
import static pl.tmkd.serverz.sq.msg.Utils.isIpAndPortInList;
import static pl.tmkd.serverz.sq.msg.Utils.isServerNotInList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import pl.tmkd.serverz.sq.RefreshType;
import pl.tmkd.serverz.sq.Server;
import pl.tmkd.serverz.sq.ServerAddress;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener {
    private ListView listView;
    private MyAdapter adapter;
    private ArrayList<Server> arrayList;
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
        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.floatingButton);
        listView = findViewById(R.id.idListView);
        listView.setOnItemClickListener(this);
        button.setOnClickListener(this);
        listView.setAdapter(adapter);
        registerForContextMenu(listView);
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(0, v.getId(), 0, "Edit");
        menu.add(0, v.getId(), 0 , "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Dialog dialog = createDialog();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        assert info != null;
        int index = info.position;

            if (item.getTitle() == "Edit") {
                editServer(dialog, index);
            }

            else if (item.getTitle() == "Delete") {
                deleteServer(dialog, index);
            }
        return false;
    }

    public void editServer(Dialog dialog, int index) {
        dialog.setContentView(R.layout.custom_edit_dialog);
        EditText editIp = dialog.findViewById(R.id.editTextIp);
        EditText editPort = dialog.findViewById(R.id.editTextPort);
        Button b_save = dialog.findViewById(R.id.button_save);
        Button b_dismiss = dialog.findViewById(R.id.button_dismiss);

        dialog.show();
        editIp.setText(arrayList.get(index).getIp());
        editPort.setText(String.valueOf(arrayList.get(index).getPort()));

        b_save.setOnClickListener(v -> {
            isEditedServerCorrect(dialog, index, editIp, editPort);
        });

        b_dismiss.setOnClickListener(v -> dialog.dismiss());
    }

    public void deleteServer(Dialog dialog, int index) {
        dialog.setContentView(R.layout.custome_delete_dialog);
        Button b_ok = dialog.findViewById(R.id.button_ok);
        Button b_no= dialog.findViewById(R.id.button_no);
        dialog.show();

        b_ok.setOnClickListener(v -> {
            Server server = arrayList.get(index);
            server.stop();
            arrayList.remove(server);
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        b_no.setOnClickListener(v -> dialog.dismiss());
    }

    @Override
    public void onClick(View view) {
        Dialog dialog = createDialog();
        dialog.setContentView(R.layout.custom_edit_dialog);

        EditText editIp = dialog.findViewById(R.id.editTextIp);
        EditText editPort = dialog.findViewById(R.id.editTextPort);
        Button b_save = dialog.findViewById(R.id.button_save);
        Button b_dismiss = dialog.findViewById(R.id.button_dismiss);
        dialog.show();

        b_save.setOnClickListener(view1 -> {
            isNewServerCorrect(dialog, editIp, editPort);
        });

        b_dismiss.setOnClickListener(view2 -> dialog.dismiss());
    }

    public Dialog createDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public void isNewServerCorrect(Dialog dialog, EditText editIp, EditText editPort) {
        try {
            boolean isIpEmpty = editIp.getText().toString().isEmpty();
            Server server = new Server(editIp.getText().toString(), Integer.parseInt(editPort.getText().toString()), RefreshType.INFO_ONLY);
            if (isServerNotInList(arrayList, server) && !isIpEmpty) {
                addServerToList(server, dialog);
            } else {
                showIncorrectDataMessage();
            }
        } catch (NumberFormatException | NetworkOnMainThreadException e) {
            showIncorrectDataMessage();
            Log.e(TAG_MAIN, "Wrong data" + e);
        }
    }

    public void isEditedServerCorrect(Dialog dialog, int index, EditText editIp, EditText editPort) {
        try {
            Server server = arrayList.get(index);
            server.stop();
            String ip = editIp.getText().toString();
            boolean isIpEmpty = ip.isEmpty();
            int port = Integer.parseInt(editPort.getText().toString());
            if (!isIpAndPortInList(arrayList, ip, port) && !isIpEmpty) {
                editServerAddress(server, ip, port, dialog);
            } else {
                showIncorrectDataMessage();
                editIp.setText(server.getIp());
                editPort.setText(String.valueOf(server.getPort()));
            }
        } catch (NumberFormatException | NetworkOnMainThreadException e) {
            showIncorrectDataMessage();
            Log.e(TAG_MAIN, "Wrong data" + e);
        }
    }

    public void showIncorrectDataMessage() {
        String text = "Incorrect IP address or port";
        Toast.makeText(this, text, LENGTH_SHORT).show();
    }

    public void addServerToList(Server server, Dialog dialog) {
        server.setListener(adapter);
        server.start();
        arrayList.add(server);
        adapter.notifyDataSetChanged();
        dialog.dismiss();
    }

    public void editServerAddress(Server server, String ip, int port, Dialog dialog) {
        server.setIp(ip);
        server.setPort(port);
        server.start();
        adapter.notifyDataSetChanged();
        dialog.dismiss();
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
