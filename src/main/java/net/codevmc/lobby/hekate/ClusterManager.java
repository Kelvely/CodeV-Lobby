package net.codevmc.lobby.hekate;

import io.hekate.core.Hekate;
import io.hekate.core.HekateBootstrap;
import io.hekate.core.HekateFutureException;
import io.hekate.messaging.MessagingChannelConfig;
import io.hekate.messaging.MessagingServiceFactory;
import net.codevmc.lobby.Lobby;
import net.codevmc.lobby.hekate.receivers.LobbyTpReceiver;

public class ClusterManager {

    private final HekateBootstrap bootstrap;
    private final Lobby lobby;
    private Hekate hekate;

    public ClusterManager(String clusterName, String nodeName, Lobby lobby) {
        this.lobby = lobby;
        bootstrap = new HekateBootstrap().withClusterName(clusterName).withNodeName(nodeName).withLifecycleListener(hekate -> {
            if(hekate.state() == Hekate.State.UP) ClusterManager.this.hekate = hekate;
        });
        MessagingChannelConfig<String> lobbyJoinChannelConfig = MessagingChannelConfig.of(String.class).withName("lobby-join").withReceiver(new LobbyTpReceiver(lobby));
        MessagingChannelConfig<String> matchmakingChannelConfig = MessagingChannelConfig.of(String.class).withName("matchmaking");
        MessagingChannelConfig<String> homeJoinChannelConfig = MessagingChannelConfig.of(String.class).withName("home-join");
        MessagingServiceFactory messagingServiceFactory = new MessagingServiceFactory().withChannel(lobbyJoinChannelConfig);
        bootstrap.withService(messagingServiceFactory);
    }

    public void instantiate() throws HekateFutureException {
        try {
            bootstrap.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Hekate getHekate() {
        return hekate;
    }
}
