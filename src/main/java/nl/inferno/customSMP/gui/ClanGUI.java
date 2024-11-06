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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClanGUI {
    private final CustomSMP plugin;
    private final Inventory inventory;
    private final Player player;
    private final Clan clan;

    public ClanGUI(Player player) {
        this.plugin = CustomSMP.getInstance();
        this.player = player;
        this.clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        this.inventory = Bukkit.createInventory(null, 54, "Clan Management");
        initializeItems();
    }

    private void initializeItems() {
        // Clan Info
        inventory.setItem(13, createGuiItem(Material.SHIELD, "&b" + clan.getName(),
                "&7Tag: &f" + clan.getTag(),
                "&7Level: &f" + clan.getLevel(),
                "&7Experience: &f" + clan.getExperience() + "/" + plugin.getLevelSystem().getRequiredExperience(clan.getLevel() + 1),
                "&7Members: &f" + clan.getMembers().size() + "/" + clan.getMaxMembers()
        ));

        // Members Management
        inventory.setItem(28, createMembersItem());

        // Bank Management
        inventory.setItem(30, createBankItem());

        // Territory Management
        inventory.setItem(32, createTerritoryItem());

        // Settings
        inventory.setItem(34, createSettingsItem());

        // Fill empty slots with glass panes
        fillEmptySlots();
    }

    private ItemStack createMembersItem() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Leden Beheren");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Online leden: " + getOnlineMembersCount(),
                ChatColor.GRAY + "Klik om leden te beheren"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createBankItem() {
        return createGuiItem(Material.GOLD_INGOT, "&6Bank Beheren",
                "&7Balans: &f" + clan.getBalance(),
                "&7Klik om de bank te beheren"
        );
    }

    private ItemStack createTerritoryItem() {
        return createGuiItem(Material.COMPASS, "&eTerritories",
                "&7Geclaimde chunks: &f" + getClaimedChunksCount(),
                "&7Klik om territories te beheren"
        );
    }

    private ItemStack createSettingsItem() {
        return createGuiItem(Material.REDSTONE_TORCH, "&cInstellingen",
                "&7Klik om clan instellingen te wijzigen"
        );
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

    private void fillEmptySlots() {
        ItemStack filler = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    private int getOnlineMembersCount() {
        return (int) clan.getMembers().stream()
                .filter(uuid -> Bukkit.getPlayer(uuid) != null)
                .count();
    }

    private int getClaimedChunksCount() {
        return plugin.getTerritorySystem().getClaimedChunksCount(clan);
    }

    public void open() {
        player.openInventory(inventory);
    }
}
