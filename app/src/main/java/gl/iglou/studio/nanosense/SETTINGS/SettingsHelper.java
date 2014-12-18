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

}
