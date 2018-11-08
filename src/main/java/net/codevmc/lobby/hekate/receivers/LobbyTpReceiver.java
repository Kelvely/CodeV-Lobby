package net.codevmc.lobby.hekate.receivers;

import io.hekate.messaging.Message;
import io.hekate.messaging.MessageReceiver;
import net.codevmc.lobby.Lobby;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class LobbyTpReceiver implements MessageReceiver<String> {

    private final Lobby lobby;
    private final BukkitScheduler scheduler;

    public LobbyTpReceiver(Lobby lobby) {
        this.lobby = lobby;
        this.scheduler = lobby.getServer().getScheduler();
    }

    @Override
    public void receive(Message<String> msg) {

        String req = msg.get();

        switch(req) {
            case "acquire":
                Future<String> playerAmountFuture = scheduler.callSyncMethod(lobby, () -> Integer.toString(lobby.isBukkitInitialized() ? lobby.getServer().getOnlinePlayers().size() : -1));
                try {
                    msg.reply(playerAmountFuture.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                break;
        }

    }
}
