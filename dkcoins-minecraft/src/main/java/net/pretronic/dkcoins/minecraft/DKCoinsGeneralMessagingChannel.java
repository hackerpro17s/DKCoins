package net.pretronic.dkcoins.minecraft;

import net.pretronic.dkcoins.common.DefaultDKCoins;
import net.pretronic.libraries.document.Document;
import org.mcnative.runtime.api.network.messaging.MessageReceiver;
import org.mcnative.runtime.api.network.messaging.MessagingChannelListener;

import java.util.UUID;

public class DKCoinsGeneralMessagingChannel implements MessagingChannelListener {

    @Override
    public Document onMessageReceive(MessageReceiver sender, UUID requestId, Document request) {
        String action = request.getString("action");
        if(action.equalsIgnoreCase(DKCoinsMessagingChannelAction.CLEAR_CACHES)) {
            DefaultDKCoins.getInstance().getAccountManager().clearCaches();
        }
        return Document.newDocument();
    }
}
