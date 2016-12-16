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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.greenrobot.eventbus.EventBus;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.demo.iotworld.device.DeviceChangeNameRequest;
import org.kaaproject.kaa.demo.iotworld.device.DeviceInfoRequest;
import org.kaaproject.kaa.demo.iotworld.device.DeviceStatusSubscriptionRequest;
import org.kaaproject.kaa.demo.iotworld.device.PhotoEventClassFamilyV2;
import org.kaaproject.kaa.demo.iotworld.geo.GeoFencingPositionUpdate;
import org.kaaproject.kaa.demo.iotworld.geo.GeoFencingStatusRequest;
import org.kaaproject.kaa.demo.iotworld.geo.OperationModeUpdateRequest;
import org.kaaproject.kaa.demo.iotworld.photo.DeleteUploadedPhotosRequest;
import org.kaaproject.kaa.demo.iotworld.photo.PauseSlideShowRequest;
import org.kaaproject.kaa.demo.iotworld.photo.PhotoAlbumInfo;
import org.kaaproject.kaa.demo.iotworld.photo.PhotoAlbumsRequest;
import org.kaaproject.kaa.demo.iotworld.photo.PhotoAlbumsResponse;
import org.kaaproject.kaa.demo.iotworld.photo.PhotoFrameStatusUpdate;
import org.kaaproject.kaa.demo.iotworld.photo.PhotoUploadRequest;
import org.kaaproject.kaa.demo.iotworld.photo.SlideShowStatus.SlideShowStatus;
import org.kaaproject.kaa.demo.iotworld.photo.StartSlideShowRequest;


public class PhotoDevice extends AbstractGeoFencingDevice implements PhotoEventClassFamilyV2.Listener {

    private final PhotoEventClassFamilyV2 mPhotoEventClassFamily;
    
    private List<PhotoAlbumInfo> mAlbums;
    private List<PhotoAlbumInfo> mSortedAlbums;
    private PhotoFrameStatusUpdate mPhotoFrameStatus;
    
    private final AlbumsComparator mAlbumsComparator = new AlbumsComparator();
    
    private static Map<Integer, SlideShowStatus> positionToSlideShowStatusMap = new HashMap<>();
    static {
    	positionToSlideShowStatusMap.put(0, SlideShowStatus.PAUSED);
    	positionToSlideShowStatusMap.put(1, SlideShowStatus.PLAYING);
    }
    
    private static Map<SlideShowStatus, Integer> slideShowStatusToPositionMap = new HashMap<>();
    static {
    	slideShowStatusToPositionMap.put(SlideShowStatus.PAUSED, 0);
    	slideShowStatusToPositionMap.put(SlideShowStatus.PLAYING, 1);
    }
    
    public static SlideShowStatus getSlideShowStatus(int position) {
    	return positionToSlideShowStatusMap.get(position);
    }
    
    public static int getSlideShowStatusPosition(SlideShowStatus status) {
    	return slideShowStatusToPositionMap.get(status);
    }
    
    public PhotoDevice(String endpointKey, DeviceStore deviceStore, KaaClient client, EventBus eventBus) {
        super(endpointKey, deviceStore, client, eventBus);
        mPhotoEventClassFamily = mClient.getEventFamilyFactory().getPhotoEventClassFamilyV2();
    }
    
    @Override
    protected void initListeners() {
        super.initListeners();
        mPhotoEventClassFamily.addListener(this);
    }
    
    @Override
    protected void releaseListeners() {
        super.releaseListeners();
        mPhotoEventClassFamily.removeListener(this);
    }
    
    @Override
    public void requestDeviceInfo() {
        super.requestDeviceInfo();
        mPhotoEventClassFamily.sendEvent(new PhotoAlbumsRequest(), mEndpointKey);
    }
    
    public List<PhotoAlbumInfo> getAlbums() {
        return mAlbums;
    }
    
    public List<PhotoAlbumInfo> getSortedAlbums() {
        return mSortedAlbums;
    }
    
    public PhotoAlbumInfo getAlbum(String albumId) {
        if (mAlbums != null) {
            for (PhotoAlbumInfo album : mAlbums) {
                if (album.getId().equals(albumId)) {
                    return album;
                }
            }
        }
        return null;
    }
    
    public PhotoFrameStatusUpdate getPhotoFrameStatus() {
        return mPhotoFrameStatus;
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.PHOTO;
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

    private class AlbumsComparator implements Comparator<PhotoAlbumInfo> {

        @Override
        public int compare(PhotoAlbumInfo lhs, PhotoAlbumInfo rhs) {
            if (mPhotoFrameStatus != null && mPhotoFrameStatus.getAlbumId() != null) {
                String albumId = mPhotoFrameStatus.getAlbumId();
                if (lhs.getId().equals(albumId)) {
                    return -1;
                } else if (rhs.getId().equals(albumId)) {
                    return 1;
                }
            }
            return 0;
        }
        
    }

    @Override public void onEvent(final PhotoAlbumsRequest photoAlbumsRequest, final String s) {

    }

    @Override
    public void onEvent(PhotoAlbumsResponse photoAlbumsResponse, String sourceEndpoint) {
        if (mEndpointKey.equals(sourceEndpoint)) {
            mAlbums = photoAlbumsResponse.getAlbums();
            mSortedAlbums = new ArrayList<PhotoAlbumInfo>(mAlbums);
            Collections.sort(mSortedAlbums, mAlbumsComparator);
            fireDeviceUpdated();
        }
    }

    @Override public void onEvent(final PhotoUploadRequest photoUploadRequest, final String s) {

    }

    @Override
    public void onEvent(final StartSlideShowRequest startSlideShowRequest, final String s) {

    }

    @Override
    public void onEvent(final PauseSlideShowRequest pauseSlideShowRequest, final String s) {

    }

    @Override public void onEvent(final DeleteUploadedPhotosRequest deleteUploadedPhotosRequest,
        final String s) {

    }

    @Override
    public void onEvent(PhotoFrameStatusUpdate photoFrameStatusUpdate, String sourceEndpoint) {
        if (mEndpointKey.equals(sourceEndpoint)) {
            mPhotoFrameStatus = photoFrameStatusUpdate;
            Collections.sort(mSortedAlbums, mAlbumsComparator);
            fireDeviceUpdated();
        }
    }
    
    public void startStopSlideshow(String albumId) {
        if (mPhotoFrameStatus != null && mPhotoFrameStatus.getAlbumId() != null && 
                mPhotoFrameStatus.getAlbumId().equals(albumId) &&
                mPhotoFrameStatus.getStatus() == SlideShowStatus.PLAYING) {
            mPhotoEventClassFamily.sendEvent(new PauseSlideShowRequest(), mEndpointKey);
        } else {
            mPhotoEventClassFamily.sendEvent(new StartSlideShowRequest(albumId), mEndpointKey);
        }
    }
    
    public void pauseSlideshow() {
        mPhotoEventClassFamily.sendEvent(new PauseSlideShowRequest(), mEndpointKey);
    }
    
    public void uploadPhoto(String name, byte[] data) {
        mPhotoEventClassFamily.sendEvent(new PhotoUploadRequest(name, ByteBuffer.wrap(data)), mEndpointKey);
    }
    
    public void deleteUploadedPhotos() {
        mPhotoEventClassFamily.sendEvent(new DeleteUploadedPhotosRequest(), mEndpointKey);
    }

}
