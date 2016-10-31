/**
 * Copyright 2014-2016 CyberVision, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kaaproject.kaa.demo.photoframe.kaa;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaClientPlatformContext;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.demo.photoframe.AlbumInfo;
import org.kaaproject.kaa.demo.photoframe.DeviceInfo;
import org.kaaproject.kaa.demo.photoframe.PlayInfo;
import org.kaaproject.kaa.demo.photoframe.PlayStatus;
import org.kaaproject.kaa.demo.photoframe.communication.Events;
import org.kaaproject.kaa.demo.photoframe.fragment.BaseFragment;

import java.util.List;
import java.util.Map;

/**
 * Performs initialization of the application resources including initialization of the Kaa client.
 * Handles the Kaa client lifecycle.
 * Stores a reference to the actual endpoint configuration.
 * Receives configuration updates from the Kaa cluster.
 * Manages the endpoint profile object, notifies the Kaa cluster of the profile updates.
 * <p>
 * Implements {@link SimpleKaaClientStateListener}
 */
public class KaaManager extends SimpleKaaClientStateListener {

    private final KaaClient mClient;
    private final EventBus mEventBus;

    private final KaaInfoSlave mInfoSlave;
    private final KaaEventsSlave mEventsSlave;
    private final KaaUserVerifierSlave mKaaUserVerifierSlave;

    public KaaManager(Context ctx) {
        mEventBus = EventBus.getDefault();

        mInfoSlave = new KaaInfoSlave();
        mEventsSlave = new KaaEventsSlave(mInfoSlave);

        final KaaClientPlatformContext kaaClientContext = new AndroidKaaPlatformContext(ctx);
        mClient = Kaa.newClient(kaaClientContext, this, true);

        mInfoSlave.initDeviceInfo(ctx);
        mEventsSlave.init(mClient);

        mKaaUserVerifierSlave = new KaaUserVerifierSlave(this);
    }

    /**
     * Initialize the Kaa client using the Android context.
     * Start the Kaa client workflow.
     */
    public void start() {
        mClient.start();
    }

    /**
     * User Verifier part
     */
    public void login(String userExternalId, String userAccessToken) {
        mKaaUserVerifierSlave.login(userExternalId, userAccessToken);
    }

    public boolean isUserAttached() {
        return mKaaUserVerifierSlave.isUserAttached();
    }

    public void logout() {
        mKaaUserVerifierSlave.logout();
    }

    /**
     * Events part
     */
    public void updateStatus(PlayStatus playing, String mBucketId) {
        mEventsSlave.updateStatus(playing, mBucketId);
    }

    public void discoverRemoteDevices() {
        mEventsSlave.discoverRemoteDevices();
    }

    public void stopPlayRemoteDeviceAlbum(String mEndpointKey) {
        mEventsSlave.stopPlayRemoteDeviceAlbum(mEndpointKey);
    }

    public void requestRemoteDeviceInfo(String mEndpointKey) {
        mEventsSlave.requestRemoteDeviceAlbums(mEndpointKey);
        mEventsSlave.requestRemoteDeviceStatus(mEndpointKey);
    }

    public void playRemoteDeviceAlbum(String mEndpointKey, String bucketId) {
        mEventsSlave.playRemoteDeviceAlbum(mEndpointKey, bucketId);
    }

    /**
     * Information part
     */
    public String getRemoteDeviceEndpoint(int position) {
        if (mInfoSlave.getRemoteDevicesMap().keySet().toArray().length > position) {
            return (String) mInfoSlave.getRemoteDevicesMap().keySet().toArray()[position];
        }
        return null;
    }

    public PlayInfo getRemoteDeviceStatus(String endpointKey) {
        return mInfoSlave.getRemoteDeviceStatus(endpointKey);
    }

    public Map<String, DeviceInfo> getRemoteDevicesMap() {
        return mInfoSlave.getRemoteDevicesMap();
    }

    public String getRemoteDeviceModel(String mEndpointKey) {
        return mInfoSlave.getRemoteDevicesMap().get(mEndpointKey).getModel();
    }

    public List<AlbumInfo> getRemoteDeviceAlbums(String mEndpointKey) {
        return mInfoSlave.getRemoteDeviceAlbums(mEndpointKey);
    }

    /**
     * Stop the Kaa client. Release all network connections and application
     * resources. Shut down all the Kaa client tasks.
     */
    public void stop() {
        mClient.stop();
    }

    @Override
    public void onStarted() {
        /**
         *  For showing WaitFragment
         */

        mEventBus.postSticky(new Events.KaaStartedEvent());
    }

    @Override
    public void onResume() {
        if (mKaaUserVerifierSlave.isUserAttached()) {
            mEventsSlave.notifyRemoteDevices();
        }
    }

    KaaClient getClient() {
        return mClient;
    }

    void onUserAttach(boolean successResult, String error) {
        if (successResult) {
            mEventsSlave.notifyRemoteDevices();
            mEventBus.post(new Events.UserAttachEvent());
        } else {
            mEventBus.post(new Events.UserAttachEvent(error));
        }
    }

    void onUserDetach(boolean successResult) {
        if (successResult) {
            mEventBus.post(new Events.UserDetachEvent());
        } else {
            mEventBus.post(new Events.UserDetachEvent("Failed to detach endpoint from user!"));
        }
    }
}