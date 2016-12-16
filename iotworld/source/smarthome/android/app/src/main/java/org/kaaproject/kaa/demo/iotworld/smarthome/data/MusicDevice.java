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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.demo.iotworld.device.DeviceChangeNameRequest;
import org.kaaproject.kaa.demo.iotworld.device.DeviceInfoRequest;
import org.kaaproject.kaa.demo.iotworld.device.DeviceStatusSubscriptionRequest;
import org.kaaproject.kaa.demo.iotworld.device.MusicEventClassFamilyV2;
import org.kaaproject.kaa.demo.iotworld.geo.GeoFencingPositionUpdate;
import org.kaaproject.kaa.demo.iotworld.geo.GeoFencingStatusRequest;
import org.kaaproject.kaa.demo.iotworld.geo.OperationModeUpdateRequest;
import org.kaaproject.kaa.demo.iotworld.music.AlbumInfo;
import org.kaaproject.kaa.demo.iotworld.music.ChangeVolumeRequest;
import org.kaaproject.kaa.demo.iotworld.music.PauseRequest;
import org.kaaproject.kaa.demo.iotworld.music.PlayListRequest;
import org.kaaproject.kaa.demo.iotworld.music.PlayListResponse;
import org.kaaproject.kaa.demo.iotworld.music.PlayRequest;
import org.kaaproject.kaa.demo.iotworld.music.PlaybackInfo;
import org.kaaproject.kaa.demo.iotworld.music.PlaybackStatusUpdate;
import org.kaaproject.kaa.demo.iotworld.music.SeekRequest;
import org.kaaproject.kaa.demo.iotworld.music.StopRequest;

public class MusicDevice extends AbstractGeoFencingDevice implements MusicEventClassFamilyV2.Listener {

    private final MusicEventClassFamilyV2 mMusicEventClassFamily;
    
    private List<AlbumInfo> mAlbums;
    private List<AlbumInfo> mSortedAlbums;
    private PlaybackInfo mPlaybackInfo;
    
    private final AlbumsComparator mAlbumsComparator = new AlbumsComparator();
    
    public MusicDevice(String endpointKey, DeviceStore deviceStore, KaaClient client, EventBus eventBus) {
        super(endpointKey, deviceStore, client, eventBus);
        mMusicEventClassFamily = mClient.getEventFamilyFactory().getMusicEventClassFamilyV2();
    }
    
    @Override
    protected void initListeners() {
        super.initListeners();
        mMusicEventClassFamily.addListener(this);
    }
    
    @Override
    protected void releaseListeners() {
        super.releaseListeners();
        mMusicEventClassFamily.removeListener(this);
    }
    
    @Override
    public void requestDeviceInfo() {
        super.requestDeviceInfo();
        mMusicEventClassFamily.sendEvent(new PlayListRequest(), mEndpointKey);
    }
    
    public List<AlbumInfo> getAlbums() {
        return mAlbums;
    }
    
    public List<AlbumInfo> getSortedAlbums() {
        return mSortedAlbums;
    }
    
    public AlbumInfo getAlbum(String albumId) {
        if (mAlbums != null) {
            for (AlbumInfo album : mAlbums) {
                if (album.getAlbumId().equals(albumId)) {
                    return album;
                }
            }
        }
        return null;
    }
    
    public PlaybackInfo getPlaybackInfo() {
        return mPlaybackInfo;
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.MUSIC;
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

    private class AlbumsComparator implements Comparator<AlbumInfo> {

        @Override
        public int compare(AlbumInfo lhs, AlbumInfo rhs) {
            if (mPlaybackInfo != null && mPlaybackInfo.getSong() != null) {
                String albumId = mPlaybackInfo.getSong().getAlbumId();
                if (lhs.getAlbumId().equals(albumId)) {
                    return -1;
                } else if (rhs.getAlbumId().equals(albumId)) {
                    return 1;
                }
            }
            return 0;
        }
        
    }

    @Override public void onEvent(final PlayListRequest playListRequest, final String s) {

    }

    @Override
    public void onEvent(PlayListResponse playListResponse, String sourceEndpoint) {
        if (mEndpointKey.equals(sourceEndpoint)) {
            mAlbums = playListResponse.getAlbums();
            mSortedAlbums = new ArrayList<AlbumInfo>(mAlbums);
            Collections.sort(mSortedAlbums, mAlbumsComparator);
            fireDeviceUpdated();
        }
    }

    @Override public void onEvent(final PlayRequest playRequest, final String s) {

    }

    @Override public void onEvent(final PauseRequest pauseRequest, final String s) {

    }

    @Override public void onEvent(final StopRequest stopRequest, final String s) {

    }

    @Override public void onEvent(final ChangeVolumeRequest changeVolumeRequest, final String s) {

    }

    @Override public void onEvent(final SeekRequest seekRequest, final String s) {

    }

    @Override
    public void onEvent(PlaybackStatusUpdate playbackStatusUpdate, String sourceEndpoint) {
        if (mEndpointKey.equals(sourceEndpoint)) {
            mPlaybackInfo = playbackStatusUpdate.getPlaybackInfo();
            Collections.sort(mSortedAlbums, mAlbumsComparator);
            fireDeviceUpdated();
        }
    }
    
    public void changeVolume(int newVolume) {
        mMusicEventClassFamily.sendEvent(new ChangeVolumeRequest(newVolume), mEndpointKey);
    }
    
    public void play(String url) {
        mMusicEventClassFamily.sendEvent(new PlayRequest(url), mEndpointKey);
    }
    
    public void pause() {
        mMusicEventClassFamily.sendEvent(new PauseRequest(), mEndpointKey);
    }
    
    public void stop() {
        mMusicEventClassFamily.sendEvent(new StopRequest(), mEndpointKey);
    }
    
    public void seek(int time) {
        mMusicEventClassFamily.sendEvent(new SeekRequest(time), mEndpointKey);
    }

}
