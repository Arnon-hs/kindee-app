package com.kindee.app;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override public void onCreate(Bundle state) {
        registerPlugin(KinDeeHealthPlugin.class);
        super.onCreate(state);
    }
}
