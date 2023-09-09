package vn.techres.line.call.helper;

import android.graphics.Bitmap;

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/1/2022
 */
public interface StatusCall {
    void onCallReject(String data);

    void onCallAccept(String data);

    void onChangeCallMe();

    void onChangeCallOther();

    void onFloatingChangeCall(boolean is);

    void onOtherOffMic(boolean status);

    void onOtherOffCamera(boolean status);

    void onScreenShot(Bitmap bitmap);
}
