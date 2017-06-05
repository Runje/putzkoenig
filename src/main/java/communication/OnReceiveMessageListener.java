package communication;

import com.example.communication.model.communication.messages.KoenigsputzMessage;

/**
 * Created by Thomas on 17.01.2017.
 */
public interface OnReceiveMessageListener
{
    void onReceiveMessage(KoenigsputzMessage message);
}
