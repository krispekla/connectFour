/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectfour.utils;

import connectfour.model.GameState;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author pekla
 */
public class Helper {

    public static final String FILENAME = "connectState";

    public static GameState readGameStateFromFile() throws IOException {
        GameState game;

        File file = new File(FILENAME);

        if (file.length() == 0) {
            return new GameState(6, "Player 1", "Player 2");
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(file))) {
            game = (GameState) ois.readObject();
        } catch (ClassNotFoundException ex) {
            throw new IOException("Ne postoji spremljeni game", ex);
        }

        return game;
    }

    public static void saveToFile(GameState game) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(FILENAME, false))) {
            oos.writeObject(game);
        }
    }

    public static void createEmptyFile() throws IOException {
        File file = new File(Helper.FILENAME);
        file.createNewFile();
    }
}
