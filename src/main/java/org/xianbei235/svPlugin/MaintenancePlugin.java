package org.xianbei235.svPlugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MaintenancePlugin extends JavaPlugin implements CommandExecutor {
    private boolean mode;
    private Set<String> whitelist = new HashSet<>();

    @Override
    public void onEnable() {
        loadConfig();
        this.getCommand("sv").setExecutor(this);
        if (mode) {
            startKickTask();
        }
    }

    private void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        mode = getConfig().getBoolean("mode");
        whitelist = new HashSet<>(getConfig().getStringList("whitelist"));
    }

    private void savePluginConfig() {
        getConfig().set("mode", mode);
        getConfig().set("whitelist", new ArrayList<>(whitelist));
        saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) && args.length > 0) {
            switch (args[0]) {
                case "on":
                    if (mode) {
                        sender.sendMessage("§c维护模式已经开启，无法开启");
                        return true;
                    }
                    mode = true;
                    Bukkit.broadcastMessage("§a维护模式已开启");
                    startKickTask();
                    savePluginConfig();
                    return true;
                case "off":
                    if (!mode) {
                        sender.sendMessage("§c维护模式已经关闭，无法重复关闭");
                        return true;
                    }
                    mode = false;
                    Bukkit.broadcastMessage("§c维护模式已关闭");
                    savePluginConfig();
                    return true;
                case "add":
                    if (args.length < 2) return false;
                    if (whitelist.contains(args[1])) {
                        sender.sendMessage(args[1] + "§c已经在白名单中");
                        return true;
                    }
                    whitelist.add(args[1]);
                    sender.sendMessage(args[1] + "§a已添加至白名单");
                    savePluginConfig();
                    kickNonWhitelistedPlayers();
                    return true;
                case "remove":
                    if (args.length < 2) return false;
                    whitelist.remove(args[1]);
                    sender.sendMessage(args[1] + "§c已移出白名单");
                    savePluginConfig();
                    kickNonWhitelistedPlayers();
                    return true;
                case "help":
                    sender.sendMessage("-- 欢迎使用MaintenancePlugin插件 --");
                    sender.sendMessage("插件By-  xianbei235");
                    sender.sendMessage("/sv on  - 开启维护模式");
                    sender.sendMessage("/sv off - 关闭维护模式");
                    sender.sendMessage("/sv add <玩家名> - 添加玩家到白名单");
                    sender.sendMessage("/sv remove <玩家名> - 从白名单移除玩家");
                    return true;
            }
        }
        return false;
    }

    private void kickNonWhitelistedPlayers() {
        String kickMessage = getConfig().getString("kickmsg", "§c服务器正在维护，请稍后再试");
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!whitelist.contains(player.getName()) && !player.isOp()) {
                player.kickPlayer(kickMessage);
            }
        }
    }

    private void startKickTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (mode) {
                    kickNonWhitelistedPlayers();
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(this, 0, 10);
    }
}

