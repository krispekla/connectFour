/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectfour.model;

import java.io.Serializable;

/**
 *
 * @author pekla
 */
public class GameState implements Serializable {

    private int _size;
    public int[][] _gridFields;
    private Player _player1;
    private Player _player2;
    public Player currentPlayer;
    public Boolean winner;
    public Boolean recording;

    public GameState(int size, String player1, String player2) {
        this._size = size;
        _gridFields = new int[_size][_size];
        this._player1 = new Player(1, player1);
        this._player2 = new Player(2, player2);
        this.currentPlayer = this._player1;
        this.winner = false;
        this.recording = false;
        resetInitialGrid();
    }

    public int[][] getGridFields() {
        return _gridFields;
    }

    public int getGridFieldValue(int row, int col) {
        return _gridFields[row][col];
    }

    public void setGridFieldValue(int row, int col, int value) {
        this._gridFields[row][col] = value;
    }

    public Player getPlayer1() {
        return _player1;
    }

    public Player getPlayer2() {
        return _player2;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public int getSize() {
        return _size;
    }

    public Boolean getWinner() {
        return winner;
    }

    public void setWinner(Boolean winner) {
        this.winner = winner;
    }

    public Boolean getRecording() {
        return recording;
    }

    public void setRecording(Boolean recording) {
        this.recording = recording;
    }

    public void resetInitialGrid() {
        for (int i = 0; i < _size; i++) {
            for (int j = 0; j < _size; j++) {
                setGridFieldValue(j, i, 0);
            }
        }

    }

}
