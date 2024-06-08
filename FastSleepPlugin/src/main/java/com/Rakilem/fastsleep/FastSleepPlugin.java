package com.Rakilem.fastsleep;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerBedEnterEvent;
import cn.nukkit.event.player.PlayerBedLeaveEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.utils.Config;

public class FastSleepPlugin extends PluginBase implements Listener {

    private TaskHandler taskHandler = null;
    private int sleepingPlayers = 0;
    private int timeIncreasePerPlayer;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);

        // Load configuration from config.yml
        this.saveDefaultConfig();
        this.reloadConfig();
        Config config = this.getConfig();

        // Read values from config.yml
        timeIncreasePerPlayer = config.getInt("timeIncreasePerPlayer", 100);
    }

    @EventHandler
    public void onPlayerEnterBed(PlayerBedEnterEvent event) {
        sleepingPlayers++;

        if (sleepingPlayers == 1) {
            taskHandler = getServer().getScheduler().scheduleRepeatingTask(this, new Runnable() {
                @Override
                public void run() {
                    int timeIncrease = timeIncreasePerPlayer * sleepingPlayers;

                    getServer().getLevels().forEach((name, level) -> {
                        if (level.getTime() >= 12542 && level.getTime() <= 23458) { // Only increase time during night
                            level.setTime(level.getTime() + timeIncrease); // Increase time by configured amount
                        }

                        // Teleport players to their bed when it's almost morning
                        if (level.getTime() >= 23000 && level.getTime() < 24000) {
                            level.getPlayers().values().forEach(player -> {
                                if (player.isSleeping()) {
                                    player.stopSleep();
                                    player.teleport(player.getSpawn().add(0, 1, 0)); // Teleport player above their bed
                                }
                            });
                        }
                    });
                }
            }, 20);
        }
    }

    @EventHandler
    public void onPlayerLeaveBed(PlayerBedLeaveEvent event) {
        sleepingPlayers--;

        if (sleepingPlayers <= 0 && taskHandler != null) {
            taskHandler.cancel();
            taskHandler = null;
        }
    }
}
