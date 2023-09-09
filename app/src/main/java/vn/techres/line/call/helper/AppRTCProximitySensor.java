package vn.techres.line.call.helper;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.Nullable;

import org.webrtc.ThreadUtils;

import timber.log.Timber;
import vn.techres.line.call.utils.AppRTCUtils;

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/1/2022
 */
public class AppRTCProximitySensor implements SensorEventListener {
    private final ThreadUtils.ThreadChecker threadChecker = new ThreadUtils.ThreadChecker();

    private final Runnable onSensorStateListener;
    private final SensorManager sensorManager;
    @Nullable
    private Sensor proximitySensor;
    private boolean lastStateReportIsNear;

    static AppRTCProximitySensor create(Context context, Runnable sensorStateListener) {
        return new AppRTCProximitySensor(context, sensorStateListener);
    }

    private AppRTCProximitySensor(Context context, Runnable sensorStateListener) {
        Timber.d("AppRTCProximitySensor %s", AppRTCUtils.getThreadInfo());
        onSensorStateListener = sensorStateListener;
        sensorManager = ((SensorManager) context.getSystemService(Context.SENSOR_SERVICE));
    }

    public boolean start() {
        threadChecker.checkIsOnValidThread();
        if (!initDefaultSensor()) {
            return false;
        }
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        return true;
    }

    public void stop() {
        threadChecker.checkIsOnValidThread();
        if (proximitySensor == null) {
            return;
        }
        sensorManager.unregisterListener(this, proximitySensor);
    }

    public boolean sensorReportsNearState() {
        threadChecker.checkIsOnValidThread();
        return lastStateReportIsNear;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        threadChecker.checkIsOnValidThread();
        AppRTCUtils.assertIsTrue(sensor.getType() == Sensor.TYPE_PROXIMITY);
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        threadChecker.checkIsOnValidThread();
        AppRTCUtils.assertIsTrue(event.sensor.getType() == Sensor.TYPE_PROXIMITY);
        float distanceInCentimeters = event.values[0];
        assert proximitySensor != null;
        lastStateReportIsNear = distanceInCentimeters < proximitySensor.getMaximumRange();
        if (onSensorStateListener != null) {
            onSensorStateListener.run();
        }
        Timber.d("onSensorChanged" + AppRTCUtils.getThreadInfo() + ": "
                + "accuracy=" + event.accuracy + ", timestamp=" + event.timestamp + ", distance="
                + event.values[0]);
    }

    private boolean initDefaultSensor() {
        if (proximitySensor != null) {
            return true;
        }
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (proximitySensor == null) {
            return false;
        }
        logProximitySensorInfo();
        return true;
    }

    /**
     * Helper method for logging information about the proximity sensor.
     */
    private void logProximitySensorInfo() {
        if (proximitySensor == null) {
            return;
        }
        String info = "Proximity sensor: " + "name=" + proximitySensor.getName() +
                ", vendor: " + proximitySensor.getVendor() +
                ", power: " + proximitySensor.getPower() +
                ", resolution: " + proximitySensor.getResolution() +
                ", max range: " + proximitySensor.getMaximumRange() +
                ", min delay: " + proximitySensor.getMinDelay() +
                // Added in API level 20.
                ", type: " + proximitySensor.getStringType() +
                // Added in API level 21.
                ", max delay: " + proximitySensor.getMaxDelay() +
                ", reporting mode: " + proximitySensor.getReportingMode() +
                ", isWakeUpSensor: " + proximitySensor.isWakeUpSensor();
        Timber.d(info);
    }
}
