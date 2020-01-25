/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectfour.controller;

import connectfour.model.GameState;
import connectfour.model.Player;
import connectfour.utils.Helper;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author Kris
 */
public class GameController implements Initializable {

    GameState state = new GameState(6, "Player 1", "Player 2");

    @FXML
    private GridPane gameGrid;
    @FXML
    private Button btnStartNew;
    @FXML
    private Text txtDisplay;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        firstInit();
        setupGame();
        renderGame();
    }

    private void firstInit() {
        if (Files.exists(Paths.get(Helper.FILENAME))) {
            try {
                state = Helper.readGameStateFromFile();
            } catch (IOException ex) {
                Logger.getLogger(GameController.class.getName()).log(
                        Level.SEVERE, null, ex);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Ne može se pročitati sadržaj datoteke!");
                alert.showAndWait();
            }
        } else {
            try {
                Helper.createEmptyFile();
                state = new GameState(6, "Player 1", "Player 2");
            } catch (IOException ex) {
                Logger.getLogger(GameController.class.getName()).log(
                        Level.SEVERE, null, ex);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Ne može se kreirati prazna datoteka!");
                alert.showAndWait();
            }
        }
    }

    private void setupGame() {

        txtDisplay.setText("Next turn: " + state.currentPlayer.getName());

        gameGrid.setDisable(false);
        btnStartNew.setDisable(true);

        if (state.getWinner()) {
            state.setWinner(false);
            state.resetInitialGrid();
            saveCurrentState();
            renderGame();
        }
    }

    private void renderGame() {
        for (final Node node : gameGrid.getChildren()) {
            System.out.println(node);

                        Circle c;
            if (node instanceof Circle) {
                c = (Circle) node;
            } else {
                return;
            }
            int row = GridPane.getRowIndex(node);
            int col = GridPane.getColumnIndex(node);

            if (row < 0 || row > 5 || col > 5 || col < 0 || node == null) {
                return;
            }



            String style = "";

            c.getStyleClass().clear();

            switch (state.getGridFieldValue(row, col)) {
                case 0:
                    style = "neutral";
                    c.getStyleClass().add(style);
                    break;
                case 1:
                    style = "orange";
                    c.getStyleClass().add(style);
                    break;
                case 2:
                    style = "red";
                    c.getStyleClass().add(style);
                    break;
            }

        }
    }

    @FXML
    private void onNextTurn(MouseEvent e) {
        Node source = e.getPickResult().getIntersectedNode();
        int colIndex = GridPane.getColumnIndex(source);
        int rowIndex = GridPane.getRowIndex(source);

        addNextToGrid(state.getCurrentPlayer().getPlayerNumber(), rowIndex, colIndex);
    }

    private void changePlayer() {
        if (state.getCurrentPlayer() == state.getPlayer1()) {
            state.setCurrentPlayer(state.getPlayer2());
        } else {
            state.setCurrentPlayer(state.getPlayer1());

        }
        txtDisplay.setText("Next turn: " + state.getCurrentPlayer().getName());
    }

    private void addNextToGrid(int nextPlayer, int rowIndex, int colIndex) {
        int lastAddedInColumn = -1;
        for (int i = 0; i < state.getSize(); i++) {
            if (state.getGridFieldValue(state.getSize() - 1, colIndex) == 0) {
                lastAddedInColumn = 5;
                break;
            } else if (state.getGridFieldValue(i, colIndex) == 1 || state.getGridFieldValue(i, colIndex) == 2) {
                lastAddedInColumn = i - 1;
                break;
            }
        }
        if (lastAddedInColumn < 0) {
            return;
        }

        state.setGridFieldValue(lastAddedInColumn, colIndex, nextPlayer);

        calculateGame(lastAddedInColumn, colIndex, nextPlayer);
        if (state.getWinner()) {
            setWinner();
        }

        changePlayer();
        saveCurrentState();
        renderGame();
    }

    private void calculateGame(int row, int col, int playerCheck) {
        //Check coll
        int tempInRow = 0;
        for (int j = 0; j < state.getSize(); j++) {
            if (state.getGridFieldValue(j, col) == playerCheck) {
                tempInRow++;

            } else {
                tempInRow = 0;
            }

            if (tempInRow == 4) {
                state.setWinner(true);
                break;
            }
        }

        //Check row
        tempInRow = 0;
        for (int j = 0; j < state.getSize(); j++) {
            if (state.getGridFieldValue(row, j) == playerCheck) {
                tempInRow++;

            } else {
                tempInRow = 0;
            }

            if (tempInRow == 4) {
                state.setWinner(true);
                break;
            }
        }
//         TODO
//        //Check diagonal hor and diagonal vert

    }

    private void setWinner() {
        btnStartNew.setDisable(false);
        txtDisplay.setText("Winner is " + state.getCurrentPlayer().getName());
        gameGrid.setDisable(true);
    }

    @FXML
    private void onStartNewGame(MouseEvent event) {
        setupGame();
    }

    private void saveCurrentState() {
        try {
            Helper.saveToFile(state);
        } catch (IOException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Došlo je do pogreške u radu s datotekom!");
            alert.showAndWait();
        }
    }

}
