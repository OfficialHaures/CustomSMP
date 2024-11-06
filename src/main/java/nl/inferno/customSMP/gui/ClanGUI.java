package nl.inferno.customSMP.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClanGUI {
    private final Inventory inventory;
    private final Player player;

    public ClanGUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, "Clan Management");
        initializeItems();
    }

    private void initializeItems() {
        inventory.setItem(13, createGuiItem(Material.BLACK_BANNER, "&bClan Info",
            "&7Naam: &f" + getClanName(),
            "&7Level: &f" + getClanLevel()));

        inventory.setItem(29, createGuiItem(Material.PLAYER_HEAD, "&aLeden Beheren"));
        inventory.setItem(31, createGuiItem(Material.GOLD_INGOT, "&6Bank Beheren"));
        inventory.setItem(33, createGuiItem(Material.COMPASS, "&eClan Home"));
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        List<String> coloredLore = Arrays.stream(lore)
            .map(line -> ChatColor.translateAlternateColorCodes('&', line))
            .collect(Collectors.toList());

        meta.setLore(coloredLore);
        item.setItemMeta(meta);
        return item;
    }
}
