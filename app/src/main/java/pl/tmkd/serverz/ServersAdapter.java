package pl.tmkd.serverz;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import pl.tmkd.serverz.sq.Server;
import pl.tmkd.serverz.sq.ServerListener;

public class ServersAdapter extends BaseAdapter implements ServerListener {
    private final Context context;
    final ArrayList<Server> arrayList;

    public ServersAdapter(Context context, ArrayList<Server> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"ViewHolder", "SetTextI18n"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView)
            convertView = LayoutInflater.from(context).inflate(R.layout.server_item, parent, false);
        TextView playersNum = convertView.findViewById(R.id.playersNum);
        TextView name = convertView.findViewById(R.id.name);
        TextView map = convertView.findViewById(R.id.map);
        TextView time = convertView.findViewById(R.id.serverTime);
        TextView firstPerson = convertView.findViewById(R.id.isFirstPerson);
        TextView version = convertView.findViewById(R.id.version);
        LinearLayout mainLayout = convertView.findViewById(R.id.serverItemBackground);

        Server server = arrayList.get(position);
        name.setText(server.getName());
        playersNum.setText(server.getPlayersNum() + "/" + server.getMaxPlayers());
        time.setText(server.getServerTime());
        firstPerson.setText(server.isFirstPerson() ? "1pp" : "3pp");
        version.setText(server.getVersion());
        map.setText(server.getMap());
        if (server.hasRefreshFailed()) {
            mainLayout.setBackgroundResource(R.color.failed_server_background);
        } else {
            mainLayout.setBackgroundColor(Color.TRANSPARENT);
        }
        return convertView;
    }

    @Override
    public void onServerInfoRefreshed() {
        ((Activity)context).runOnUiThread(this::notifyDataSetChanged);
    }

    @Override
    public void onServerInfoRefreshFailed(Server server) {
        ((Activity)context).runOnUiThread(this::notifyDataSetChanged);
    }

    public void stopServers() {
        for (Server server : arrayList) {
            server.stop();
        }
    }

    public void startServers() {
        for (Server server : arrayList) {
            server.start();
        }
    }
}
