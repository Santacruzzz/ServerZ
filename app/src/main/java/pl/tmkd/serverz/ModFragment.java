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

import pl.tmkd.serverz.sq.Mod;

public class ModFragment extends Fragment {
    private Context baseContext;
    private View view;
    ArrayAdapter<Mod> modsAdapter;

    public ModFragment() {
        baseContext = null;
    }

    public ModFragment(Context baseContext, ArrayList<Mod> mods) {
        this.baseContext = baseContext;
        modsAdapter = new ArrayAdapter<>(baseContext, R.layout.mod_item, mods);
    }

    public void setMods(ArrayList<Mod> mods) {
        modsAdapter = new ArrayAdapter<>(baseContext, R.layout.player_item, mods);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        baseContext = context;
        SecondActivity parent = (SecondActivity) context;
        parent.updateModsFragment(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.mods_fragment, container, false);
        setAdapter();
        return view;
    }

    public void setAdapter() {
        ListView modsList = view.findViewById(R.id.listOfMods);
        modsList.setAdapter(modsAdapter);
    }

    public void update() {
        modsAdapter.notifyDataSetChanged();
    }
}
