package edu.uob;

import java.util.HashMap;

public class PlayersManagement {
    HashMap<String, Player> players;

    public PlayersManagement() {
        this.players = new HashMap<String, Player>();
    }

    public void addPlayer(String username, Player player) {
        this.players.put(username, player);
    }

    public boolean ifHasPlayer(String username) {
        if(this.players.containsKey(username)) {
            return true;
        }
        return false;
    }

    public Player getPlayer(String username) {
        return this.players.get(username);
    }

    public HashMap<String, Player> getPlayersMap() {
        return this.players;
    }
}
