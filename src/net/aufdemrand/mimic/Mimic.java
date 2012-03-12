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

		if (args[0].equalsIgnoreCase("help")) {
			player.sendMessage(ChatColor.GOLD + "----- Mimic -----");
			player.sendMessage(ChatColor.GRAY + "/mimic view types");
			player.sendMessage(ChatColor.GRAY + "  -- Views the types of mimics available.");
			player.sendMessage(ChatColor.GRAY + "/mimic set type [type]");
			player.sendMessage(ChatColor.GRAY + " -- Sets the type of the mimic.");
			player.sendMessage(ChatColor.GRAY + "/mimic set clue [clue]");
			player.sendMessage(ChatColor.GRAY + "  -- Sets the clue for the mimic.");
			player.sendMessage(ChatColor.GRAY + "/mimic view clue");
			player.sendMessage(ChatColor.GRAY + "  -- Views the clue for the mimic.");
			player.sendMessage(ChatColor.GRAY + "/mimic view messages");
			player.sendMessage(ChatColor.GRAY + "  -- Views the messages stored in the mimic.");
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
		getLogger().log(Level.INFO, "Mimic" + getDescription().getVersion() + ": Settings Saved." );
		getLogger().log(Level.INFO, "Mimic" + getDescription().getVersion() + ": Sucessfully Unloaded.");
	}

	public int ConfigCooldownInSeconds;
	public int ConfigChatterRange;
	public int ConfigClickCooldownInSeconds;
	public boolean ConfigUseSmartWrap;
	public String ConfigTextStyle;
	public List<String> ConfigTypes;

	@Override
	public void onEnable() {

		getLogger().log(Level.INFO, "Mimic" + getDescription().getVersion() + ": Loading Config." );

		int ConfigCooldownInSeconds = getConfig().getInt("cooldown-in-seconds");
		int ConfigClickCooldownInSeconds = getConfig().getInt("click-cooldown-in-seconds", 5);
		final int ConfigChatterRange = getConfig().getInt("chatter-range", 25);
		boolean ConfigUseSmartWrap = getConfig().getBoolean("use-smart-wrap", true);
		String ConfigTextStyle = getConfig().getString("text-style", "Normal");
		List<String> ConfigTypes = getConfig().getStringList("types.list");

		getLogger().log(Level.INFO, "Mimic" + getDescription().getVersion() + ": Loading Character." );
		CitizensAPI.getCharacterManager().registerCharacter(new CharacterFactory(MimicCharacter.class).withName("mimic").withTypes(EntityType.CHICKEN, EntityType.SPIDER));

		getLogger().log(Level.INFO, "Mimic" + getDescription().getVersion() + ": Loading Listener." );
		getServer().getPluginManager().registerEvents(new MimicListener(this), this);

		getLogger().log(Level.INFO, "Mimic" + getDescription().getVersion() + ": Loading Chatter Engine." );
		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {

			public void run() {

				Collection<net.citizensnpcs.api.npc.NPC> MimicNPCs = CitizensAPI.getNPCManager().getNPCs(MimicCharacter.class); // Get a collection of Mimic NPCs to check against
				List<net.citizensnpcs.api.npc.NPC> MimicList = new ArrayList(MimicNPCs); // Turn Collection returned by Citizens into an Arraylist to easily work with
				List<net.citizensnpcs.api.npc.NPC> MimicsInRange = new ArrayList();  // Initialize ArrayList of Mimics within config "chatter-range" of Player

				if (MimicNPCs.isEmpty() == false) {  // Let's do this ONLY IF we have Mimics actually in the world...

					// Start Enhanced Loop to check location distance of Mimic NPCs to the Player

					for (int z=0; z< MimicList.size(); z++){   // Do for each instance of Mimic

						net.citizensnpcs.api.npc.NPC thisMimic = MimicList.get(z);   // Set specific mimic for comparison
						List<Player> PlayersInRangeofMimic = new ArrayList(); // List of players

						int PlayerCount = 0; // Used for counting players, sending messages, etc.

						for(Player player : getServer().getOnlinePlayers()) {
							Location playerLocation = player.getLocation();						

							if (player.getWorld() == thisMimic.getBukkitEntity().getWorld()) {  // Check if they are in the same world.

								if (playerLocation.distance(thisMimic.getBukkitEntity().getLocation()) <= ConfigChatterRange) { PlayersInRangeofMimic.add(PlayerCount, player); } // If Player distance is less than config "chatter-range" of thisMimic

							}

						} // End getting players in range of Mimic .. now stored in PlayersInRangeofMimic

						// Now, which messages to send to the players in range?

						if (!PlayersInRangeofMimic.isEmpty()) {  // Only doing if players are in range of a Mimic

							// Get type of mimic for chatter
							String MimicType = getConfig().getString(thisMimic.getId() + ".type");
		//					getServer().broadcastMessage("" + MimicType);
							List<String> MimicTypeMessages = getConfig().getStringList("types." + MimicType + ".texts");

							Random u = new Random(); //
							int WhatShouldWeDo = u.nextInt(MimicTypeMessages.size());

							Random v = new Random();
							int ClueOrMessage = u.nextInt(4);

							// Stored Message coming through.... Mimic-style
							List<String> StoredMimicMessages = getConfig().getStringList(thisMimic.getId() + ".savedmessages");
							Random t = new Random();             // Which key should we read?
							int whichmessage = t.nextInt(StoredMimicMessages.size()); 

							String theMessage;
							
							if (ClueOrMessage == 0) { // do clue 								
								theMessage = MimicTypeMessages.get(WhatShouldWeDo).toString().replace("<text>", getConfig().getString(thisMimic.getId() + ".clue"));
							}
							else { // do message
								theMessage = MimicTypeMessages.get(WhatShouldWeDo).toString().replace("<text>", StoredMimicMessages.get(whichmessage).toString());
							}

												
							if (theMessage != null) {
								
					//			theMessage = WordWrapper(theMessage);
							
								for (int w=0; w< PlayersInRangeofMimic.size(); w++) { 
								PlayersInRangeofMimic.get(w).sendMessage(theMessage); 
							}}


						} // END Players Empty Check

					} // END PLAYERS IN RANGE

				} // END FOR EACH PARROT

			} // END CHECK OF PARROTS

		}, ConfigCooldownInSeconds * 20, ConfigCooldownInSeconds * 20);

		getLogger().log(Level.INFO, "Mimic" + getDescription().getVersion() + ": Sucessfully Enabled." );
	}
}

