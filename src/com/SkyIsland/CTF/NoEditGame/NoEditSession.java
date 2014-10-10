package com.SkyIsland.CTF.NoEditGame;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.SkyIsland.CTF.CTFPlugin;
import com.SkyIsland.CTF.CTFSession;
import com.SkyIsland.CTF.Team.CTFTeam;
import com.SkyIsland.CTF.Team.TeamPlayer;

public class NoEditSession implements CTFSession, Listener {

	private List<CTFTeam> Teams;
	private boolean running;
	private String name;
	private Scoreboard scoreboard;
	
	/**
	 * Default constructor for a No Edit Game Session
	 */
	public NoEditSession(String name) {
		this.name = name;
		this.Teams = new LinkedList<CTFTeam>();
		Bukkit.getPluginManager().registerEvents(this, CTFPlugin.plugin);
		this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective obj = scoreboard.registerNewObjective("Points", "dummy");
		obj.setDisplayName("Points");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
	}
	
	/**
	 * This constructor allows for specification of Teams
	 * @param Teams The Teams to be added to the session
	 */
	public NoEditSession(String name, List<CTFTeam> Teams) {
		this.name = name;
		this.Teams = Teams;
		this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective obj = scoreboard.registerNewObjective(name + "_score", "dummy");
		obj.setDisplayName("Points");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		for (CTFTeam team : Teams) {
			Team scoreTeam = scoreboard.registerNewTeam(team.getName());
			scoreTeam.setPrefix(ChatColor.BOLD + name + ChatColor.RESET);
			scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(team.getName()).setScore(0);
			for (TeamPlayer tp : team.getTeamPlayers()) {
				scoreTeam.addPlayer(tp.getPlayer());
				tp.getPlayer().setScoreboard(scoreboard);
			}
		}
	}
	
	/**
	 * This returns a list of Teams within the Session
	 */
	@Override
	public List<CTFTeam> getTeams() {
		return this.Teams;
	}
	
	/**
	 * This method generates a new empty Team within the Game Session with the specified name
	 * @param name The Name to be attributed to the team
	 */
	@Override
	public CTFTeam createTeam(String name) {
		Score score = scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(name);
		CTFTeam team = new NoEditTeam(name, score);
		Teams.add(team);
		scoreboard.registerNewTeam(name).setPrefix(ChatColor.DARK_PURPLE + "| " + name + ChatColor.RESET);
		scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(team.getName()).setScore(0);
		return team;
	}

	/**
	 * This method generates a new Team with the players and name specified
	 * @param name The Team Name
	 * @param players The list of players to be associated with the team
	 */
	@Override
	public CTFTeam createTeam(String name, List<TeamPlayer> players) {
		NoEditTeam team;
		Score score;
		score = scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(name);
		team = new NoEditTeam(name, score);
		this.Teams.add(team);
		scoreboard.registerNewTeam(name).setPrefix(ChatColor.BOLD + name + ChatColor.RESET);
		scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(team.getName()).setScore(0);
		team.setTeamPlayers(players);
		//we gotta give each player the scoreboard
		Team scoreTeam = scoreboard.getTeam(team.getName());
		if (players != null && !players.isEmpty())
		for (TeamPlayer tp : players) {
			scoreTeam.addPlayer(tp.getPlayer());
			tp.getPlayer().setScoreboard(scoreboard);
		}
		
		
		return team;
	}

	@Override
	public void removeTeam(CTFTeam team) {
		if (Teams.contains(team)) {
			Teams.remove(team);
			scoreboard.getTeam(team.getName()).unregister();
		}
	}

	@Override
	public void addPlayer(CTFTeam team, TeamPlayer player) {
		if (Teams.contains(team)) {//only add if this session has that team???
			team.addPlayer(player);
			scoreboard.getTeam(team.getName()).addPlayer(player.getPlayer());
			scoreboard.
			getObjective(DisplaySlot.SIDEBAR)
			.getScore(player.getPlayer().getDisplayName())
			.setScore(0);
			player.getPlayer().setScoreboard(scoreboard);
		}
	}

	@Override
	public void removePlayer(CTFTeam team, TeamPlayer player) {
		if (Teams.contains(team)) {
			team.removePlayer(player);
			scoreboard.getTeam(team.getName()).removePlayer(player.getPlayer());
			player.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		}
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}

	@Override
	public void start() {
		this.running = true;
		for (CTFTeam t : this.getTeams()) {
			t.resetFlag();
			t.resetScore();
			t.getGoal().setAccepting(true);
			for (TeamPlayer tp : t.getTeamPlayers()) {
				tp.spawn();
			}
		}
		
		
		
	}

	@Override
	public void stop() {
		running = false;
		for (CTFTeam t : this.getTeams()) {
			t.getGoal().setAccepting(false);
			for (TeamPlayer tp : t.getTeamPlayers()) {
				tp.moveLeave();
				tp.getPlayer().sendMessage("Game stopped!");
				tp.getPlayer().sendMessage("Your team scored " + t.getScore() + "points! You scored " + tp.getPoints() + " of those!");
				tp.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
			}
		}		
		
		scoreboard = null;

	}
	
	
	@EventHandler
	public void punchWool(PlayerInteractEvent event) {
		if (event.getAction() == Action.LEFT_CLICK_BLOCK)
		if (event.getClickedBlock().getType() == Material.WOOL) {
			event.getClickedBlock().breakNaturally();
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void playerMove(PlayerMoveEvent event) {
		NoEditTeam team = null;
		for (CTFTeam t : Teams) {
			if (t.inTeam(         CTFPlugin.getTeamPlayer(event.getPlayer())       )) {
				team = (NoEditTeam) t;
				break;
			}
		}
		
		//if (team.getGoal() != null && (team != null && team.getGoal().isInGoal(event.getTo()))) {
		if (team != null && team.getGoal() != null && team.getGoal().isInGoal(event.getTo())) {
			//goal
		}
		
		
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public boolean hasTeam(String name) {
		for (CTFTeam t : Teams) {
			if (t.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public CTFTeam getTeam(String name) {
		for (CTFTeam t : Teams) {
			if (t.getName().equalsIgnoreCase(name)) {
				return t;
			}
		}
		return null;		
	}
	
	public Scoreboard getScoreboard() {
		return this.scoreboard;
	}

}
