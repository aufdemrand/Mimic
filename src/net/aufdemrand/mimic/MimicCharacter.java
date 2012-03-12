package net.aufdemrand.mimic;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import net.aufdemrand.mimic.Mimic;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;

public class MimicCharacter extends Character {

	private Mimic plugin;


	@Override
	public void load(DataKey arg0) throws NPCLoadException {
		// TODO Auto-generated method stub

	}

	@Override
	public void save(DataKey arg0) {
		// TODO Auto-generated method stub

	}

	@Override
    public void onRightClick(NPC npc, Player player) {
		
	    plugin = (Mimic) Bukkit.getServer().getPluginManager().getPlugin("Mimic");

		if (npc.getTrait(Owner.class).getOwner().equals(player.getName()) && npc.getCharacter().getName().equals("mimic")) {

			String theClue = plugin.getConfig().getString(npc.getId() + ".clue"); 
			String theType = plugin.getConfig().getString(npc.getId() + ".type"); 
			
			player.sendMessage(ChatColor.GOLD + "----- Mimic -----");
			player.sendMessage(ChatColor.GOLD + "  ID: " + npc.getId());
			player.sendMessage(ChatColor.GOLD + "  Name: " + npc.getName());
			if (theType == null) { player.sendMessage(ChatColor.GOLD + "  Type: Not set. Use /mimic set type [type].");  }
			else {	player.sendMessage(ChatColor.GOLD + "  Type: " + theType); }
			if (theClue == null) { player.sendMessage(ChatColor.GOLD + "  Clue: Not set. Use /mimic set clue [clue].");  }
			else {	player.sendMessage(ChatColor.GOLD + "  Clue: " + theClue); }
		}
	
		
		return;
	}

		
}

