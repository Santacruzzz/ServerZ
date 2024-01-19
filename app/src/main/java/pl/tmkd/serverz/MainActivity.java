package pl.tmkd.serverz;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class MainActivity extends Activity {
    private View mainView;
    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainView = getLayoutInflater().inflate(R.layout.layout_main, null);
        setContentView(mainView);

        textView = findViewById(R.id.textView);
        textView.setText("Siemano");
    }
}
