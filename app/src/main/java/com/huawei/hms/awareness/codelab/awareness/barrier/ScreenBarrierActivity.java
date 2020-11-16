
package com.jutaol.nearby.dinner.awareness.barrier;

import com.jutaol.nearby.dinner.R;
import com.jutaol.nearby.dinner.Utils;
import com.jutaol.nearby.dinner.logger.LogView;
import com.huawei.hms.kit.awareness.barrier.AwarenessBarrier;
import com.huawei.hms.kit.awareness.barrier.BarrierStatus;
import com.huawei.hms.kit.awareness.barrier.ScreenBarrier;
import com.huawei.hms.kit.awareness.status.ScreenStatus;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;

public class ScreenBarrierActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String KEEPING_BARRIER_LABEL = "keeping screen barrier label";

    private LogView mLogView;

    private ScrollView mScrollView;

    private PendingIntent mPendingIntent;

    private ScreenBarrierReceiver mBarrierReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_barrier);
        initView();
        String barrierReceiverAction = getApplication().getPackageName() + "SCREEN_BARRIER_RECEIVER_ACTION";
        Intent intent = new Intent(barrierReceiverAction);
        // You can also create PendingIntent with getActivity() or getService().
        // This depends on what action you want Awareness Kit to trigger when the barrier status changes.
        mPendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Register a broadcast receiver to receive the broadcast sent by Awareness Kit when the barrier status changes.
        mBarrierReceiver = new ScreenBarrierReceiver();
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
        findViewById(R.id.add_screenBarrier_keeping).setOnClickListener(this);
        findViewById(R.id.delete_screen_barrier).setOnClickListener(this);
        findViewById(R.id.clear_screen_barrier_log).setOnClickListener(this);
        mLogView = findViewById(R.id.logView);
        mScrollView = findViewById(R.id.log_scroll);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_screenBarrier_keeping:
                AwarenessBarrier keepingScreenOnBarrier = ScreenBarrier.keeping(ScreenStatus.SCREEN_ON);
                Utils.addBarrier(this, KEEPING_BARRIER_LABEL, keepingScreenOnBarrier, mPendingIntent);
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

    final class ScreenBarrierReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            BarrierStatus barrierStatus = BarrierStatus.extract(intent);
            String label = barrierStatus.getBarrierLabel();
            int barrierPresentStatus = barrierStatus.getPresentStatus();
            if (!KEEPING_BARRIER_LABEL.equals(label)) {
                return;
            }
            if (barrierPresentStatus == BarrierStatus.TRUE) {
                mLogView.printLog("The screen is on screen.");
            } else if (barrierPresentStatus == BarrierStatus.FALSE) {
                mLogView.printLog("The screen is off screen.");
            } else {
                mLogView.printLog("The screen status is unknown.");
            }
            mScrollView.postDelayed(() -> mScrollView.smoothScrollTo(0, mScrollView.getBottom()), 200);
        }
    }
}
