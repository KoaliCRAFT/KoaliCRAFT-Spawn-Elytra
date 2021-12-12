package ch.hekates.koalicraft.listeners;

import ch.hekates.koalicraft.Koalicraft;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.KeybindComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import java.util.ArrayList;
import java.util.List;

public class SpawnElytraListener implements Listener {

    private int maxBoosts;
    private int multiplyValue;
    private int spawnRadius;
    private int boosts = 0;
    private final List<Player> flying = new ArrayList<>();
    private final List<Player> boosted = new ArrayList<>();
    public SpawnElytraListener(Koalicraft plugin){

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            this.multiplyValue = plugin.getConfig().getInt("multiply-value");
            this.spawnRadius = plugin.getConfig().getInt("spawn-radius");
            this.maxBoosts = plugin.getConfig().getInt("max-boosts");

            Bukkit.getWorld("world").getPlayers().forEach(player -> {
                if (player.getGameMode() != GameMode.SURVIVAL) return;
                player.setAllowFlight(isInSpawnRadius(player));
                if (flying.contains(player) && !player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isAir()) {
                    player.setAllowFlight(false);
                    player.setGliding(false);
                    boosted.remove(player);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        flying.remove(player);
                    }, 5);
                }
            });
        }, 0, 3);

    }
    @EventHandler
    public void onDoubleJump(PlayerToggleFlightEvent event){
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
        if (!isInSpawnRadius(event.getPlayer())) return;
        event.setCancelled(true);
        event.getPlayer().setGliding(true);
        event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new ComponentBuilder("Drücke ")
                        .append(new KeybindComponent("key.swapOffhand"))
                        .append(" um dich zu boosten.")
                        .create());
        flying.add(event.getPlayer());
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.PLAYER
        && (event.getCause() == EntityDamageEvent.DamageCause.FALL || event.getCause() == EntityDamageEvent.DamageCause.FLY_INTO_WALL)
        && flying.contains(event.getEntity())) event.setCancelled(true);
        boosts = 0;
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (boosts >= maxBoosts) return;
        if (boosted.contains(event.getPlayer())) return;
        event.setCancelled(true);
        boosted.add(event.getPlayer());
        boosts ++;
        if (boosts >= maxBoosts) {
            event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§cDu hast alle deine Boosts aufgebraucht! (" + boosts + "/" + maxBoosts + ")"));
        } else {
            event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Du kannst dich in 10 Sekunden erneut boosten. " + boosts + "/" + maxBoosts));
        }
        event.getPlayer().setVelocity(event.getPlayer().getLocation().getDirection().multiply(multiplyValue));
        Bukkit.getScheduler().runTaskLater(Koalicraft.getPlugin(Koalicraft.class), () -> {
            boosted.remove(event.getPlayer());

        }, 200);
    }

    @EventHandler
    public void onToggleGlide(EntityToggleGlideEvent event) {
        if (event.getEntityType() == EntityType.PLAYER && flying.contains(event.getEntity())) event.setCancelled(true);
    }

    private boolean isInSpawnRadius(Player player){
        if (!player.getWorld().getName().equals("world")) return false;
        return player.getWorld().getSpawnLocation().distance(player.getLocation()) <= spawnRadius;
    }
}
