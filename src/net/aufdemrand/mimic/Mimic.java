package net.aufdemrand.mimic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Mimic extends JavaPlugin {

	// COMMANDS

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
		
		if (!player.isOp() || !player.hasPermission("mimic.create")) {
			player.sendMessage(ChatColor.GOLD + "You do not have permission to create or modify Mimics.");
			return true;
		}

		if (args[0].equalsIgnoreCase("help")) {
			player.sendMessage(ChatColor.GOLD + "----- Mimic -----");
			player.sendMessage(ChatColor.GOLD + "/mimic view types");
			player.sendMessage(ChatColor.GOLD + "  -- Views the types of mimics available.");
			player.sendMessage(ChatColor.GOLD + "/mimic set type [type]");
			player.sendMessage(ChatColor.GOLD + " -- Sets the type of the mimic.");
			player.sendMessage(ChatColor.GOLD + "/mimic set clue [clue]");
			player.sendMessage(ChatColor.GOLD + "  -- Sets the clue for the mimic.");
			player.sendMessage(ChatColor.GOLD + "/mimic view clue");
			player.sendMessage(ChatColor.GOLD + "  -- Views the clue for the mimic.");
			player.sendMessage(ChatColor.GOLD + "/mimic reset messages");
			player.sendMessage(ChatColor.GOLD + "  -- Resets the messages stored in the mimic.");
			return true;
		} 

		if (player.getMetadata("selected").isEmpty()) { 
			player.sendMessage(ChatColor.RED + "You must have a Mimic selected.");
			return true;
		}

		// Basics over, NPC Selected... but which one?

		NPC ThisNPC = CitizensAPI.getNPCManager().getNPC(player.getMetadata("selected").get(0).asInt());      // Gets NPC Citizens Entity of Selected

		// Citizens NPC Selected... now what?

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
			player.sendMessage(ChatColor.GOLD + "Settings saved.");
			saveConfig();
			return true;
		}
		else if (args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("clue")) {

			String NewClue = "";
			for(int i = 2; i < args.length; i++) { if (i != args.length - 1) {NewClue = NewClue + args[i] + " ";} else {NewClue = NewClue + args[i];} }

			getConfig().set(ThisNPC.getId() + ".clue", NewClue.toString());  // Set the key in the config.
			saveConfig();                   // Save the config.
			player.sendMessage(ChatColor.GOLD + "Clue saved.");   // Talk to the player.

			return true;
		}

		else if (args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("type")) {

			String NewType = "";
			for(int i = 2; i < args.length; i++) { if (i != args.length - 1) {NewType = NewType + args[i] + " ";} else {NewType = NewType + args[i];} }

			List<String> ValididyCheck = getConfig().getStringList("voice types.list");

			boolean isTypeValid = false;

			for (int x = 0; x < ValididyCheck.size(); x++) { if (NewType.equalsIgnoreCase(ValididyCheck.get(x))) { isTypeValid = true; NewType = ValididyCheck.get(x);} }

			if (isTypeValid == true) {

				getConfig().set(ThisNPC.getId() + ".type", NewType);  // Set the key in the config.
				saveConfig();                   // Save the config.
				player.sendMessage(ChatColor.GOLD + "Type set to " + NewType + ".");   // Talk to the player.

				return true;
			}


			else { player.sendMessage(ChatColor.RED + NewType + " is an invalid Mimic type."); }
		}


		else if (args[0].equalsIgnoreCase("view") && args[1].equalsIgnoreCase("types")) {

			List<String> TypesCheck = getConfig().getStringList("voice types.list");

			String TypeString = "";

			for (int x = 0; x < TypesCheck.size(); x++) { if (x != args.length - 1) {TypeString = TypeString + args[x] + ", ";} else {TypeString = TypeString + args[x];}  }


			player.sendMessage(ChatColor.GOLD + "Available types: " + TypeString + ".");   // Talk to the player.

			return true;
		}

		else if (args[0].equalsIgnoreCase("view") && args[1].equalsIgnoreCase("clue")) {

			String theClue = getConfig().getString(ThisNPC.getId() + ".clue"); 

			if (theClue == null) { player.sendMessage(ChatColor.GOLD + "Clue not set. Use /mimic set clue [clue].");  }
			else {	player.sendMessage(ChatColor.GOLD + "Clue: " + theClue); }

			return true;
		}

		
		else if (args[0].equalsIgnoreCase("reset") && args[1].equalsIgnoreCase("messages")) {

			
			getConfig().set(ThisNPC.getId() + ".messages", null);
			String theClue = getConfig().getString(ThisNPC.getId() + ".clue"); 

			if (theClue == null) { player.sendMessage(ChatColor.GOLD + "Clue not set. Use /mimic set clue [clue].");  }
			else {	player.sendMessage(ChatColor.GOLD + "Clue: " + theClue); }

			return true;
		}


		return true;
	}


	@Override
	public void onDisable() {
		getLogger().log(Level.INFO, " v" + getDescription().getVersion() + ": Settings Saved." );
		getLogger().log(Level.INFO, " v" + getDescription().getVersion() + ": Sucessfully Unloaded.");
	}

	public int ConfigCooldownInSeconds;
	public int ConfigChatterRange;
	public int ConfigClickCooldownInSeconds;
	public boolean ConfigUseSmartWrap;
	public String ConfigTextStyle;
	public List<String> ConfigTypes;

	@Override
	public void onEnable() {

		getLogger().log(Level.INFO, " v" + getDescription().getVersion() + ": Loading Config." );

		getConfig().options().copyDefaults(true);

		int ConfigCooldownInSeconds = getConfig().getInt("cooldown-in-seconds");
		int ConfigClickCooldownInSeconds = getConfig().getInt("click-cooldown-in-seconds", 5);
		final int ConfigChatterRange = getConfig().getInt("chatter-range", 25);
		boolean ConfigUseSmartWrap = getConfig().getBoolean("use-smart-wrap", true);
		String ConfigTextStyle = getConfig().getString("text-style", "Normal");

		getLogger().log(Level.INFO, " v" + getDescription().getVersion() + ": Loading Character." );
		CitizensAPI.getCharacterManager().registerCharacter(new CharacterFactory(MimicCharacter.class).withName("mimic"));

		getLogger().log(Level.INFO, " v" + getDescription().getVersion() + ": Loading Listener." );
		getServer().getPluginManager().registerEvents(new MimicListener(this), this);

		getLogger().log(Level.INFO, " v" + getDescription().getVersion() + ": Loading Chatter Engine." );
		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {

			public void run() {

				Collection<net.citizensnpcs.api.npc.NPC> MimicNPCs = CitizensAPI.getNPCManager().getNPCs(MimicCharacter.class); // Get a collection of Mimic NPCs to check against
				List<net.citizensnpcs.api.npc.NPC> MimicList = new ArrayList<NPC>(MimicNPCs); // Turn Collection returned by Citizens into an Arraylist to easily work with

				if (MimicNPCs.isEmpty() == false) {  // Let's do this ONLY IF we have Mimics actually in the world...

					// Start Enhanced Loop to check location distance of Mimic NPCs to the Player

					for (int z=0; z< MimicList.size(); z++){   // Do for each instance of Mimic

						net.citizensnpcs.api.npc.NPC thisMimic = MimicList.get(z);   // Set specific mimic for comparison
						List<Player> PlayersInRangeofMimic = new ArrayList<Player>(); // List of players

						int PlayerCount = 0; // Used for counting players, sending messages, etc.

						for(Player player : getServer().getOnlinePlayers()) {
							Location playerLocation = player.getLocation();						

							if (player.getWorld() == thisMimic.getBukkitEntity().getWorld()) {  // Check if they are in the same world.

								if (playerLocation.distance(thisMimic.getBukkitEntity().getLocation()) <= ConfigChatterRange) { PlayersInRangeofMimic.add(PlayerCount, player); } // If Player distance is less than config "chatter-range" of thisMimic

							}

						} // End getting players in range of Mimic .. now stored in PlayersInRangeofMimic

						// Now, which messages to send to the players in range?

						if (!PlayersInRangeofMimic.isEmpty()) {  // Only doing if players are in range of a Mimic

							String MimicType = getConfig().getString(thisMimic.getId() + ".type");

							if (MimicType != null) {
								// Get type of mimic for chatter

								//					getServer().broadcastMessage("" + MimicType);
								List<String> MimicTypeMessages = getConfig().getStringList("talk types." + MimicType + ".texts");

								if (MimicTypeMessages.isEmpty() == false) {

									Random u = new Random(); //
									int WhatShouldWeDo = u.nextInt(MimicTypeMessages.size());

									Random v = new Random();
									int ClueOrMessage = v.nextInt(4);

									// Stored Message coming through.... Mimic-style
									List<String> StoredMimicMessages = getConfig().getStringList(thisMimic.getId() + ".savedmessages");

									if (!StoredMimicMessages.isEmpty()) {

										Random t = new Random();             // Which key should we read?
										int whichmessage = t.nextInt(StoredMimicMessages.size()); 

										String theMessage;

										if (ClueOrMessage == 0) { // do clue 								

											String theClue = getConfig().getString(thisMimic.getId() + ".clue");

											if (theClue == null) { theMessage = MimicTypeMessages.get(WhatShouldWeDo).toString().replace("<text>", StoredMimicMessages.get(whichmessage).toString()); }
											else {theMessage = MimicTypeMessages.get(WhatShouldWeDo).toString().replace("<text>", getConfig().getString(thisMimic.getId() + ".clue"));	}
										}
										else { // do message
											theMessage = MimicTypeMessages.get(WhatShouldWeDo).toString().replace("<text>", StoredMimicMessages.get(whichmessage).toString());
										}


										if (theMessage != null) {

											//			theMessage = WordWrapper(theMessage);

											for (int w=0; w< PlayersInRangeofMimic.size(); w++) { 
												PlayersInRangeofMimic.get(w).sendMessage(theMessage); 
											}
										}
									}
								}
							}
						} // END Players Empty Check

					} // END PLAYERS IN RANGE

				} // END FOR EACH PARROT

			} // END CHECK OF PARROTS

		}, ConfigCooldownInSeconds * 20, ConfigCooldownInSeconds * 20);

		getLogger().log(Level.INFO, " v" + getDescription().getVersion() + ": Sucessfully Enabled." );
	}
}

