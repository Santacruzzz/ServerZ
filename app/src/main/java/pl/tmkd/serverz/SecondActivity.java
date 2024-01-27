package pl.tmkd.serverz;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class SecondActivity extends MainActivity {
    private View view;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.active_server);
    }
}