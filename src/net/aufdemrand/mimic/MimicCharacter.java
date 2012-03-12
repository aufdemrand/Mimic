package net.aufdemrand.mimic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import net.aufdemrand.mimic.Mimic;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.character.Character;
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
	    
		
		String npcType = npc.getBukkitEntity().getType().toString();
			plugin.getServer().broadcastMessage("" + npcType);
			return;
	}

	
	@Override
	public void NPCSpawnEvent(NPC npc, Location location) { 

	return;
	}
}

