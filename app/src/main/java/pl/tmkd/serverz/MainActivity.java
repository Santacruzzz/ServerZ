package pl.tmkd.serverz;

import static android.widget.Toast.LENGTH_SHORT;
import static pl.tmkd.serverz.sq.Constants.TAG_MAIN;
import static pl.tmkd.serverz.sq.msg.Utils.isIpAndPortInList;
import static pl.tmkd.serverz.sq.msg.Utils.isServerInList;
import static pl.tmkd.serverz.sq.msg.Utils.saveServerDataInIntent;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
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

public class MainActivity extends Activity {
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
        serversView.setOnItemClickListener(this::onServerClick);
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

    public void onServerClick(AdapterView<?> parent, View view, int position, long id) {
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
        EditText sunriseTime = dialog.findViewById(R.id.sunriseTime);
        EditText sunsetTime = dialog.findViewById(R.id.sunsetTime);
        Button saveButton = dialog.findViewById(R.id.saveButton);
        Button dismissButton = dialog.findViewById(R.id.dismissButton);

        dialog.show();
        editIp.setText(server.getIp());
        editPort.setText(String.valueOf(server.getPort()));
        sunriseTime.setText(String.valueOf(server.getSunriseTime().getHour()));
        sunsetTime.setText(String.valueOf(server.getSunsetTime().getHour()));

        saveButton.setOnClickListener(v -> editServer(dialog, serverIndex, editIp, editPort, sunriseTime, sunsetTime));
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
        EditText sunriseTime = dialog.findViewById(R.id.sunriseTime);
        EditText sunsetTime = dialog.findViewById(R.id.sunsetTime);
        Button saveButton = dialog.findViewById(R.id.saveButton);
        Button dismissButton = dialog.findViewById(R.id.dismissButton);
        sunriseTime.setVisibility(View.GONE);
        sunsetTime.setVisibility(View.GONE);
        dialog.show();

        saveButton.setOnClickListener(view1 -> addNewServer(dialog, editIp, editPort, sunriseTime, sunsetTime));
        dismissButton.setOnClickListener(view2 -> dialog.dismiss());
    }

    public Dialog createDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public void addNewServer(Dialog dialog, EditText editTextIp, EditText editTextPort,
                             EditText editTextSunriseTime, EditText editTextSunsetTime) {
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

    public void editServer(Dialog dialog, int serverIndex, EditText editTextIp, EditText editTextPort,
                           EditText editTextSunriseTime, EditText editTextSunsetTime) {
        Server server = servers.get(serverIndex);
        try {
            server.stop();
            String loadedIp = editTextIp.getText().toString();
            String loadedPort = editTextPort.getText().toString();
            String loadedSunriseTime = editTextSunriseTime.getText().toString();
            String loadedSunsetTime = editTextSunsetTime.getText().toString();

            if (loadedIp.isEmpty()) {
                throw new Exception("Empty IP address");
            }
            if (loadedPort.isEmpty()) {
                throw new Exception("Empty port number");
            }
            if (loadedSunriseTime.isEmpty()) {
                throw new Exception("Empty sunrise");
            }
            if (loadedSunsetTime.isEmpty()) {
                throw new Exception("Empty sunset");
            }
            boolean ipAndPortNotChanged = loadedIp.equals(server.getIp()) &&
                    Integer.parseInt(loadedPort) == server.getPort();
            boolean isIpAndPortAlreadyOnTheList =
                    isIpAndPortInList(servers, loadedIp, Integer.parseInt(loadedPort));
            if (isIpAndPortAlreadyOnTheList && !ipAndPortNotChanged) {
                throw new Exception("Server already in the list");
            }
            editServerAddress(server, loadedIp, Integer.parseInt(loadedPort),
                    Integer.parseInt(loadedSunriseTime), Integer.parseInt(loadedSunsetTime));
            dialog.dismiss();

        } catch (NumberFormatException | NetworkOnMainThreadException e) {
            Toast.makeText(this, "Incorrect IP address or port number", LENGTH_SHORT).show();
            Log.e(TAG_MAIN, "Wrong data: " + e);
        } catch (Exception e) {
            String message = e.getMessage();
            Toast.makeText(this, message, LENGTH_SHORT).show();
            editTextIp.setText(server.getIp());
            editTextPort.setText(String.valueOf(server.getPort()));
            editTextSunriseTime.setText(String.valueOf(server.getSunriseTime().getHour()));
            editTextSunsetTime.setText(String.valueOf(server.getSunsetTime().getHour()));
        }
    }
    
    public void addServerToList(Server server) {
        server.setListener(serversAdapter);
        server.start();
        servers.add(server);
        serversAdapter.notifyDataSetChanged();
        changeLayoutVisibility();
    }

    public void editServerAddress(Server server, String ip, int port, int newSunriseTime, int newSunsetTime) {
        server.setIp(ip);
        server.setPort(port);
        server.setSunriseTime(newSunriseTime);
        server.setSunsetTime(newSunsetTime);
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
            addresses.add(new ServerAddress(server.getPort(), server.getIp().toString(),
                    server.getSunriseTime().getHour(), server.getSunsetTime().getHour()));
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
                server.setSunriseTime(address.getSunriseTime());
                server.setSunsetTime(address.getSunsetTime());
                server.setListener(serversAdapter);
                servers.add(server);
            }
        } catch (Exception e) {
            Log.e(TAG_MAIN, "Exception while reading file: " + e);
        }
    }
}
