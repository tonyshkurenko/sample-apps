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

package org.kaaproject.kaa.demo.iotworld.smarthome.data;

import GeoFencingEventClassFamilyV2.IrrigationEventClassFamilyV2;
import org.greenrobot.eventbus.EventBus;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.demo.iotworld.device.DeviceChangeNameRequest;
import org.kaaproject.kaa.demo.iotworld.device.DeviceInfoRequest;
import org.kaaproject.kaa.demo.iotworld.device.DeviceStatusSubscriptionRequest;
import org.kaaproject.kaa.demo.iotworld.irrigation.IrrigationControlRequest;
import org.kaaproject.kaa.demo.iotworld.irrigation.IrrigationStatus;
import org.kaaproject.kaa.demo.iotworld.irrigation.IrrigationStatusUpdate;
import org.kaaproject.kaa.demo.iotworld.irrigation.StartIrrigationRequest;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.event.IrrigationStatusUpdatedEvent;

import android.os.Handler;
import android.os.Looper;

public class IrrigationDevice extends AbstractDevice implements IrrigationEventClassFamilyV2.Listener {

    private final IrrigationEventClassFamilyV2 mIrrigationEventClassFamily;
    
    private IrrigationStatus mIrrigationStatus;
    private Handler mHandler;    
    private CountDownTask mCountDownTask;
    
    public IrrigationDevice(String endpointKey, DeviceStore deviceStore, KaaClient client, EventBus eventBus) {
        super(endpointKey, deviceStore, client, eventBus);
        mIrrigationEventClassFamily = mClient.getEventFamilyFactory().getIrrigationEventClassFamilyV2();
    }
    
    @Override
    public void initDevice() {
        mCountDownTask = new CountDownTask();
        mHandler = new Handler(Looper.getMainLooper());
        super.initDevice();
    }
    
    @Override
    protected void initListeners() {
        super.initListeners();
        mIrrigationEventClassFamily.addListener(this);
    }
    
    @Override
    protected void releaseListeners() {
        super.releaseListeners();
        mIrrigationEventClassFamily.removeListener(this);
    }
    
    public IrrigationStatus getIrrigationStatus() {
        return mIrrigationStatus;
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.IRRIGATION;
    }

    @Override
    public void onEvent(final IrrigationControlRequest irrigationControlRequest, final String s) {

    }

    @Override
    public void onEvent(IrrigationStatusUpdate irrigationStatusUpdate, String sourceEndpoint) {
        if (mEndpointKey.equals(sourceEndpoint)) {
            mIrrigationStatus = irrigationStatusUpdate.getStatus();
            fireDeviceUpdated();
            startTimer();
        }
    }

    @Override
    public void onEvent(final StartIrrigationRequest startIrrigationRequest, final String s) {

    }

    public void startIrrigation() {
        mIrrigationEventClassFamily.sendEvent(new StartIrrigationRequest(), mEndpointKey);
    }
    
    public void changeIrrigationInterval(int newIrrigationIntervalSec) {
        mIrrigationEventClassFamily.sendEvent(new IrrigationControlRequest(newIrrigationIntervalSec), mEndpointKey);
    }
    
    private void startTimer() {
        mHandler.removeCallbacks(mCountDownTask);
        if (mIrrigationStatus != null && 
                mIrrigationStatus.getTimeToNextIrrigationMs() > 0) {
            mHandler.postDelayed(mCountDownTask, 1000);
        }
    }
    
    @Override
    public void releaseDevice(boolean fireListeners) {
        super.releaseDevice(fireListeners);
        mHandler.removeCallbacks(mCountDownTask);
    }
    
    protected void fireIrrigationStatusUpdated() {
        mEventBus.post(new IrrigationStatusUpdatedEvent(mEndpointKey));
    }

    @Override public void onEvent(final DeviceInfoRequest deviceInfoRequest, final String s) {

    }

    @Override
    public void onEvent(final DeviceStatusSubscriptionRequest deviceStatusSubscriptionRequest,
        final String s) {

    }

    @Override
    public void onEvent(final DeviceChangeNameRequest deviceChangeNameRequest, final String s) {

    }

    private class CountDownTask implements Runnable {
        @Override
        public void run() {
            if (mIrrigationStatus != null && mIrrigationStatus.getTimeToNextIrrigationMs() > 0) {
                long nextIrrigationMs = mIrrigationStatus.getTimeToNextIrrigationMs();
                nextIrrigationMs -= 1000;
                mIrrigationStatus.setTimeToNextIrrigationMs(nextIrrigationMs);
                fireIrrigationStatusUpdated();
                if (nextIrrigationMs > 0) {
                    mHandler.postDelayed(mCountDownTask, 1000);
                }
            }
        }
    }

}
