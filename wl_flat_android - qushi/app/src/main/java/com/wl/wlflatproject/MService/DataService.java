package com.wl.wlflatproject.MService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.wl.wlflatproject.MUtils.DateUtils;

public class DataService extends BroadcastReceiver {
    private static final String ACTION_TIME_CHANGED = "android.intent.action.TIME_TICK";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
//        if (ACTION_TIME_CHANGED.equals(action)) {
//            String s = DateUtils.getInstance().dateFormat8(System.currentTimeMillis());
//            if(DateUtils.getInstance().dateFormat8(System.currentTimeMillis()).equals("02:00")){
//                Intent intent2 = new Intent(Intent.ACTION_REBOOT);
//                intent2.putExtra("nowait", 1);
//                intent2.putExtra("interval", 1);
//                intent2.putExtra("window", 0);
//                context.sendBroadcast(intent2);
//            }
//        }
    }
}
