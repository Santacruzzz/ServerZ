package pl.tmkd.serverz;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;

import pl.tmkd.serverz.sq.Server;

public class MainActivity extends Activity {
    private View mainView;
    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainView = getLayoutInflater().inflate(R.layout.layout_main, null);
        setContentView(mainView);

        textView = findViewById(R.id.textView);

        try {
            Server server = new Server("138.201.226.81", 27026);
            textView.setText(server.getAddress());
        } catch (Exception e) {
            textView.setText(e.toString());
        }
    }
}
