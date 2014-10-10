package com.SkyIsland.CTF.Team;

import org.bukkit.entity.Player;

public class TeamPlayer {
	
	private Player player;
	
	private boolean hasFlag;
	
	private CTFTeam team;
	
	
	
	
	public TeamPlayer(Player player) {
		this.player = player;
		this.hasFlag = false;
		team = null;
	}
	
	
	
	public Player getPlayer() {
		return this.player;
	}
	
	public boolean hasFlag() {
		return this.hasFlag;
	}
	
	public CTFTeam getTeam() {
		return this.team;
	}
	
	public void setTeam(CTFTeam team) {
		this.team = team;
	}
	
	
}
