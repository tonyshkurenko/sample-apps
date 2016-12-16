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

import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.demo.iotworld.device.DeviceChangeNameRequest;
import org.kaaproject.kaa.demo.iotworld.device.DeviceInfoRequest;
import org.kaaproject.kaa.demo.iotworld.device.DeviceStatusSubscriptionRequest;
import org.kaaproject.kaa.demo.iotworld.device.LightEventClassFamilyV2;
import org.kaaproject.kaa.demo.iotworld.geo.GeoFencingPositionUpdate;
import org.kaaproject.kaa.demo.iotworld.geo.GeoFencingStatusRequest;
import org.kaaproject.kaa.demo.iotworld.geo.OperationModeUpdateRequest;
import org.kaaproject.kaa.demo.iotworld.light.BulbInfo;
import org.kaaproject.kaa.demo.iotworld.light.BulbListRequest;
import org.kaaproject.kaa.demo.iotworld.light.BulbListStatusUpdate;
import org.kaaproject.kaa.demo.iotworld.light.BulbStatus;
import org.kaaproject.kaa.demo.iotworld.light.ChangeBulbBrightnessRequest;
import org.kaaproject.kaa.demo.iotworld.light.ChangeBulbStatusRequest;

public class LightningDevice extends AbstractGeoFencingDevice implements LightEventClassFamilyV2.Listener {

    private final LightEventClassFamilyV2 mLightEventClassFamily;
    
    private List<BulbInfo> mBulbs;
    
    public LightningDevice(String endpointKey, DeviceStore deviceStore, KaaClient client, EventBus eventBus) {
        super(endpointKey, deviceStore, client, eventBus);
        mLightEventClassFamily = mClient.getEventFamilyFactory().getLightEventClassFamilyV2();
    }
    
    @Override
    protected void initListeners() {
        super.initListeners();
        mLightEventClassFamily.addListener(this);
    }
    
    @Override
    protected void releaseListeners() {
        super.releaseListeners();
        mLightEventClassFamily.removeListener(this);
    }
    
    @Override
    public void requestDeviceInfo() {
        super.requestDeviceInfo();
        mLightEventClassFamily.sendEvent(new BulbListRequest(), mEndpointKey);
    }
    
    public List<BulbInfo> getBulbs() {
        return mBulbs;
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.LIGHTNING;
    }

    @Override public void onEvent(final BulbListRequest bulbListRequest, final String s) {

    }

    @Override
    public void onEvent(BulbListStatusUpdate bulbListStatusUpdate, String sourceEndpoint) {
        if (mEndpointKey.equals(sourceEndpoint)) {
            mBulbs = bulbListStatusUpdate.getBulbs();
            fireDeviceUpdated();
        }
    }

    @Override public void onEvent(final ChangeBulbBrightnessRequest changeBulbBrightnessRequest,
        final String s) {

    }

    @Override
    public void onEvent(final ChangeBulbStatusRequest changeBulbStatusRequest, final String s) {

    }

    private BulbInfo getBulbById(String bulbId) {
        if (mBulbs != null) {
            for (BulbInfo bulb : mBulbs) {
                if (bulb.getBulbId().equals(bulbId)) {
                    return bulb;
                }
            }
        }
        return null;
    }
    
    public void changeBulbBrightness(String bulbId, int brightness) {
        mLightEventClassFamily.sendEvent(new ChangeBulbBrightnessRequest(bulbId, brightness), mEndpointKey);
    }
    
    public void changeBulbStatus(String bulbId, BulbStatus status) {
        mLightEventClassFamily.sendEvent(new ChangeBulbStatusRequest(bulbId, status), mEndpointKey);
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

    @Override
    public void onEvent(final GeoFencingStatusRequest geoFencingStatusRequest, final String s) {

    }

    @Override public void onEvent(final OperationModeUpdateRequest operationModeUpdateRequest,
        final String s) {

    }

    @Override
    public void onEvent(final GeoFencingPositionUpdate geoFencingPositionUpdate, final String s) {

    }
}
