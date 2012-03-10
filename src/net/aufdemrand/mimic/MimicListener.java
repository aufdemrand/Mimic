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
	public void ParrotListener(PlayerChatEvent event) {     // Now we're listening to chat.

		Player player = event.getPlayer();   // Get the player name

		Location PlayerLoc = player.getLocation();
		List<net.citizensnpcs.api.npc.NPC> ParrotsInRange = getClosestParrots(PlayerLoc, player);

		if (ParrotsInRange.isEmpty() == false) {

			for (int x=0; x< ParrotsInRange.size(); x++){   // Do for each instance of Parrot that is in range

				net.citizensnpcs.api.npc.NPC thisParrot = ParrotsInRange.get(x);   // Set specific parrot NPC data we're working with

				Random r = new Random();                 // Random number for a chance of the Parrot listening.
				int chanceofremembering = r.nextInt(3);  // 4 random numbers.
				if(chanceofremembering==0) {             // 25% chance of random number being 0.
					// player.sendMessage("*AAWWWK!*");     // Parrot hears the message.

					List<String> CurrentSavedMessages = plugin.getConfig().getStringList(thisParrot.getId() + ".savedmessages");

					if (CurrentSavedMessages.size() >= 10) {				
						Random s = new Random();             // Which key should we update?
						int whichmessage = s.nextInt(10);

						CurrentSavedMessages.set(whichmessage, event.getMessage());

						plugin.getConfig().set(thisParrot.getId() + ".savedmessages", CurrentSavedMessages);
						plugin.saveConfig();
					}

					else 

						CurrentSavedMessages.add(0, event.getMessage());
					plugin.getConfig().set(thisParrot.getId() + ".savedmessages", CurrentSavedMessages);
					plugin.saveConfig();	


				} 

			}
		}

	}

	public List<net.citizensnpcs.api.npc.NPC> getClosestParrots(Location PlayerLoc, Player player){  // Lets get the closest Parrot NPC entity data using the player entity data and the Player Location

		Collection<net.citizensnpcs.api.npc.NPC> ParrotNPCs = CitizensAPI.getNPCManager().getNPCs(MimicCharacter.class); // Get a collection of Parrot NPCs to check against

		List<net.citizensnpcs.api.npc.NPC> ParrotList = new ArrayList(ParrotNPCs); // Turn Collection returned by Citizens into an Arraylist
		List<net.citizensnpcs.api.npc.NPC> ParrotsInRange = new ArrayList();  // Initialize ArrayList of Parrots within config "chatter-range" of Player

		int NumberofParrotsInRange = 0;


		if (ParrotNPCs.isEmpty() == false) {  // Let's do this ONLY IF we have Parrots actually in the world



			// Start Enhanced Loop to check location distance of Parrot NPCs to the Player

			for (int z=0; z< ParrotList.size(); z++){   // Do for each instance of Parrot
				net.citizensnpcs.api.npc.NPC thisParrot = ParrotList.get(z);   // Set specific parrot for comparison

				if (player.getWorld() == thisParrot.getBukkitEntity().getWorld()) {

					if (PlayerLoc.distance(thisParrot.getBukkitEntity().getLocation()) <= plugin.getConfig().getInt("chatter-range", 25)) {  // If Player distance is less than config "chatter-range" of thisParrot

						ParrotsInRange.add(NumberofParrotsInRange, thisParrot);  // Then add this parrot to the arrayList ParrotsInRange
						NumberofParrotsInRange++;
					}

				}
			} 




		}  // GREAT! Now we have all the Parrots in range!

		return ParrotsInRange;            // Lets return them to the program 
	}


}