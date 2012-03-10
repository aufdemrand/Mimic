package net.aufdemrand.mimic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.character.CharacterFactory;
import net.citizensnpcs.api.trait.trait.Owner;
import net.aufdemrand.mimic.MimicCharacter;
import net.aufdemrand.mimic.MimicListener;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class Mimic extends JavaPlugin {

	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be in-game to execute commands.");
			return true;
		}

		if (args.length < 1) {
			sender.sendMessage(ChatColor.RED + "Use /mimic help for command reference.");
			return true;
		}

		Player player = (Player) sender;

		if (args[0].equalsIgnoreCase("help")) {
			player.sendMessage(ChatColor.GOLD + "----- Mimic -----");
			player.sendMessage(ChatColor.GREEN + "/parrot clue" + ChatColor.GRAY
					+ " -- Sets the clue for the mimic.");
			return true;
		} 

		if (player.getMetadata("selected").isEmpty()) { 
			player.sendMessage(ChatColor.RED + "You must have a Mimic selected.");
			return true;
		}


		// Basics over, NPC Selected... but which one?

		int NPCSelected = player.getMetadata("selected").get(0).asInt();  // Gets NPC Citizens ID of selected
		NPC ThisNPC = CitizensAPI.getNPCManager().getNPC(player.getMetadata("selected").get(0).asInt());      // Gets NPC Citizens Entity of Selected
		Entity ThisBukkitNPC = ThisNPC.getBukkitEntity();  // Gets Bukkit Entity of Selected


		// Parrot Selected... now what?

		if (!ThisNPC.getTrait(Owner.class).getOwner().equals(player.getName())) {
			player.sendMessage(ChatColor.RED + "You must be the owner of the mimic to execute commands.");
			return true;
		}

		if (ThisNPC.getCharacter() == null || !ThisNPC.getCharacter().getName().equals("mimic")) {
			player.sendMessage(ChatColor.RED + "That command must be performed on a mimic!");
			return true;
		}

		// Commands

		if (args[0].equalsIgnoreCase("save")) {
			player.sendMessage("Settings saved.");
			saveConfig();
			return true;
		}
		else if (args[0].equalsIgnoreCase("clue")) {

			String NewClue = "";
			for(int i = 1; i < args.length; i++) { if (i != args.length - 1) {NewClue = NewClue + args[i] + " ";} else {NewClue = NewClue + args[i];} }

			getConfig().set(ThisNPC.getId() + ".clue", NewClue.toString());  // Set the key in the config.
			saveConfig();                   // Save the config.
			player.sendMessage(ChatColor.GREEN + "Clue saved.");   // Talk to the player.

			return true;
		}

		return true;
	}

	@Override
	public void onDisable() {
		getLogger().log(Level.INFO, " v" + getDescription().getVersion() + " disabled.");
		
	}

	@Override
	public void onEnable() {

		this.getConfig().options().copyDefaults(true);
		saveConfig();  

		
		CitizensAPI.getCharacterManager().registerCharacter(new CharacterFactory(MimicCharacter.class).withName("parrot").withTypes(EntityType.CHICKEN));
		getServer().getPluginManager().registerEvents(new MimicListener(this), this);

		int ParrotCooldown = getConfig().getInt("cooldown-in-seconds", 20);



		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {

			public void run() {

				Collection<net.citizensnpcs.api.npc.NPC> ParrotNPCs = CitizensAPI.getNPCManager().getNPCs(MimicCharacter.class); // Get a collection of Parrot NPCs to check against
				List<net.citizensnpcs.api.npc.NPC> ParrotList = new ArrayList(ParrotNPCs); // Turn Collection returned by Citizens into an Arraylist to easily work with
				List<net.citizensnpcs.api.npc.NPC> ParrotsInRange = new ArrayList();  // Initialize ArrayList of Parrots within config "chatter-range" of Player

				if (ParrotNPCs.isEmpty() == false) {  // Let's do this ONLY IF we have Parrots actually in the world...

					// Start Enhanced Loop to check location distance of Parrot NPCs to the Player

					for (int z=0; z< ParrotList.size(); z++){   // Do for each instance of Parrot

						net.citizensnpcs.api.npc.NPC thisParrot = ParrotList.get(z);   // Set specific parrot for comparison
						List<Player> PlayersInRangeofParrot = new ArrayList(); // List of players

						int PlayerCount = 0; // Used for counting players, sending messages, etc.

						for(Player player : getServer().getOnlinePlayers()) {
							Location playerLocation = player.getLocation();						

							if (player.getWorld() == thisParrot.getBukkitEntity().getWorld()) {  // Check if they are in the same world.

								if (playerLocation.distance(thisParrot.getBukkitEntity().getLocation()) <= getConfig().getInt("chatter-range", 25)) { PlayersInRangeofParrot.add(PlayerCount, player); } // If Player distance is less than config "chatter-range" of thisParrot

							}

						} // End getting players in range of Parrot .. now stored in PlayersInRangeofParrot

						// Now, which messages to send to the players in range?
						// Lets say, 20% chance of being the clue, and 20% chance of being Parrot sounds, and 60% chance of being a stored message

						if (!PlayersInRangeofParrot.isEmpty()) {  // Only doing if players are in range of a Parrot

							Random u = new Random(); //
							int WhatShouldWeDo = u.nextInt(10);

							if (WhatShouldWeDo == 0 || WhatShouldWeDo == 1) { // Display the clue! 
								String theMessage = getConfig().getString(thisParrot.getId() + ".clue");  // Get the clue specific to the parrot
								if (theMessage != null) { for (int w=0; w< PlayersInRangeofParrot.size(); w++){ PlayersInRangeofParrot.get(w).sendMessage("A parrot squaks, '" + theMessage + "'"); }} 						
							}

							else if (WhatShouldWeDo == 2 || WhatShouldWeDo == 3) { // PARROT SOUNDS! 
								List<String> ParrotSounds = getConfig().getStringList("chatter");
								Random t = new Random();             // Which sound should we read?
								int whichmessage = t.nextInt(ParrotSounds.size());
								if (ParrotSounds != null) { for (int w=0; w< PlayersInRangeofParrot.size(); w++){ PlayersInRangeofParrot.get(w).sendMessage(ParrotSounds.get(whichmessage).toString()); }} 						
							}

							else { // Stored Message coming through.... Parrot-style
								List<String> StoredParrotMessages = getConfig().getStringList(thisParrot.getId() + ".savedmessages");
								Random t = new Random();             // Which key should we read?
								if (StoredParrotMessages.size() > 0) { 
									int whichmessage = t.nextInt(StoredParrotMessages.size());
									for (int w=0; w< PlayersInRangeofParrot.size(); w++){ PlayersInRangeofParrot.get(w).sendMessage("A parrot squaks, '" + StoredParrotMessages.get(whichmessage).toString() + "'"); }}
								else { // No stored message? Parrot sounds instead.

									List<String> ParrotSounds = getConfig().getStringList("chatter");
									Random c = new Random();             // Which sound should we read?
									int whichmessagenow = c.nextInt(ParrotSounds.size());
									// getServer().broadcastMessage("" + ParrotSounds.size());  // Debug
									if (ParrotSounds != null) { for (int w=0; w< PlayersInRangeofParrot.size(); w++){ PlayersInRangeofParrot.get(w).sendMessage(ParrotSounds.get(whichmessagenow).toString()); }}

								} 						
							}

						} // END Players Empty Check

					} // END PLAYERS IN RANGE

				} // END FOR EACH PARROT

			} // END CHECK OF PARROTS

		}, ParrotCooldown * 20, ParrotCooldown * 20);

		getLogger().log(Level.INFO, " v" + getDescription().getVersion() + " enabled.");

	}

}