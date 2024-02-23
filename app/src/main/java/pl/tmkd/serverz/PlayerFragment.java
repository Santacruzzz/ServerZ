package pl.tmkd.serverz;

import static pl.tmkd.serverz.sq.Constants.TAG_MAIN;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class PlayerFragment extends Fragment {
    private View view;
    private final Context baseContext;
    List<String> players;
    ArrayAdapter<String> itemsAdapter;

    public PlayerFragment(Context baseContext) {
        this.baseContext = baseContext;
        this.players = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.players_fragment, container, false);
        createPlayersAdapter();
        return view;
    }

    public void createPlayersAdapter() {
        itemsAdapter = new ArrayAdapter<> (baseContext, R.layout.player_item, players);
        ListView listView = view.findViewById(R.id.listOfPlayers);
        listView.setAdapter(itemsAdapter);
    }

    public void setPlayers(List<String> players) {
        this.players = players;
        if (itemsAdapter != null) {
            itemsAdapter.clear();
            itemsAdapter.addAll(players);
        }
    }
}
