package nl.inferno.customSMP.gui;

import nl.inferno.customSMP.CustomSMP;
import nl.inferno.customSMP.models.Clan;
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
    private final CustomSMP plugin;
    private final Inventory inventory;
    private final Player player;

    public ClanGUI(Player player) {
        this.plugin = CustomSMP.getInstance();
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, "Clan Management");
        initializeItems();
    }

    private void initializeItems() {
        Clan playerClan = plugin.getClanManager().getPlayerClan(player.getUniqueId());

        if (playerClan != null) {
            // Player is in a clan
            inventory.setItem(13, createGuiItem(Material.BLACK_BANNER, "&bClan Info",
                    "&7Naam: &f" + playerClan.getName(),
                    "&7Tag: &f" + playerClan.getTag(),
                    "&7Level: &f" + playerClan.getLevel(),
                    "&7Experience: &f" + playerClan.getExperience(),
                    "&7Leden: &f" + playerClan.getMembers().size() + "/" + playerClan.getMaxMembers(),
                    "&7Bank: &f" + playerClan.getBalance()));

            // Clan management items
            inventory.setItem(29, createGuiItem(Material.PLAYER_HEAD, "&aLeden Beheren",
                    "&7Klik om leden te beheren"));
            inventory.setItem(31, createGuiItem(Material.GOLD_INGOT, "&6Bank Beheren",
                    "&7Balans: &f" + playerClan.getBalance(),
                    "&7Klik om de bank te beheren"));
            inventory.setItem(33, createGuiItem(Material.COMPASS, "&eClan Home",
                    "&7Klik om naar de clan home te gaan"));

            // Additional clan options
            if (playerClan.isLeader(player.getUniqueId())) {
                inventory.setItem(45, createGuiItem(Material.BARRIER, "&cClan Opheffen",
                        "&7Klik om de clan op te heffen"));
                inventory.setItem(47, createGuiItem(Material.NAME_TAG, "&eNaam Wijzigen",
                        "&7Klik om de clan naam te wijzigen"));
                inventory.setItem(51, createGuiItem(Material.PAPER, "&eTag Wijzigen",
                        "&7Klik om de clan tag te wijzigen"));
            }
        } else {
            // Player is not in a clan
            inventory.setItem(13, createGuiItem(Material.GRAY_BANNER, "&bGeen Clan",
                    "&7Je zit momenteel niet in een clan",
                    "&7Gebruik &f/clan create &7om er een te maken"));

            // Create clan button
            inventory.setItem(31, createGuiItem(Material.EMERALD, "&aMaak een Clan",
                    "&7Klik om een nieuwe clan te maken"));
        }

        // Fill empty slots with glass panes
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " "));
            }
        }
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

    public void open() {
        player.openInventory(inventory);
    }
}
