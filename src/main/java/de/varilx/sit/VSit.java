package de.varilx.sit;

import de.varilx.BaseAPI;
import de.varilx.BaseSpigotAPI;
import de.varilx.configuration.VaxConfiguration;
import de.varilx.sit.command.SitCommand;
import de.varilx.sit.listener.BlockSitListener;
import de.varilx.sit.listener.PlayerSitListener;
import de.varilx.utils.language.LanguageUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public final class VSit extends JavaPlugin {

    @Override
    public void onEnable() {
        for (World world : Bukkit.getWorlds()) {
            for (ArmorStand armorStand : world.getEntitiesByClass(ArmorStand.class)) {
                if (!armorStand.getPersistentDataContainer().has(new NamespacedKey(this, "sit"))) continue;
                armorStand.remove();
            }
        }

        new BaseSpigotAPI(this, 24310).enable();

        Bukkit.getPluginManager().registerEvents(new BlockSitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerSitListener(), this);

        new SitCommand(this);

        Bukkit.getServer().sendMessage(LanguageUtils.getMessage("startup"));
    }

    public void sitDown(Player player, Block block, boolean command) {
        if (block.getRelative(BlockFace.UP).getType().isCollidable()) return;
        Location loc = block.getLocation().add(0.5, (command ? 0.2:0)+getHeight(block), 0.5);
        loc.setYaw(getNewStandYaw(player));
        ArmorStand armorStand = block.getWorld().spawn(loc, ArmorStand.class, (stand) -> {
            stand.setCanMove(false);
            stand.setInvisible(true);
            stand.setInvulnerable(true);
            stand.setSmall(true);
            stand.getPersistentDataContainer().set(new NamespacedKey(this, "sit"), PersistentDataType.BOOLEAN, true);
        });
        armorStand.addPassenger(player);
    }

    private double getHeight(@Nullable Block clickedBlock) {
        return clickedBlock.getBoundingBox().getHeight()-1.7; // get height of block and adjust for the armor stand
    }
    private float getNewStandYaw(Player player) {
        float yaw = player.getLocation().getYaw() + 180; // get yaw facing opposite the player
        return Math.round( ((yaw > 180) ? yaw : yaw-360) / 90) * 90; // make sure it isn't over 180 and round to nearest 90 degrees
    }

}
