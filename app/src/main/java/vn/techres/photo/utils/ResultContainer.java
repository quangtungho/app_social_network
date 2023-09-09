package vn.techres.photo.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.List;

import vn.techres.photo.frame.FrameEntity;
import vn.techres.photo.multitouch.controller.MultiTouchEntity;

public class ResultContainer {
    public static final String IMAGES_KEY = "imagesKey";
    public static final String PHOTO_BACKGROUND_IMAGE_KEY = "photoBgKey";
    public static final String FRAME_STICKER_IMAGES_KEY = "frameStickerKey";
    public static final String FRAME_BACKGROUND_IMAGE_KEY = "frameBackgroundKey";
    public static final String FRAME_IMAGES_KEY = "frameImageKey";

    private static ResultContainer instance = null;

    private ArrayList<MultiTouchEntity> mImages = new ArrayList<>();
    private Uri mPhotoBackgroundImage = null;
    // frame
    private ArrayList<MultiTouchEntity> mFrameStickerImages = new ArrayList<>();
    private Uri mFrameBackgroundImage = null;
    private ArrayList<FrameEntity> mFrameImages = new ArrayList<>();
    private HashMap<String, Bitmap> mDecodedImageMap = new HashMap<>();

    public static ResultContainer getInstance() {
        if (instance == null) {
            instance = new ResultContainer();
        }

        return instance;
    }

    private ResultContainer() {

    }

    public void removeImageEntity(MultiTouchEntity entity) {
        mImages.remove(entity);
    }

    public void putImageEntities(List<MultiTouchEntity> images) {
        mImages.clear();
        for (MultiTouchEntity entity : images) {
            mImages.add(entity);
        }
    }

    public void putImage(String key, Bitmap bitmap) {
        mDecodedImageMap.put(key, bitmap);
    }

    public Bitmap getImage(String key) {
        return mDecodedImageMap.get(key);
    }

    public List<MultiTouchEntity> copyImageEntities() {
        ArrayList<MultiTouchEntity> result = new ArrayList<MultiTouchEntity>();
        for (MultiTouchEntity entity : mImages) {
            result.add(entity);
        }

        return result;
    }

    public List<MultiTouchEntity> getImageEntities() {
        return mImages;
    }

    public Uri getPhotoBackgroundImage() {
        return mPhotoBackgroundImage;
    }

    public void setPhotoBackgroundImage(Uri photoBackgroundImage) {
        mPhotoBackgroundImage = photoBackgroundImage;
    }

    public void setFrameBackgroundImage(Uri frameBackgroundImage) {
        mFrameBackgroundImage = frameBackgroundImage;
    }

    public Uri getFrameBackgroundImage() {
        return mFrameBackgroundImage;
    }

    public void putFrameImage(FrameEntity entity) {
        mFrameImages.add(entity);
    }

    public List<FrameEntity> copyFrameImages() {
        ArrayList<FrameEntity> result = new ArrayList<>();
        for (FrameEntity uri : mFrameImages) {
            result.add(uri);
        }

        return result;
    }

    public void putFrameSticker(MultiTouchEntity entity) {
        mFrameStickerImages.add(entity);
    }

    public void putFrameStickerImages(List<MultiTouchEntity> images) {
        mFrameStickerImages.clear();
        for (MultiTouchEntity entity : images) {
            mFrameStickerImages.add(entity);
        }
    }

    public List<MultiTouchEntity> copyFrameStickerImages() {
        ArrayList<MultiTouchEntity> result = new ArrayList<>();
        for (MultiTouchEntity entity : mFrameStickerImages) {
            result.add(entity);
        }

        return result;
    }

    public void removeFrameSticker(MultiTouchEntity entity) {
        mFrameStickerImages.remove(entity);
    }

    /**
     * Clear all frame image uri
     */
    public void clearFrameImages() {
        mFrameImages.clear();
    }

    public void clearAll() {
        mImages.clear();
        mPhotoBackgroundImage = null;
        mFrameStickerImages.clear();
        mFrameImages.clear();
        mFrameBackgroundImage = null;
        mDecodedImageMap.clear();
    }

    public void clearAllImageInFrameCreator() {
        mFrameStickerImages.clear();
        mFrameImages.clear();
        mFrameBackgroundImage = null;
    }

    public void saveToBundle(Bundle bundle) {
        bundle.putParcelableArrayList(IMAGES_KEY, mImages);
        bundle.putParcelable(PHOTO_BACKGROUND_IMAGE_KEY, mPhotoBackgroundImage);
        bundle.putParcelableArrayList(FRAME_STICKER_IMAGES_KEY,
                mFrameStickerImages);
        bundle.putParcelable(FRAME_BACKGROUND_IMAGE_KEY, mFrameBackgroundImage);
        bundle.putParcelableArrayList(FRAME_IMAGES_KEY, mFrameImages);
    }

    public void restoreFromBundle(Bundle bundle) {
        mImages = bundle.getParcelableArrayList(IMAGES_KEY);
        if (mImages == null) {
            mImages = new ArrayList<>();
        }
        mPhotoBackgroundImage = bundle
                .getParcelable(PHOTO_BACKGROUND_IMAGE_KEY);
        mFrameStickerImages = bundle
                .getParcelableArrayList(FRAME_STICKER_IMAGES_KEY);
        if (mFrameStickerImages == null) {
            mFrameStickerImages = new ArrayList<>();
        }
        mFrameBackgroundImage = bundle
                .getParcelable(FRAME_BACKGROUND_IMAGE_KEY);
        mFrameImages = bundle.getParcelableArrayList(FRAME_IMAGES_KEY);
        if (mFrameImages == null) {
            mFrameImages = new ArrayList<>();
        }
    }
}
