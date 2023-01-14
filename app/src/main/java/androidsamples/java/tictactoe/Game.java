package androidsamples.java.tictactoe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Main Game class. Holds the host and tic tac toe game values
 * and holds whether the game is open or if its the turn of player 1 or 2.
 * Has appropriate getters and setters.
 */
public class Game {
    private String host;
    private List<String>gameValues;
    private boolean open;
    private int turn;
    private String gameID;
    private boolean complete;
    private String lostID;

    public Game(String host, String id) {
        this.host = host;
        open = true;
        gameValues = Arrays.asList("", "", "", "", "", "", "", "", "");
        this.gameID = id;
        turn = 1;
        complete = false;
        lostID = "";
    }

    public Game(){}

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public List<String> getGameValues() {
        return gameValues;
    }

    public void setGameValues(List<String> gameValues) {
        this.gameValues = gameValues;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public String getGameID() {
        return gameID;
    }

    public void setGameID(String gameID) {
        this.gameID = gameID;
    }

    public boolean isComplete() {
        return complete;
    }
    public void setIsComplete(boolean complete) {
        this.complete = complete;
    }

    public String getLostID() {
        return lostID;
    }

    public void setLostID(String lostID) {
        this.lostID = lostID;
    }
}
