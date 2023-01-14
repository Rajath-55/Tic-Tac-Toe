package androidsamples.java.tictactoe;


import java.util.ArrayList;


/**
 * The main Player class for storing user data in the Firebase database.
 */
public class Player {
    private String email;
    private String uid;
    private int numberOfLosses, numberOfWins;

    /**
     * Default constructor needed for initialising empty objects if needed to be added
     * to database.
     */
    public Player(){}

    /**
     * Parameterized constructor.
     * @param email
     * @param uid
     */
    public Player(String email, String uid) {
        this.email = email;
        this.uid = uid;
    }

    /**
     * Returns the email
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email
     * @param email The mail to be set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the UID
     * @return UID
     */
    public String getUid() {
        return uid;
    }

    /**
     * Set the UID
     * @param uid UID set value
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Returns the number of losses in tic tac toe games for the player
     * @return numberOfLosses
     */
    public int getNumberOfLosses() {
        return numberOfLosses;
    }

    /**
     * Sets the numberOfLosses parameter
     * @param numberOfLosses Param for numberOfLosses
     */
    public void setNumberOfLosses(int numberOfLosses) {
        this.numberOfLosses = numberOfLosses;
    }

    /**
     * Returns the number of wins in tic tac toe games.
     * @return numberOfWins
     */
    public int getNumberOfWins() {
        return numberOfWins;
    }

    /**
     * Sets the number of wins
     * @param numberOfWins New wins parameter.
     */
    public void setNumberOfWins(int numberOfWins) {
        this.numberOfWins = numberOfWins;
    }


}
