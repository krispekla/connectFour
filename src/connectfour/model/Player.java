/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectfour.model;

import javafx.scene.paint.Color;

/**
 *
 * @author Kris
 */
public class Player {
    
    private int playerNumber;

    public int getPlayerNumber() {
        return playerNumber;
    }

    public Player(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    

    private String name;
    private Color color;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

}
