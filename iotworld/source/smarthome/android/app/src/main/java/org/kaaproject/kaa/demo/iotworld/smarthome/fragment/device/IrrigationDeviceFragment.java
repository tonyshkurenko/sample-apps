/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.kaaproject.kaa.demo.iotworld.irrigation.IrrigationStatus;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.IrrigationDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.event.IrrigationStatusUpdatedEvent;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.TimeUtils;
import org.kaaproject.kaa.demo.iotworld.smarthome.widget.IrrigationMonthUsageWidget;
import org.kaaproject.kaa.demo.iotworld.smarthome.widget.IrrigationTankWidget;
import org.kaaproject.kaa.demo.iotworld.smarthome.widget.IrrigationTimerWidget;

import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class IrrigationDeviceFragment extends AbstractDeviceFragment<IrrigationDevice> {
    
    private static final int MIN_IRRIGATION_INTERVAL_SEC = 60;
    private static final int MAX_IRRIGATION_INTERVAL_SEC = 3600;
    
    private TextView mIrrigationStatusText;
    private SeekBar mIrrigationIntervalControlView;
    private TextView mIrrigationIntervalText;
    private IrrigationTimerWidget mLeftTillNextIrrigationView;
    private IrrigationTankWidget mLeftInTankView;
    private IrrigationMonthUsageWidget mSpentThisMonthView;
    
    public IrrigationDeviceFragment() {
        super();
    }

    public IrrigationDeviceFragment(String endpointKey) {
        super(endpointKey);
    }

    @Override
    protected int getDeviceLayout() {
        return R.layout.fragment_irrigation_device;
    }

    @Override
    public String getFragmentTag() {
        return IrrigationDeviceFragment.class.getSimpleName();
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.irrigation, menu);        
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_irrigate);
        View actionView = MenuItemCompat.getActionView(item);
        if (actionView != null) {
            Button button = (Button)actionView.findViewById(R.id.irrigateButton);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    irrigate();
                }
            });
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_irrigate) {
            irrigate();
            return true;
        } 
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void setupView(LayoutInflater inflater, View rootView) {
        mIrrigationStatusText = (TextView) rootView.findViewById(R.id.irrigationStatusText);
        mIrrigationIntervalControlView = (SeekBar) rootView.findViewById(R.id.irrigationIntervalControlView);
        mIrrigationIntervalText = (TextView) rootView.findViewById(R.id.irrigationIntervalText);
        mLeftTillNextIrrigationView = (IrrigationTimerWidget) rootView.findViewById(R.id.leftTillNextIrrigationView);
        mLeftInTankView = (IrrigationTankWidget) rootView.findViewById(R.id.leftInTankView);
        mSpentThisMonthView = (IrrigationMonthUsageWidget) rootView.findViewById(R.id.spentThisMonthView);
        mIrrigationIntervalControlView.setMax(MAX_IRRIGATION_INTERVAL_SEC - MIN_IRRIGATION_INTERVAL_SEC);
        
        mIrrigationIntervalControlView.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int irrigationInterval = MIN_IRRIGATION_INTERVAL_SEC + progress; 
                String intervalText = getResources().getString(R.string.irrigation_interval_text, 
                        TimeUtils.secondsToTimer(irrigationInterval, false));
                mIrrigationIntervalText.setText(intervalText);
                if (fromUser) {
                    mDevice.changeIrrigationInterval(irrigationInterval);
                }
            }
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
        });
    }
    
    @Override
    protected void bindDevice(boolean firstLoad) {
        super.bindDevice(firstLoad);
        IrrigationStatus irrigationStatus = mDevice.getIrrigationStatus();
        if (irrigationStatus != null) {
            mIrrigationStatusText.setText(
                    getResources().getStringArray(R.array.irrigation_status)[irrigationStatus.getIsIrrigation() ? 1 : 0]);
            if (!irrigationStatus.getIgnoreIrrigationIntervalUpdate() || firstLoad) {
                mIrrigationIntervalControlView.setProgress(irrigationStatus.getIrrigationIntervalSec() 
                        - MIN_IRRIGATION_INTERVAL_SEC);
            }
            mLeftTillNextIrrigationView.setMaxTimeSec(irrigationStatus.getIrrigationIntervalSec());
            mLeftTillNextIrrigationView.setValue(irrigationStatus.getTimeToNextIrrigationMs()/1000);
            mLeftInTankView.setValue(((float)irrigationStatus.getRemainingWater())/1000f);
            mSpentThisMonthView.setValue(((float)irrigationStatus.getMonthlySpentWater())/1000f);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(IrrigationStatusUpdatedEvent irrigationStatusUpdatedEvent) {
        if (mEndpointKey != null && mEndpointKey.equals(irrigationStatusUpdatedEvent.getEndpointKey())) {
            IrrigationStatus irrigationStatus = mDevice.getIrrigationStatus();
            if (irrigationStatus != null) {
                mLeftTillNextIrrigationView.setValue(irrigationStatus.getTimeToNextIrrigationMs()/1000);
            }
        }
    }
    
    private void irrigate() {
        mDevice.startIrrigation();
    }

}
