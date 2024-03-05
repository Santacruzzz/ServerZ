package pl.tmkd.serverz;

import static android.widget.Toast.LENGTH_SHORT;
import static pl.tmkd.serverz.sq.Constants.TAG_MAIN;
import static pl.tmkd.serverz.sq.msg.Utils.isIpAndPortInList;
import static pl.tmkd.serverz.sq.msg.Utils.isServerInList;
import static pl.tmkd.serverz.sq.msg.Utils.saveServerDataInIntent;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;


import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import pl.tmkd.serverz.sq.RefreshType;
import pl.tmkd.serverz.sq.Server;
import pl.tmkd.serverz.sq.ServerAddress;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {
    private ListView serversView;
    private ServersAdapter serversAdapter;
    private ArrayList<Server> servers;
    private File file;
    private FrameLayout emptyServers;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View mainView = getLayoutInflater().inflate(R.layout.layout_main, null);
        setContentView(mainView);
        file = new File(this.getFileStreamPath("servers.txt").toURI());
        servers = new ArrayList<>();
        serversAdapter = new ServersAdapter(this, servers);
        serversView = findViewById(R.id.idServersView);
        emptyServers = findViewById(R.id.emptyServers);
        serversView.setOnItemClickListener(this);
        serversView.setAdapter(serversAdapter);
        registerForContextMenu(serversView);
        loadServers();
        changeLayoutVisibility();
    }

    public void changeLayoutVisibility() {
        FloatingActionButton addServerButton = (FloatingActionButton) findViewById(R.id.floatingButton);
        ExtendedFloatingActionButton addFirstServerButton = (ExtendedFloatingActionButton) findViewById(R.id.extendedButton);

        if (servers.isEmpty()) {
            emptyServers.setVisibility(View.VISIBLE);
            serversView.setVisibility(View.GONE);
            addServerButton.setVisibility(View.GONE);
            addFirstServerButton.setOnClickListener(this::onNewServerButtonClick);
        } else {
            serversView.setVisibility(View.VISIBLE);
            emptyServers.setVisibility(View.GONE);
            addServerButton.setVisibility(View.VISIBLE);
            addServerButton.setOnClickListener(this::onNewServerButtonClick);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Server server = servers.get(position);
        Intent intent = new Intent(MainActivity.this, SecondActivity.class);
        saveServerDataInIntent(server, intent);
        startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        menu.add(0, view.getId(), 0, "Edit");
        menu.add(0, view.getId(), 1 , "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Dialog dialog = createDialog();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info != null) {
            if (item.getTitle() == "Edit") {
                showEditServerDialog(dialog, info.position);
            } else if (item.getTitle() == "Delete") {
                showDeleteServerDialog(dialog, info.position);
            }
        }
        return true;
    }

    public void showEditServerDialog(Dialog dialog, int serverIndex) {
        Server server = servers.get(serverIndex);
        dialog.setContentView(R.layout.server_dialog);
        EditText editIp = dialog.findViewById(R.id.editTextIp);
        EditText editPort = dialog.findViewById(R.id.editTextPort);
        Button saveButton = dialog.findViewById(R.id.saveButton);
        Button dismissButton = dialog.findViewById(R.id.dismissButton);

        dialog.show();
        editIp.setText(server.getIp());
        editPort.setText(String.valueOf(server.getPort()));

        saveButton.setOnClickListener(v -> editServer(dialog, serverIndex, editIp, editPort));
        dismissButton.setOnClickListener(v -> dialog.dismiss());
    }

    public void showDeleteServerDialog(Dialog dialog, int serverIndex) {
        dialog.setContentView(R.layout.custome_delete_dialog);
        Button yesButton = dialog.findViewById(R.id.yesButton);
        Button noButton = dialog.findViewById(R.id.noButton);
        dialog.show();

        yesButton.setOnClickListener(v -> {
            Server server = servers.get(serverIndex);
            server.stop();
            servers.remove(server);
            serversAdapter.notifyDataSetChanged();
            dialog.dismiss();
            changeLayoutVisibility();
        });

        noButton.setOnClickListener(v -> dialog.dismiss());
    }

    public void onNewServerButtonClick(View view) {
        Dialog dialog = createDialog();
        dialog.setContentView(R.layout.server_dialog);

        EditText editIp = dialog.findViewById(R.id.editTextIp);
        EditText editPort = dialog.findViewById(R.id.editTextPort);
        Button saveButton = dialog.findViewById(R.id.saveButton);
        Button dismissButton = dialog.findViewById(R.id.dismissButton);
        dialog.show();

        saveButton.setOnClickListener(view1 -> addNewServer(dialog, editIp, editPort));
        dismissButton.setOnClickListener(view2 -> dialog.dismiss());
    }

    public Dialog createDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public void addNewServer(Dialog dialog, EditText editTextIp, EditText editTextPort) {
        try {
            String serverIp = editTextIp.getText().toString();
            int serverPort = Integer.parseInt(editTextPort.getText().toString());
            Server server = new Server(serverIp, serverPort, RefreshType.INFO_ONLY);

            if (serverIp.isEmpty()) {
                throw new Exception("Empty IP address");
            }
            if (isServerInList(servers, server)) {
                throw new Exception("Server already in list");
            }
            addServerToList(server);
            dialog.dismiss();

        } catch (NumberFormatException | NetworkOnMainThreadException e) {
            Toast.makeText(this, "Incorrect IP address or port", LENGTH_SHORT).show();
            Log.e(TAG_MAIN, "Wrong data" + e);
        } catch (Exception e) {
            String message = e.getMessage();
            Toast.makeText(this, message, LENGTH_SHORT).show();
        }
    }

    public void editServer(Dialog dialog, int serverIndex, EditText editIp, EditText editPort) {
        Server server = servers.get(serverIndex);
        try {
            server.stop();
            String newIp = editIp.getText().toString();
            int newPort = Integer.parseInt(editPort.getText().toString());

            if (isIpAndPortInList(servers, newIp, newPort)) {
                throw new Exception("Server already in the list");
            }
            if (newIp.isEmpty()) {
                throw new Exception("Incorrect IP address");
            }
            editServerAddress(server, newIp, newPort);
            dialog.dismiss();


        } catch (NumberFormatException | NetworkOnMainThreadException e) {
            Toast.makeText(this, "Incorrect IP address or port", LENGTH_SHORT).show();
            Log.e(TAG_MAIN, "Wrong data: " + e);
        } catch (Exception e) {
            String message = e.getMessage();
            Toast.makeText(this, message, LENGTH_SHORT).show();
        } finally {
            editIp.setText(server.getIp());
            editPort.setText(String.valueOf(server.getPort()));
        }
    }

    public void addServerToList(Server server) {
        server.setListener(serversAdapter);
        server.start();
        servers.add(server);
        serversAdapter.notifyDataSetChanged();
        changeLayoutVisibility();
    }

    public void editServerAddress(Server server, String ip, int port) {
        server.setIp(ip);
        server.setPort(port);
        server.start();
        serversAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        serversAdapter.startServers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        serversAdapter.stopServers();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ArrayList<ServerAddress> addresses = new ArrayList<>();
        for (Server server : servers) {
            addresses.add(new ServerAddress(server.getPort(), server.getIp()));
        }
        try {
            DataStorageManager.saveData(addresses, file);
        } catch (IOException e) {
            Log.e(TAG_MAIN, "Exception while saving file: " + e);
        }
    }

    public void loadServers() {
        try {
            ArrayList<ServerAddress> addresses = DataStorageManager.readData(file);

            for (ServerAddress address : addresses) {
                Server server = new Server(address.getIp(), address.getPort(), RefreshType.INFO_ONLY);
                server.setListener(serversAdapter);
                servers.add(server);
            }
        } catch (Exception e) {
            Log.e(TAG_MAIN, "Exception while reading file: " + e);
        }
    }
}
