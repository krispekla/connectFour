/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectfour.controller;

import connectfour.model.ChatService;
import connectfour.model.GameState;
import connectfour.model.IChatListener;
import connectfour.model.IChatService;
import connectfour.utils.Helper;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javax.xml.transform.TransformerException;

/**
 * FXML Controller class
 *
 * @author Kris
 */
public class GameController implements Initializable {

    public GameState state = new GameState(6, "Player 1", "Player 2");
    public ObjectOutputStream os;
    public ChatService chat;
    public ChatService server;
    public Boolean isServer = false;
    private Socket sock;
    private Registry registry;
    @FXML
    private GridPane gameGrid;
    @FXML
    private Button btnStartNew;
    @FXML
    private Text txtDisplay;
    @FXML
    private Button btnHost;
    @FXML
    private TextArea txtareaInfo;
    @FXML
    private Button btnClient;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnLoad;
    @FXML
    private Button btnDisconnect;
    @FXML
    private Text txtPlayer1;
    @FXML
    private Text txtPlayer2;
    @FXML
    private TextArea txtAreaChat;
    @FXML
    private TextField txtInputChat;
    @FXML
    private Button btnChatSend;
    @FXML
    private Button btnRecord;
    @FXML
    private Button btnReplay;
    @FXML
    private Button btnReflection;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        state = new GameState(6, "Player 1", "Player 2");

