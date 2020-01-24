/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectfour.controller;

import connectfour.model.Player;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;

/**
 * FXML Controller class
 *
 * @author Kris
 */
public class GameController implements Initializable {

    private static final int SIZE = 6;
    private static int[][] gridFields = new int[SIZE][SIZE];
    Player player1 = new Player(1);
    Player player2 = new Player(2);
    Player currentPlayer;

    @FXML
    private GridPane gameGrid;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupGame();
        renderGame();
    }

    private void setupGame() {
        fillInitialGrid();
        currentPlayer = player1;
    }

    private void renderGame() {
        for (final Node node : gameGrid.getChildren()) {
            int row = GridPane.getRowIndex(node);
            int col = GridPane.getColumnIndex(node);

            if (row < 0 || row > 5 || col > 5 || col < 0 || node == null) {
                return;
            }

            Circle c;

            if (node instanceof Circle) {
                c = (Circle) node;
            } else {
                return;
            }

            String style = "";

            c.getStyleClass().clear();

            switch (gridFields[GridPane.getRowIndex(node)][GridPane.getColumnIndex(node)]) {
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

    private void fillInitialGrid() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                gridFields[j][i] = 0;
            }
        }

    }

    @FXML
    private void onNextTurn(MouseEvent e) {
        Node source = e.getPickResult().getIntersectedNode();
        int colIndex = GridPane.getColumnIndex(source);
        int rowIndex = GridPane.getRowIndex(source);

        addNextToGrid(currentPlayer.getPlayerNumber(), rowIndex, colIndex);
    }

    private void changePlayer() {
        if (currentPlayer == player1) {
            currentPlayer = player2;
        } else {
            currentPlayer = player1;
        }
    }

    private void addNextToGrid(int nextPlayer, int rowIndex, int colIndex) {
        //Ako je red napunjen 
        //if () {
        int lastAddedInColumn = -1;
        for (int i = 0; i < 6; i++) {
            if (gridFields[5][colIndex] == 0) {
                lastAddedInColumn = 5;
                break;
            } else if (gridFields[i][colIndex] == 1 || gridFields[i][colIndex] == 2) {
                lastAddedInColumn = i - 1;
                break;
            }
        }
        if (lastAddedInColumn < 0) {
            return;
        }

        gridFields[lastAddedInColumn][colIndex] = nextPlayer;
        changePlayer();
        renderGame();
        //}

    }

}
