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

package org.kaaproject.kaa.demo.iotworld.smarthome.data.event;

import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceType;

public class DeviceRemovedEvent {

    private final String mEndpointKey;
    private final DeviceType mDeviceType;
    
    public DeviceRemovedEvent(String endpointKey, DeviceType deviceType) {
        mEndpointKey = endpointKey;
        mDeviceType = deviceType;
    }

    public String getEndpointKey() {
        return mEndpointKey;
    }
    
    public DeviceType getDeviceType() {
        return mDeviceType;
    }
    
}
