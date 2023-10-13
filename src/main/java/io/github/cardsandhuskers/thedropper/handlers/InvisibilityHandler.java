package io.github.cardsandhuskers.thedropper.handlers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.thedropper.TheDropper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class InvisibilityHandler implements Runnable{
    private TheDropper plugin;
    private PacketAdapter invisListener;
    private Integer assignedTaskId;

    public InvisibilityHandler(TheDropper plugin) {
        this.plugin = plugin;
    }

    /**
     * Sets correct players as glowing and enables the packet listener that will keep them glowing
     */


    public void disableInvis() {
        var protocolManager = ProtocolLibrary.getProtocolManager();
        if(invisListener != null) protocolManager.removePacketListener(invisListener);

        for(Team t: TeamHandler.getInstance().getTeams()) {
            for(Player p:t.getOnlinePlayers()) {
                ArrayList<Player> isInvis = getInvis(p);

                for(Player pl:isInvis) {
                    sendFakePacket(pl, p, (byte) 0x0);
                }

            }
        }
    }

    public void sendArtificialInvisPackets() {
        for(Team t: TeamHandler.getInstance().getTeams()) {
            for(Player p:t.getOnlinePlayers()) {
                ArrayList<Player> isGlowing = getInvis(p);
                for(Player pl:isGlowing) {
                    sendFakePacket(pl, p, (byte) 0x20);
                }
            }
        }
    }

    @Override
    public void run() {
        sendArtificialInvisPackets();
    }

    /**
     * Schedules this instance to run every tick
     */
    public void startOperation() {
        // Initialize our assigned task's id, for later use so we can cancel
        this.assignedTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 1L);
    }
    public void cancelOperation() {
        if (assignedTaskId != null) Bukkit.getScheduler().cancelTask(assignedTaskId);
    }

    /**
     * Gets the list of players that should be made invisible for the passed player
     * @param p
     * @return list of invis players
     */
    private ArrayList<Player> getInvis(Player p) {
        Team playerTeam = TeamHandler.getInstance().getPlayerTeam(p);
        if (playerTeam == null) return new ArrayList<>();

        ArrayList<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        for(Player teamPlayer:playerTeam.getOnlinePlayers()) {
            players.remove(teamPlayer);
        }
        return players;
    }

    /**
     *
     * @param target - target of the packet
     * @param recipient - recipient of the fake packet
     * @param type - packet type (0x20 is invis, 0x0 is not invis)
     */
    private void sendFakePacket(Player target, Player recipient, byte type) {
        var protocolManager = ProtocolLibrary.getProtocolManager();

        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, target.getEntityId()); //Set packet's entity id
        WrappedDataWatcher watcher = new WrappedDataWatcher(); //Create data watcher, the Entity Metadata packet requires this
        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class); //Found this through google, needed for some stupid reason
        watcher.setEntity(target); //Set the new data watcher's target
        packet.getDataValueCollectionModifier().write(0, List.of(
                new WrappedDataValue(0, WrappedDataWatcher.Registry.get(Byte.class), type)
        ));

        try {
            protocolManager.sendServerPacket(recipient, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
