package pl.tmkd.serverz;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import pl.tmkd.serverz.sq.Mod;

public class ModFragment extends Fragment {
    private final Context baseContex;
    private View view;
    ArrayAdapter<Mod> modsAdapter;
    ArrayList<Mod> mods;

    public ModFragment(Context baseContext) {
        this.baseContex = baseContext;
        this.mods = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.mods_fragment, container, false);
        createModAdapter();
        return view;
    }

    public void createModAdapter() {
        modsAdapter = new ArrayAdapter<>(baseContex, R.layout.mod_item, mods);
        ListView modesList = view.findViewById(R.id.listOfMods);
        modesList.setAdapter(modsAdapter);
    }

    public void setMods(ArrayList<Mod> mods) {
        this.mods = mods;
        if (modsAdapter != null) {
            modsAdapter.clear();
            modsAdapter.addAll(mods);
        }
    }
}
