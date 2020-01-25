/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectfour.model;

import java.io.Serializable;
import javafx.scene.paint.Color;

/**
 *
 * @author Kris
 */
public class Player implements Serializable{
    
    private  int playerNumber;

    public int getPlayerNumber() {
        return playerNumber;
    }

    public Player(int playerNumber, String name) {
        this.playerNumber = playerNumber;
        this.name = name;
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
