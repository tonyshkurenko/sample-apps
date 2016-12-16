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
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.AbstractDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.event.DeviceRemovedEvent;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.event.DeviceUpdatedEvent;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.AbstractSmartHomeFragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public abstract class AbstractDeviceFragment<D extends AbstractDevice> extends AbstractSmartHomeFragment {

    public static final String ENDPOINT_KEY = "endpointKey";
    
    protected String mEndpointKey;
    protected D mDevice; 
    
    private String mDeviceTitle;
    
    public AbstractDeviceFragment() {
        super();
        setHasOptionsMenu(true);
    }
    
    public AbstractDeviceFragment(String endpointKey) {
        super();
        setHasOptionsMenu(true);
        mEndpointKey =  endpointKey;
    }
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mEndpointKey == null) {
            mEndpointKey = savedInstanceState.getString(ENDPOINT_KEY);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDevice = (D) mDeviceStore.getDevicesMap().get(mEndpointKey);
       
        LayoutInflater inflater = (LayoutInflater)
                mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        setupView(inflater, getView());

        bindDevice(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mEndpointKey != null) {
            outState.putString(ENDPOINT_KEY, mEndpointKey);
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(getDeviceLayout(), container,
                false);
        return rootView;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DeviceUpdatedEvent deviceUpdatedEvent) {
        if (mEndpointKey != null && mEndpointKey.equals(deviceUpdatedEvent.getEndpointKey())) {
            bindDevice(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DeviceRemovedEvent deviceRemovedEvent) {
        if (mEndpointKey != null && mEndpointKey.equals(deviceRemovedEvent.getEndpointKey())) {
            mActivity.popBackStack();
        }
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
        inflater.inflate(R.menu.device, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.refresh:
            mDevice.requestDeviceInfo();
            return true;
        case R.id.rename:
            mDevice.renameDevice(mActivity);
            return true;
        case R.id.detach:
            mDevice.detach();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected int getBarsBackgroundColor() {
        return getResources().getColor(mDevice.getDeviceType().getBaseColorResId());
    }
    
    @Override
    protected boolean showNavigationDrawer() {
        return false;
    }
    
    @Override
    protected boolean displayHomeAsUp() {
        return true;
    }
    
    @Override
    protected String getTitle() {
        return mDeviceTitle;
    }
 
    protected abstract int getDeviceLayout();
    
    protected abstract void setupView(LayoutInflater inflater, View rootView);
    
    protected void bindDevice(boolean firstLoad) {
        mDeviceTitle = null;
        if (mDevice.getDeviceInfo() != null && mDevice.getDeviceInfo().getName() != null) {
            mDeviceTitle = mDevice.getDeviceInfo().getName();
        } else {
            mDeviceTitle = getResources().getString(R.string.unknown);
        }
        if (mToolbar != null) {
            mToolbar.setTitle(getTitle());
        }
    }
       
}
