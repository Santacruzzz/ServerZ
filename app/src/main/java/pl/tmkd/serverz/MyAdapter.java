package pl.tmkd.serverz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MyAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<MyData> arrayList;

    public MyAdapter(Context context, ArrayList<MyData> arrayList) {
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

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.server_item, parent, false);
        TextView id = convertView.findViewById(R.id.idServer);
        TextView greet = convertView.findViewById(R.id.greet);
        id.setText(arrayList.get(position).getId());
        greet.setText(arrayList.get(position).getGreet());
        return convertView;
    }
}
