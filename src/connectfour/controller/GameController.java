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
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
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

    private static final int SIZE = 6;
    private static int[][] gridFields = new int[SIZE][SIZE];
    Player player1 = new Player(1, "Player 1");
    Player player2 = new Player(2, "Player 2");
    Player currentPlayer;
    Boolean winner = false;

    @FXML
    private GridPane gameGrid;
    @FXML
    private Button btnStartNew;
    @FXML
    private Text txtDisplay;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupGame();
        renderGame();
    }

    private void setupGame() {
        fillInitialGrid();
        currentPlayer = player1;
        gameGrid.setDisable(false);
        btnStartNew.setDisable(true);
        txtDisplay.setText("Next turn: " + currentPlayer.getName());

        if (winner) {
            winner = false;
            renderGame();
        }
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

            switch (gridFields[row][col]) {
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
        txtDisplay.setText("Next turn: " + currentPlayer.getName());
    }

    private void addNextToGrid(int nextPlayer, int rowIndex, int colIndex) {
        int lastAddedInColumn = -1;
        for (int i = 0; i < SIZE; i++) {
            if (gridFields[SIZE - 1][colIndex] == 0) {
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

        calculateGame(lastAddedInColumn, colIndex, nextPlayer);
        if (winner) {
            setWinner();
        }
        changePlayer();
        renderGame();
    }

    private void calculateGame(int row, int col, int playerCheck) {
        //Check coll
        int tempInRow = 0;
        for (int j = 0; j < SIZE; j++) {
            if (gridFields[j][col] == playerCheck) {
                tempInRow++;

            } else {
                tempInRow = 0;
            }

            if (tempInRow == 4) {
                winner = true;
                break;
            }
        }

        //Check row
        tempInRow = 0;
        for (int j = 0; j < SIZE; j++) {
            if (gridFields[row][j] == playerCheck) {
                tempInRow++;

            } else {
                tempInRow = 0;
            }

            if (tempInRow == 4) {
                winner = true;
                break;
            }
        }
//            TODO
//        //Check diagonal hor
//        tempInRow = 0;
//        for (int j = 0; j < SIZE; j++) {
//            if (gridFields[j][col] == playerCheck) {
//                tempInRow++;
//
//            } else {
//                tempInRow = 0;
//            }
//
//            if (tempInRow == 4) {
//                winner = true;
//                break;
//            }
//        }
//
//        //Check diagonal ver
//        tempInRow = 0;
//        for (int j = 0; j < SIZE; j++) {
//            if (gridFields[j][col] == playerCheck) {
//                tempInRow++;
//
//            } else {
//                tempInRow = 0;
//            }
//
//            if (tempInRow == 4) {
//                winner = true;
//                break;
//            }
//        }
    }

    private void setWinner() {
        btnStartNew.setDisable(false);
        txtDisplay.setText("Winner is " + currentPlayer.getName());
        gameGrid.setDisable(true);
    }

    @FXML
    private void onStartNewGame(MouseEvent event) {
        setupGame();
    }

}
