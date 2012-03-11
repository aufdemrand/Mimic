package net.aufdemrand.mimic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.metadata.MetadataValue;

public class MimicListener implements Listener {

	Mimic plugin;
	public MimicListener(Mimic instance) { plugin = instance; }

	@EventHandler
	public void MimicListener(PlayerChatEvent event) {     // Now we're listening to chat.

		Player player = event.getPlayer();   // Get the player name

		Location PlayerLoc = player.getLocation();
		List<net.citizensnpcs.api.npc.NPC> MimicsInRange = getClosestMimics(PlayerLoc, player);

		if (MimicsInRange.isEmpty() == false) {

			for (int x=0; x< MimicsInRange.size(); x++){   // Do for each instance of Mimic that is in range

				net.citizensnpcs.api.npc.NPC thisMimic = MimicsInRange.get(x);   // Set specific mimic NPC data we're working with

				Random r = new Random();                 // Random number for a chance of the Mimic listening.
				int chanceofremembering = r.nextInt(3);  // 4 random numbers.
				if(chanceofremembering==0) {             // 25% chance of random number being 0.
					// player.sendMessage("*AAWWWK!*");     // Mimic hears the message.

					List<String> CurrentSavedMessages = plugin.getConfig().getStringList(thisMimic.getId() + ".savedmessages");

					if (CurrentSavedMessages.size() >= 10) {				
						Random s = new Random();             // Which key should we update?
						int whichmessage = s.nextInt(10);

						CurrentSavedMessages.set(whichmessage, event.getMessage());

						plugin.getConfig().set(thisMimic.getId() + ".savedmessages", CurrentSavedMessages);
						plugin.saveConfig();
					}

					else 

						CurrentSavedMessages.add(0, event.getMessage());
					plugin.getConfig().set(thisMimic.getId() + ".savedmessages", CurrentSavedMessages);
					plugin.saveConfig();	


				} 

			}
		}

	}

	public List<net.citizensnpcs.api.npc.NPC> getClosestMimics(Location PlayerLoc, Player player){  // Lets get the closest Mimic NPC entity data using the player entity data and the Player Location

		Collection<net.citizensnpcs.api.npc.NPC> MimicNPCs = CitizensAPI.getNPCManager().getNPCs(MimicCharacter.class); // Get a collection of Mimic NPCs to check against

		List<net.citizensnpcs.api.npc.NPC> MimicList = new ArrayList(MimicNPCs); // Turn Collection returned by Citizens into an Arraylist
		List<net.citizensnpcs.api.npc.NPC> MimicsInRange = new ArrayList();  // Initialize ArrayList of Mimics within config "chatter-range" of Player

		int NumberofMimicsInRange = 0;


		if (MimicNPCs.isEmpty() == false) {  // Let's do this ONLY IF we have Mimics actually in the world



			// Start Enhanced Loop to check location distance of Mimic NPCs to the Player

			for (int z=0; z< MimicList.size(); z++){   // Do for each instance of Mimic
				net.citizensnpcs.api.npc.NPC thisMimic = MimicList.get(z);   // Set specific mimic for comparison

				if (player.getWorld() == thisMimic.getBukkitEntity().getWorld()) {

					if (PlayerLoc.distance(thisMimic.getBukkitEntity().getLocation()) <= plugin.getConfig().getInt("chatter-range", 25)) {  // If Player distance is less than config "chatter-range" of thisMimic

						MimicsInRange.add(NumberofMimicsInRange, thisMimic);  // Then add this mimic to the arrayList MimicsInRange
						NumberofMimicsInRange++;
					}

				}
			} 




		}  // GREAT! Now we have all the Mimics in range!

		return MimicsInRange;            // Lets return them to the program 
	}


}