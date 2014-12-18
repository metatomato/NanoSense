package gl.iglou.studio.nanosense.BT;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by metatomato on 17.12.14.
 */
public class BTDataConverter {

    private static final String TAG = BTFragment.TAG;

    public static byte[] getBytes(ByteBuffer buffer, int size) {
        buffer.position(0);
        byte[] data = new byte[size];
        buffer.get(data,0,size);
        return data;
    }

    static public void getDoublesFromBuffer(ByteBuffer buffer, int size) {
        int doubleNum = size/8;
        if(doubleNum >= 1) {
            buffer.position(0);
            double[] doubles = new double[doubleNum];
            buffer.asDoubleBuffer().get(doubles, 0, doubleNum);
            for (Double d : doubles) {
                Log.d(TAG, "RECEIVED double: " + String.valueOf(d));
            }
        } else {
            Log.d(TAG,"Not enough data to decode double");
        }
    }

    public static void getFloatsFromBuffer(ByteBuffer buffer, int size) {
        int floatNum = size/4;
        if(floatNum >= 1) {
            buffer.position(0);
            float[] floats = new float[floatNum];
            buffer.asFloatBuffer().get(floats, 0, floatNum);
            for (float f : floats) {
                Log.d(TAG, "RECEIVED float: " + String.valueOf(f));
            }
        } else {
            Log.d(TAG,"Not enough data to decode float");
        }
    }

    public static String getStringFromBuffer(ByteBuffer buffer, int size, String charset) {
        byte[] message = new byte[size];
        buffer.position(0);
        buffer.get(message, 0, size);
        String finalMessage = new String(message, Charset.forName(charset));
        return finalMessage;
        //Log.d(TAG,"RECEIVED string: " + finalMessage);
    }

    public static String decodeMessage(byte[] msg, String charSet) {
        String v = new String( msg, Charset.forName(charSet) );
        //Log.d(TAG,"Value decode for " + charSet +": "+ v);
        return v;
    }
}
