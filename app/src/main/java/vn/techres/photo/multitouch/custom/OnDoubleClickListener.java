package vn.techres.photo.multitouch.custom;


import vn.techres.photo.multitouch.controller.MultiTouchEntity;

public interface OnDoubleClickListener {
	public void onPhotoViewDoubleClick(PhotoView view, MultiTouchEntity entity);
	public void onBackgroundDoubleClick();
}
