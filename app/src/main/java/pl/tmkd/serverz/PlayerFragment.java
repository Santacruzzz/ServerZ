package pl.tmkd.serverz;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import pl.tmkd.serverz.sq.msg.Player;

public class PlayerFragment extends Fragment {
    private View view;
    private Context baseContext;
    ArrayAdapter<Player> playersAdapter;

    public PlayerFragment() {
        baseContext = null;
    }

    public PlayerFragment(Context baseContext, ArrayList<Player> players) {
        this.baseContext = baseContext;
        playersAdapter = new ArrayAdapter<> (baseContext, R.layout.player_item, players);
    }

    public void setPlayers(ArrayList<Player> players) {
        playersAdapter = new ArrayAdapter<> (baseContext, R.layout.player_item, players);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        baseContext = context;
        SecondActivity parent = (SecondActivity) context;
        parent.updatePlayersFragment(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.players_fragment, container, false);
        setAdapter();
        return view;
    }

    public void setAdapter() {
        ListView listView = view.findViewById(R.id.listOfPlayers);
        listView.setAdapter(playersAdapter);
    }

    public void update() {
        playersAdapter.notifyDataSetChanged();
    }
}
