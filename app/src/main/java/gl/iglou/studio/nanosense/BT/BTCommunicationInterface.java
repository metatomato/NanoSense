package gl.iglou.studio.nanosense.BT;

/**
 * Created by metatomato on 17.12.14.
 */
public interface BTCommunicationInterface {

    public void sendMessage(String message);

    public boolean isRemoteConnected();
}
