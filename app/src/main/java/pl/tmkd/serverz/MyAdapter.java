package pl.tmkd.serverz;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import pl.tmkd.serverz.sq.Server;
import pl.tmkd.serverz.sq.ServerListener;

public class MyAdapter extends BaseAdapter implements ServerListener {
    private final Context context;
    private final ArrayList<Server> arrayList;

    public MyAdapter(Context context, ArrayList<Server> arrayList) {
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
        convertView = LayoutInflater.from(context).inflate(R.layout.server_item, parent, false);
        TextView playersNum = convertView.findViewById(R.id.playersNum);
        TextView maxPlayers = convertView.findViewById(R.id.maxPlayers);
        TextView name = convertView.findViewById(R.id.name);

        name.setText(arrayList.get(position).getName());
        playersNum.setText(Integer.toString(arrayList.get(position).getPlayersNum()));
        maxPlayers.setText(Integer.toString(arrayList.get(position).getMaxPlayers()));
        return convertView;
    }

    @Override
    public void onServerInfoRefreshed(Server server) {
        ((Activity)context).runOnUiThread(this::notifyDataSetChanged);
    }
}
