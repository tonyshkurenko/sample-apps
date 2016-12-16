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
import org.kaaproject.kaa.demo.iotworld.device.DeviceChangeNameRequest;
import org.kaaproject.kaa.demo.iotworld.device.DeviceInfoRequest;
import org.kaaproject.kaa.demo.iotworld.device.DeviceStatusSubscriptionRequest;
import org.kaaproject.kaa.demo.iotworld.device.ThermoEventClassFamilyV2;
import org.kaaproject.kaa.demo.iotworld.geo.GeoFencingPositionUpdate;
import org.kaaproject.kaa.demo.iotworld.geo.GeoFencingStatusRequest;
import org.kaaproject.kaa.demo.iotworld.geo.OperationModeUpdateRequest;
import org.kaaproject.kaa.demo.iotworld.thermo.ChangeDegreeRequest;
import org.kaaproject.kaa.demo.iotworld.thermo.ThermostatInfo;
import org.kaaproject.kaa.demo.iotworld.thermo.ThermostatStatusUpdate;


public class ClimateDevice extends AbstractGeoFencingDevice implements ThermoEventClassFamilyV2.Listener {

    private final ThermoEventClassFamilyV2 mThermoEventClassFamily;
    
    private ThermostatInfo mThermostatInfo;

    public ClimateDevice(String endpointKey, DeviceStore deviceStore, KaaClient client, EventBus eventBus) {
        super(endpointKey, deviceStore, client, eventBus);
        mThermoEventClassFamily = mClient.getEventFamilyFactory().getThermoEventClassFamilyV2();
    }
    
    @Override
    protected void initListeners() {
        super.initListeners();
        mThermoEventClassFamily.addListener(this);
    }
    
    @Override
    protected void releaseListeners() {
        super.releaseListeners();
        mThermoEventClassFamily.removeListener(this);
    }

    public ThermostatInfo getThermostatInfo() {
        return mThermostatInfo;
    }
 
    @Override
    public DeviceType getDeviceType() {
        return DeviceType.CLIMATE;
    }

    @Override
    public void onEvent(ThermostatStatusUpdate thermostatStatusUpdate, String sourceEndpoint) {
        if (mEndpointKey.equals(sourceEndpoint)) {
            mThermostatInfo = thermostatStatusUpdate.getThermostatInfo();
            fireDeviceUpdated();
        }
    }

    @Override public void onEvent(final ChangeDegreeRequest changeDegreeRequest, final String s) {

    }

    public void changeDegree(int newDegree) {
        mThermoEventClassFamily.sendEvent(new ChangeDegreeRequest(newDegree), mEndpointKey);
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
