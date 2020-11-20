/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.jutaol.nearby.dinner.awareness.barrier;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.kit.awareness.Awareness;
import com.huawei.hms.kit.awareness.capture.BeaconStatusResponse;
import com.jutaol.nearby.dinner.R;
import com.jutaol.nearby.dinner.Utils;
import com.jutaol.nearby.dinner.logger.LogView;
import com.huawei.hms.kit.awareness.barrier.AwarenessBarrier;
import com.huawei.hms.kit.awareness.barrier.BarrierStatus;
import com.huawei.hms.kit.awareness.barrier.BeaconBarrier;
import com.huawei.hms.kit.awareness.status.BeaconStatus;

import java.util.List;

public class BeaconBarrierActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String DISCOVER_BARRIER_LABEL = "discover beacon barrier label";
    private static final String KEEP_BARRIER_LABEL = "keep beacon barrier label";
    private static final String MISSED_BARRIER_LABEL = "missed beacon barrier label";
    private LogView mLogView;
    private ScrollView mScrollView;
    private PendingIntent mPendingIntent;
    private BeaconBarrierReceiver mBarrierReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_barrier);
        initView();

        final String barrierReceiverAction = getApplication().getPackageName() + "BEACON_BARRIER_RECEIVER_ACTION";
        Intent intent = new Intent(barrierReceiverAction);
        // You can also create PendingIntent with getActivity() or getService().
        // This depends on what action you want Awareness Kit to trigger when the barrier status changes.
        mPendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Register a broadcast receiver to receive the broadcast sent by Awareness Kit when the barrier status changes.
        mBarrierReceiver = new BeaconBarrierReceiver();
        registerReceiver(mBarrierReceiver, new IntentFilter(barrierReceiverAction));
    }

    private void initView() {
        findViewById(R.id.add_beaconBarrier_discover).setOnClickListener(this);
        findViewById(R.id.add_beaconBarrier_keep).setOnClickListener(this);
        findViewById(R.id.add_beaconBarrier_missed).setOnClickListener(this);
        findViewById(R.id.delete_barrier).setOnClickListener(this);
        findViewById(R.id.clear_log).setOnClickListener(this);
        mLogView = findViewById(R.id.logView);
        mScrollView = findViewById(R.id.log_scroll);
    }

    @Override
    public void onClick(View v) {
        String namespace = "dev736430079244684843";
        String type = "shop";
        // byte[] content = new byte[]{'s', 'a', 'm', 'p', 'l', 'e'};
        BeaconStatus.Filter filter = BeaconStatus.Filter.match(namespace, type
        // , content
        );

        switch (v.getId()) {
            case R.id.add_beaconBarrier_discover:
                AwarenessBarrier discoverBeaconBarrier = BeaconBarrier.discover(filter);
                Utils.addBarrier(this, DISCOVER_BARRIER_LABEL, discoverBeaconBarrier, mPendingIntent);
                break;

            case R.id.add_beaconBarrier_keep:
                AwarenessBarrier keepBeaconBarrier = BeaconBarrier.keep(filter);
                Utils.addBarrier(this, KEEP_BARRIER_LABEL, keepBeaconBarrier, mPendingIntent);
                break;

            case R.id.add_beaconBarrier_missed:
                AwarenessBarrier missedBeaconBarrier = BeaconBarrier.missed(filter);
                Utils.addBarrier(this, MISSED_BARRIER_LABEL, missedBeaconBarrier, mPendingIntent);
                break;

            case R.id.delete_barrier:
                Utils.deleteBarrier(this, mPendingIntent);
                break;

            case R.id.clear_log:
                mLogView.setText("");
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBarrierReceiver != null) {
            unregisterReceiver(mBarrierReceiver);
        }
    }

    private void getBeaconStatus() {
        String namespace = "dev736430079244684843";
        String type = "shop";
//      byte[] content = new byte[] {'s', 'a', 'm', 'p', 'l', 'e'};
        BeaconStatus.Filter filter = BeaconStatus.Filter.match(namespace, type
                // , content
        );
        Awareness.getCaptureClient(this)
                .getBeaconStatus(filter)
                .addOnSuccessListener(new OnSuccessListener<BeaconStatusResponse>() {
                    @Override
                    public void onSuccess(BeaconStatusResponse beaconStatusResponse) {
                        List<BeaconStatus.BeaconData> beaconDataList =
                                beaconStatusResponse.getBeaconStatus().getBeaconData();
                        if (beaconDataList != null && beaconDataList.size() != 0) {
                            int i = 1;
                            StringBuilder builder = new StringBuilder();
                            for (BeaconStatus.BeaconData beaconData : beaconDataList) {
                                builder.append("Beacon Data ").append(i);
                                builder.append(" namespace:").append(beaconData.getNamespace());
                                builder.append(",type:").append(beaconData.getType());
                                builder.append(",content:").append(new String(beaconData.getContent()));
                                builder.append(",beaconId:").append(beaconData.getBeaconId());
                                builder.append(". ");
                                i++;
                            }
                            mLogView.printLog(builder.toString());
                        } else {
                            mLogView.printLog("No beacon matches filters nearby.");
                        }
//                        scrollToBottom();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        mLogView.printLog("Failed to get beacon status.");
//                        Log.e(TAG, "Failed to get beacon status.", e);
                    }
                });
    }


    final class BeaconBarrierReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            BarrierStatus barrierStatus = BarrierStatus.extract(intent);
            String label = barrierStatus.getBarrierLabel();
            int barrierPresentStatus = barrierStatus.getPresentStatus();
            switch (label) {
                case DISCOVER_BARRIER_LABEL:
                    if (barrierPresentStatus == BarrierStatus.TRUE) {
                        mLogView.printLog("A beacon matching the filters is found.");
                        getBeaconStatus();
                    } else if (barrierPresentStatus == BarrierStatus.FALSE) {
                        mLogView.printLog("The discover beacon barrier status is false.");
                    } else {
                        mLogView.printLog("The beacon status is unknown.");
                    }
                    break;

                case KEEP_BARRIER_LABEL:
                    if (barrierPresentStatus == BarrierStatus.TRUE) {
                        mLogView.printLog("A beacon matching the filters is found but not missed.");
                    } else if (barrierPresentStatus == BarrierStatus.FALSE) {
                        mLogView.printLog("No beacon matching the filters is found.");
                    } else {
                        mLogView.printLog("The beacon status is unknown.");
                    }
                    break;

                case MISSED_BARRIER_LABEL:
                    if (barrierPresentStatus == BarrierStatus.TRUE) {
                        mLogView.printLog("A beacon matching the filters is missed.");
                    } else if (barrierPresentStatus == BarrierStatus.FALSE) {
                        mLogView.printLog("The missed beacon barrier status is false.");
                    } else {
                        mLogView.printLog("The beacon status is unknown.");
                    }
                    break;

                default:
                    break;
            }
            mScrollView.postDelayed(()-> mScrollView.smoothScrollTo(0,mScrollView.getBottom()),200);
        }
    }
}
