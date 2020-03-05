package com.wl.wlflatproject.MService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wl.wlflatproject.Activity.MainActivity;

public class BootUpService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction().toString();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent intent1 = new Intent(context, MainActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        }
    }
}
