package gl.iglou.studio.nanosense.SETTINGS;

import android.util.Log;

/**
 * Created by metatomato on 18.12.14.
 */
public class SettingsHelper {

    public static float convertCurrent(int progress) {
        float value = progress * (SettingsFragment.CURRENT_MAX_VALUE
                - SettingsFragment.CURRENT_MIN_VALUE) / 100.f
                + SettingsFragment.CURRENT_MIN_VALUE;
        int step = Math.round(value / SettingsFragment.CURRENT_STEP_VALUE);
        return step * SettingsFragment.CURRENT_STEP_VALUE;
    }

    public static float convertGain(int progress) {
        float value = progress * (SettingsFragment.GAIN_MAX_VALUE
                - SettingsFragment.GAIN_MIN_VALUE) / 100.f
                + SettingsFragment.GAIN_MIN_VALUE;
        int step = Math.round(value / SettingsFragment.GAIN_STEP_VALUE);
        return step * SettingsFragment.GAIN_STEP_VALUE;
    }

    public static float convertSwipe(int progress) {
        float value = progress * (SettingsFragment.SWIPE_MAX_VALUE
                - SettingsFragment.SWIPE_MIN_VALUE) / 100.f
                + SettingsFragment.SWIPE_MIN_VALUE;
        int step = Math.round(value / SettingsFragment.SWIPE_STEP_VALUE);
        return step * SettingsFragment.SWIPE_STEP_VALUE;
    }

    public static int revertCurrent(float current) {
        float value = (current - SettingsFragment.CURRENT_MIN_VALUE)
                * 100.f/ (SettingsFragment.CURRENT_MAX_VALUE - SettingsFragment.CURRENT_MIN_VALUE);
        return Math.round(value);
    }

    public static int revertGain(float gain) {
        float value = (gain - SettingsFragment.GAIN_MIN_VALUE)
                * 100.f / ( SettingsFragment.GAIN_MAX_VALUE - SettingsFragment.GAIN_MIN_VALUE);
        return Math.round(value);
    }

    public static int revertSwipe(float swipe) {
        float value = (swipe - SettingsFragment.SWIPE_MIN_VALUE)
                * 100.f / ( SettingsFragment.SWIPE_MAX_VALUE - SettingsFragment.SWIPE_MIN_VALUE);
        return Math.round(value);
    }

    public static int calculateCurrent(float value) {
        double current = (0.25 / (value * 0.000001) - 4870.0) * 0.00128;
        return Math.round( (float) current);
    }

    public static int calculateGain(float value) {
        double gain = 8000.0 / ( value - 5 ) * 0.0512;
        return Math.round((float) gain);
    }

}