        setupGame();
        renderGame();
        gameGrid.setDisable(true);
    }

    private void setupGame() {

        txtDisplay.setText("Next turn: " + state.currentPlayer.getName());

        gameGrid.setDisable(false);
        btnStartNew.setDisable(true);

        if (state.getWinner()) {
            state.setWinner(false);
            state.resetInitialGrid();
            if (state.currentPlayer == state.getPlayer1()) {
                state.setCurrentPlayer(state.getPlayer2());
            } else {
                state.setCurrentPlayer(state.getPlayer1());
            }
            renderGame();
        }
    }

    private void renderGame() {
        txtDisplay.setText("Next turn: " + state.getCurrentPlayer().getName());

        for (final Node node : gameGrid.getChildren()) {
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
    private void onNextTurn(MouseEvent e) throws InterruptedException {
        Node source = e.getPickResult().getIntersectedNode();
        if (!(source instanceof Circle)) {
            return;
        }
        int colIndex = GridPane.getColumnIndex(source);
        int rowIndex = GridPane.getRowIndex(source);

        addNextToGrid(state.getCurrentPlayer().getPlayerNumber(), rowIndex, colIndex);
        if (state.recording) {
            try {
                Helper.saveXMLofGameState(state);
            } catch (IOException | TransformerException ex) {
                Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void changePlayer() {
        if (state.getCurrentPlayer() == state.getPlayer1()) {
            state.setCurrentPlayer(state.getPlayer2());
        } else {
            state.setCurrentPlayer(state.getPlayer1());

        }
        txtDisplay.setText("Next turn: " + state.getCurrentPlayer().getName());
    }

    private void addNextToGrid(int nextPlayer, int rowIndex, int colIndex) throws InterruptedException {
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
            txtareaInfo.appendText("Pobjednik je " + state.currentPlayer.getName());
        }

        changePlayer();
        renderGame();
        if (sock != null) {
            try {

                txtareaInfo.appendText("INFO: salje update statea");
                os.writeObject(state);
                os.flush();

                gameGrid.setDisable(true);
                if (state.getWinner()) {
                    btnStartNew.setDisable(true);
                    txtareaInfo.appendText("Pobjednik je " + state.currentPlayer.getName());
                }

            } catch (IOException ex) {
                txtareaInfo.appendText("Greska: " + ex.toString());
            }
        }
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
        if (sock != null) {
            try {
                os.writeObject(state);
                os.flush();
            } catch (IOException ex) {
                txtareaInfo.appendText("Greska: " + ex.toString());
            }
        }
        gameGrid.setDisable(true);
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

    @FXML
    private void onHostCreateClick(MouseEvent event) throws IOException {
        Host host = new Host();
//        Host host = new Host(state, gameGrid, txtAreaChat, txtareaInfo);
        host.setDaemon(true);
        host.start();
        isServer = true;
        gameGrid.setDisable(false);
        txtPlayer1.setFont(Font.font("Verdana", FontWeight.BOLD, 16));

        txtAreaChat.appendText("Chat pokrenut\n");
    }

    @FXML
    private void onClientConnect(MouseEvent event) {
//        Client client = new Client(state, gameGrid, txtAreaChat, txtareaInfo);
        Client client = new Client();
        client.setDaemon(true);
        client.start();
        txtPlayer2.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        txtAreaChat.appendText("Chat pokrenut\n");

    }

    @FXML
    private void onButtonSaveClick(MouseEvent event) {
        try {
            Helper.saveToFile(state);
        } catch (IOException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Došlo je do pogreške u radu s datotekom!");
            alert.showAndWait();
        }
    }

    @FXML
    private void onButtonLoadClick(MouseEvent event) {
        if (Files.exists(Paths.get(Helper.FILENAME))) {
            try {
                state = Helper.readGameStateFromFile();
                if (sock != null) {
                    os.writeObject(state);
                    os.flush();
                    gameGrid.setDisable(true);
                }
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
        renderGame();
    }

    @FXML
    private void onButtonDisconnectClick(MouseEvent event
    ) {
        try {
            if (os != null && sock != null) {
                os.close();
                sock.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
    }

    @FXML
    private void onButtonChatSendClick(MouseEvent event) {

        try {
            if (registry == null) {
                registry = LocateRegistry.getRegistry();
            }

            if (isServer && !txtInputChat.getText().isEmpty()) {
                registry = LocateRegistry.getRegistry();
                IChatService chat = (IChatService) registry.lookup("client");
                chat.send("Player 1: " + txtInputChat.getText());
                txtAreaChat.appendText("Player 1: " + txtInputChat.getText() + "\n");
            } else if (!txtInputChat.getText().isEmpty()) {
                registry = LocateRegistry.getRegistry();
                IChatService chat = (IChatService) registry.lookup("server");
                chat.send("Player 2: " + txtInputChat.getText());
                txtAreaChat.appendText("Player 2: " + txtInputChat.getText() + "\n");
            }
            txtInputChat.setText("");

        } catch (RemoteException | NotBoundException ex) {
            Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    private void onButtonRecordClick(MouseEvent event) {
        if (!state.recording) {
            Helper.resetXMLGameState();

        }
        state.recording = !state.recording;
        toggleRecordButton();
    }

    @FXML
    private void onButtonReplayClick(MouseEvent event) {
        replayLastGame();
    }

    private void toggleRecordButton() {
        if (state.getRecording()) {
            btnRecord.setText("Stop");
        } else {
            btnRecord.setText("Record");
        }
    }

    private void replayLastGame() {
        Replay rep = new Replay();
        rep.start();

    }

    @FXML
    private void onButtonReflectionClick(MouseEvent event) {
        Class klasa = state.currentPlayer.getClass();
        txtareaInfo.appendText("\nIme klase: " + klasa.getName() + "\n");
    }

    public class Host extends Thread implements IChatListener {

        private ServerSocket serverSocket;
//        private IChatService chat;
//        private GameState state;
//        private GridPane gameGrid;
//        private TextArea txtAreaChat;
//        private TextArea txtareaInfo;
//
//        private Host(GameState state, GridPane gameGrid, TextArea txtAreaChat, TextArea txtareaInfo) {
//            this.state = state;
//            this.gameGrid = gameGrid;
//            this.txtAreaChat = txtAreaChat;
//            this.txtareaInfo = txtareaInfo;
//        }

        public void run() {
            try {
                serverSocket = new ServerSocket(8089);
                sock = serverSocket.accept();
                os = new ObjectOutputStream(sock.getOutputStream());
                txtareaInfo.appendText("Server pokrenut\n");

                ChatService serverChat = new ChatService("Player 1");
                serverChat.addListener(this);
                Registry registry = LocateRegistry.createRegistry(1099);

                IChatService stub = (IChatService) UnicastRemoteObject.exportObject(serverChat, 0);
                registry.rebind("server", stub);

                try {
                    ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
                    GameState temp;
                    while (true) {
                        temp = (GameState) in.readObject();
                        gameGrid.setDisable(false);

                        txtareaInfo.appendText("Dobio objekt sa klijenta\n");
                        state = (GameState) temp;
                        txtareaInfo.appendText("Host Player : \n" + state.currentPlayer.getName());
                        txtareaInfo.appendText("State na hostu updatean\n");
                        renderGame();
                        if (state.getWinner()) {
                            setWinner();
                            txtareaInfo.appendText("Pobjednik je " + state.currentPlayer.getName());
                            txtDisplay.setText("Winner is " + state.getCurrentPlayer().getName());

                        }

                    }
                } catch (IOException | ClassNotFoundException ex) {
                    txtareaInfo.appendText("Greska: " + ex.toString());

                } finally {
                    close(sock, serverSocket);
                }
            } catch (IOException ex) {
                Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);

            }
        }

        private void close(Socket clientSocket, ServerSocket serverSocket) {
            try {
                clientSocket.close();
                serverSocket.close();
            } catch (IOException ex) {
                txtareaInfo.appendText("Server se zatvara: " + ex.toString());
            }
        }

        @Override
        public void onMesssageChange(String message) {

            txtAreaChat.appendText(message + "\n");
        }

    }

    public class Client extends Thread implements IChatListener {

//        private GameState state;
//        private GridPane gameGrid;
//        private TextArea txtAreaChat;
//        private TextArea txtareaInfo;
//
//        private Client(GameState state, GridPane gameGrid, TextArea txtAreaChat, TextArea txtareaInfo) {
//            this.state = state;
//            this.gameGrid = gameGrid;
//            this.txtAreaChat = txtAreaChat;
//            this.txtareaInfo = txtareaInfo;
//        }

        public void run() {
            try {
                sock = new Socket("localhost", 8089);
                os = new ObjectOutputStream(sock.getOutputStream());

                ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
                GameState temp;

                ChatService clientChat = new ChatService("Player 2");
                clientChat.addListener(this);
                Registry registry = LocateRegistry.getRegistry();

                IChatService stub = (IChatService) UnicastRemoteObject.exportObject(clientChat, 0);
                registry.rebind("client", stub);

                while (true) {
                    temp = (GameState) in.readObject();
                    gameGrid.setDisable(false);

                    txtareaInfo.appendText("Dobio objekt sa hosta\n");
                    state = (GameState) temp;
                    txtareaInfo.appendText("Client Player : \n" + state.currentPlayer.getName());
                    txtareaInfo.appendText("State na klijentu updatean\n");
                   
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
                            renderGame();
//                        }
//                    });
                            
                    if (state.getWinner()) {
                        setWinner();
                        txtDisplay.setText("Winner is " + state.getCurrentPlayer().getName());

                    }

                }

            } catch (IOException ex) {
                txtareaInfo.appendText("Greska: " + ex.toString());

            } catch (ClassNotFoundException ex) {
                txtareaInfo.appendText("Greska: " + ex.toString());
            } finally {
                close(sock);
            }

        }

        private void close(Socket clientSocket) {
            try {
                clientSocket.close();
            } catch (IOException ex) {
                txtareaInfo.appendText("Zatvara se: " + ex.toString());
            }
        }

        @Override
        public void onMesssageChange(String message) {
            txtAreaChat.appendText(message + "\n");
        }

    }

    public class Replay extends Thread {

        public void run() {
            List<GameState> stateList = Helper.readXMLGameStates();
            for (int i = 0; i < stateList.size(); i++) {
                try {
                    state = stateList.get(i);
                    renderGame();

                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }
}
