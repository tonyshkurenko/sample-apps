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

import org.greenrobot.eventbus.EventBus;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.registration.OnDetachEndpointOperationCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.demo.iotworld.device.DeviceChangeNameRequest;
import org.kaaproject.kaa.demo.iotworld.device.DeviceEventClassFamilyV2;
import org.kaaproject.kaa.demo.iotworld.device.DeviceInfo;
import org.kaaproject.kaa.demo.iotworld.device.DeviceInfoRequest;
import org.kaaproject.kaa.demo.iotworld.device.DeviceInfoResponse;
import org.kaaproject.kaa.demo.iotworld.device.DeviceStatusSubscriptionRequest;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.event.DeviceRemovedEvent;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.event.DeviceUpdatedEvent;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

public abstract class AbstractDevice implements DeviceEventClassFamilyV2.Listener, OnDetachEndpointOperationCallback {

    private static final String TAG = AbstractDevice.class.getSimpleName();
    
    protected final String mEndpointKey;
    private final DeviceStore mDeviceStore;
    protected final KaaClient mClient;
    protected final EventBus mEventBus;
    private final DeviceEventClassFamilyV2 mDeviceEventClassFamily;
    
    private DeviceInfo mDeviceInfo;
    
    public AbstractDevice(String endpointKey, 
            DeviceStore deviceStore, 
            KaaClient client, 
            EventBus eventBus) {
        mEndpointKey = endpointKey;
        mDeviceStore = deviceStore;
        mClient = client;
        mEventBus = eventBus;
        mDeviceEventClassFamily = mClient.getEventFamilyFactory().getDeviceEventClassFamilyV2();
    }
    
    protected void initListeners() {
        mDeviceEventClassFamily.addListener(this);
    }
    
    protected void releaseListeners() {
        mDeviceEventClassFamily.removeListener(this);
    }
    
    public void initDevice() {
        initListeners();
        requestDeviceInfo();
    }
    
    public void requestDeviceInfo() {
        mDeviceEventClassFamily.sendEvent(new DeviceInfoRequest(), mEndpointKey);
    }
    
    public abstract DeviceType getDeviceType();
    
    protected void fireDeviceUpdated() {
        mEventBus.post(new DeviceUpdatedEvent(mEndpointKey, getDeviceType()));
    }
    
    @Override
    public void onEvent(DeviceInfoResponse deviceInfoResponse, String sourceEndpoint) {
        if (mEndpointKey.equals(sourceEndpoint)) {
            mDeviceInfo = deviceInfoResponse.getDeviceInfo();
            mDeviceStore.deviceInfoReceived(mEndpointKey);
            fireDeviceUpdated();
            mDeviceEventClassFamily.sendEvent(new DeviceStatusSubscriptionRequest(), mEndpointKey);
        }
    }
    
    public void detach() {
        EndpointKeyHash endpointKey = new EndpointKeyHash(mEndpointKey);
        mClient.detachEndpoint(endpointKey, this);
    }
    
    @Override
    public void onDetach(SyncResponseResultType result) {
        if (result == SyncResponseResultType.SUCCESS) {
            Log.d(TAG, "Endpoint detached from user account!");
        } else {
            Log.w(TAG, "Endpoint already detached from user account!");
        }
        releaseDevice(true);
    }
    
    public String getEndpointKey() {
        return mEndpointKey;
    }
    
    public DeviceInfo getDeviceInfo() {
        return mDeviceInfo;
    }
    
    public void renameDevice(Context context) {
        if (mDeviceInfo != null) {
            final String prevDeviceName = mDeviceInfo.getName();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.rename_device);
            final EditText input = new EditText(context);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(prevDeviceName);
            builder.setView(input);
            builder.setPositiveButton(R.string.rename, new DialogInterface.OnClickListener() { 
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String newDeviceName = input.getText().toString();
                    if (!TextUtils.isEmpty(newDeviceName) && !newDeviceName.equals(prevDeviceName)) {
                        changeName(newDeviceName);
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }
    }
    
    private void changeName(String newName) {
        mDeviceEventClassFamily.sendEvent(new DeviceChangeNameRequest(newName), mEndpointKey);
    }
    
    public void releaseDevice(boolean fireListeners) {
        releaseListeners();
        if (fireListeners) {
            mDeviceStore.deviceRemoved(mEndpointKey);
            mEventBus.post(new DeviceRemovedEvent(mEndpointKey, getDeviceType()));
        }
    }
 
    
    
}
