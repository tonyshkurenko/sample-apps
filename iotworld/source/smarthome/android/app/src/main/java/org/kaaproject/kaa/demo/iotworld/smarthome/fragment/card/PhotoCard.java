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

package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.card;

import java.nio.ByteBuffer;

import org.kaaproject.kaa.demo.iotworld.photo.PhotoAlbumInfo;
import org.kaaproject.kaa.demo.iotworld.photo.PhotoFrameStatusUpdate;
import org.kaaproject.kaa.demo.iotworld.photo.SlideShowStatus.SlideShowStatus;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.PhotoDevice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoCard extends AbstractGeoFencingDeviceCard<PhotoDevice> {

    private View mNoAlbumSelectedView;
    private View mPhotoFrameDetailsView;
    
    private ImageView mThumbnailView;
    private TextView mAlbumTitleView;
    private TextView mPhotoNumberView;
    private TextView mSlideshowStatusView;
    
    private Bitmap mThumbnailBitmap;
    
    public PhotoCard(Context context) {
        super(context);
        
        mNoAlbumSelectedView = findViewById(R.id.noAlbumSelectedView);
        mPhotoFrameDetailsView = findViewById(R.id.photoFrameDetailsView);
        
        mThumbnailView = (ImageView) findViewById(R.id.thumbnailView);
        mAlbumTitleView = (TextView) findViewById(R.id.albumTitleView);
        mPhotoNumberView = (TextView) findViewById(R.id.photoNumberView);
        mSlideshowStatusView = (TextView) findViewById(R.id.slideshowStatusView);
    }

    @Override
    protected int getCardLayout() {
        return R.layout.card_photo_device;
    }
    
    @Override
    public void bind(PhotoDevice device) {
        super.bind(device);
        PhotoFrameStatusUpdate photoFrameStatus = device.getPhotoFrameStatus();
        if (photoFrameStatus != null) {
            setDetailsVisible(true);
            PhotoAlbumInfo album = null;
            if (photoFrameStatus.getAlbumId() != null) {
                album = device.getAlbum(photoFrameStatus.getAlbumId());
            }
            if (album != null) {
                mNoAlbumSelectedView.setVisibility(View.GONE);
                mPhotoFrameDetailsView.setVisibility(View.VISIBLE);
                
                ByteBuffer buffer = photoFrameStatus.getThumbnail();
                byte[] thumbnailData = buffer.array();
                
                Bitmap prevBitmap = mThumbnailBitmap;
                mThumbnailBitmap = BitmapFactory.decodeByteArray(thumbnailData, 0, thumbnailData.length);
                mThumbnailView.setImageBitmap(mThumbnailBitmap);
                if (prevBitmap != null) {
                    prevBitmap.recycle();
                }
                mAlbumTitleView.setText(album.getTitle());
                mPhotoNumberView.setText(getResources().getString(R.string.photo_number_text, String.valueOf(photoFrameStatus.getPhotoNumber()), String.valueOf(album.getSize())));
                SlideShowStatus status = photoFrameStatus.getStatus();
                mSlideshowStatusView.setText(getResources().getStringArray(R.array.slideshow_status)[PhotoDevice.getSlideShowStatusPosition(status)]);
            } else {
                mNoAlbumSelectedView.setVisibility(View.VISIBLE);
                mPhotoFrameDetailsView.setVisibility(View.GONE);
            }
        } else {
            setDetailsVisible(false);
        }
    }

}
