
package com.jutaol.nearby.dinner.awareness.barrier;

import com.jutaol.nearby.dinner.R;
import com.jutaol.nearby.dinner.Utils;
import com.jutaol.nearby.dinner.logger.LogView;
import com.huawei.hms.kit.awareness.barrier.AwarenessBarrier;
import com.huawei.hms.kit.awareness.barrier.BarrierStatus;
import com.huawei.hms.kit.awareness.barrier.WifiBarrier;
import com.huawei.hms.kit.awareness.status.WifiStatus;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;

public class WifiBarrierActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String KEEPING_BARRIER_LABEL = "keeping wifi barrier label";

    private LogView mLogView;

    private ScrollView mScrollView;

    private PendingIntent mPendingIntent;

    private WifiBarrierReceiver mBarrierReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_barrier);
        initView();
        String barrierReceiverAction = getApplication().getPackageName() + "WIFI_BARRIER_RECEIVER_ACTION";
        Intent intent = new Intent(barrierReceiverAction);
        // You can also create PendingIntent with getActivity() or getService().
        // This depends on what action you want Awareness Kit to trigger when the barrier status changes.
        mPendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Register a broadcast receiver to receive the broadcast sent by Awareness Kit when the barrier status changes.
        mBarrierReceiver = new WifiBarrierReceiver();
        registerReceiver(mBarrierReceiver, new IntentFilter(barrierReceiverAction));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBarrierReceiver != null) {
            unregisterReceiver(mBarrierReceiver);
        }
    }

    private void initView() {
        findViewById(R.id.add_wifiBarrier_keeping).setOnClickListener(this);
        findViewById(R.id.delete_wifi_barrier).setOnClickListener(this);
        findViewById(R.id.clear_wifi_barrier_log).setOnClickListener(this);
        mLogView = findViewById(R.id.logView);
        mScrollView = findViewById(R.id.log_scroll);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_wifiBarrier_keeping:
                AwarenessBarrier keepingConnectedBarrier = WifiBarrier.keeping(WifiStatus.CONNECTED, null, null);
                Utils.addBarrier(this, KEEPING_BARRIER_LABEL, keepingConnectedBarrier, mPendingIntent);
                break;
            case R.id.delete_wifi_barrier:
                Utils.deleteBarrier(this, KEEPING_BARRIER_LABEL);
                break;
            case R.id.clear_wifi_barrier_log:
                mLogView.setText("");
                break;
            default:
                break;
        }
    }

    final class WifiBarrierReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            BarrierStatus barrierStatus = BarrierStatus.extract(intent);
            String label = barrierStatus.getBarrierLabel();
            int barrierPresentStatus = barrierStatus.getPresentStatus();
            if (!KEEPING_BARRIER_LABEL.equals(label)) {
                return;
            }
            if (barrierPresentStatus == BarrierStatus.TRUE) {
                mLogView.printLog("The wifi is connected.");
            } else if (barrierPresentStatus == BarrierStatus.FALSE) {
                mLogView.printLog("The wifi is disconnected.");
            } else {
                mLogView.printLog("The wifi status is unknown.");
            }
            mScrollView.postDelayed(() -> mScrollView.smoothScrollTo(0, mScrollView.getBottom()), 200);
        }
    }
}
