/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectfour.controller;

import connectfour.model.ChatService;
import connectfour.model.GameState;
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
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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

/**
 * FXML Controller class
 *
 * @author Kris
 */
public class GameController implements Initializable {

    GameState state = new GameState(6, "Player 1", "Player 2");
    ObjectOutputStream os;
    ChatService chat;
    ChatService server;

    private Socket sock;
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
        try {
            Host host = new Host();
            host.start();
            gameGrid.setDisable(false);
            txtPlayer1.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
            ChatServer ch = new ChatServer();
            ch.start();
            txtAreaChat.appendText("Chat pokrenut\n");
        } catch (NotBoundException ex) {
            Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void onClientConnect(MouseEvent event) {
        try {
            Client client = new Client();
            client.start();
            txtPlayer2.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
            ChatClient cl = new ChatClient();
            cl.start();
            txtAreaChat.appendText("Chat pokrenut\n");
        } catch (NotBoundException ex) {
            Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
        }

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

        if (txtInputChat.getText().length() > 0 && chat != null) {
            try {
                chat.setName(txtInputChat.getText());
                Registry registry = LocateRegistry.getRegistry();

//                IChatService stub = (IChatService) UnicastRemoteObject.exportObject(chat, 0);
                registry.rebind("client", chat);
                txtAreaChat.appendText("Player 2:" + txtInputChat.getText() + "\n");

            } catch (RemoteException ex) {
                Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (txtInputChat.getText().length() > 0) {
            try {
                chat = new ChatService(txtInputChat.getText());
                Registry registry = LocateRegistry.getRegistry(1099);

                IChatService stub = (IChatService) UnicastRemoteObject.exportObject(chat, 0);
                registry.rebind("server", stub);
                txtAreaChat.appendText("Player 1:" + txtInputChat.getText() + "\n");

            } catch (RemoteException ex) {
                Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        txtInputChat.setText("");
    }

    public class Host extends Thread {

        public ServerSocket serverSocket;

        private Host() throws IOException {
            serverSocket = new ServerSocket(8089);
            sock = serverSocket.accept();
            os = new ObjectOutputStream(sock.getOutputStream());
            txtareaInfo.appendText("Server pokrenut\n");
        }

        public void run() {
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
            } catch (IOException ex) {
                txtareaInfo.appendText("Greska: " + ex.toString());

            } catch (ClassNotFoundException ex) {
                txtareaInfo.appendText("Greska: " + ex.toString());
            } finally {
                close(sock, serverSocket);
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

    }

    public class Client extends Thread {

        public void run() {
            try {
                sock = new Socket("localhost", 8089);
                os = new ObjectOutputStream(sock.getOutputStream());

                ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
                GameState temp;

                while (true) {
                    temp = (GameState) in.readObject();
                    gameGrid.setDisable(false);

                    txtareaInfo.appendText("Dobio objekt sa hosta\n");
                    state = (GameState) temp;
                    txtareaInfo.appendText("Client Player : \n" + state.currentPlayer.getName());
                    txtareaInfo.appendText("State na klijentu updatean\n");
                    renderGame();
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

    }

    public class ChatServer extends Thread {

        public ChatServer() throws NotBoundException {

        }

        public void run() {
            try {
                server = new ChatService("");

                Registry registry = LocateRegistry.createRegistry(1099);

                IChatService stub = (IChatService) UnicastRemoteObject.exportObject(server, 0);
                registry.rebind("server", stub);

                System.out.println("server ready, waiting for client...");

                while (true) {
                    IChatService client;
                    try {
                        client = (IChatService) registry.lookup("client");
                        if (client != null && client.getName().length() > 0) {
                            txtAreaChat.appendText("Player 2: " + client.getName() + "\n");
                            cleanServerRegistry();
                        }
                    } catch (NotBoundException ex) {
                        System.out.println("still no client...");
                    } catch (AccessException ex) {
                        Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Thread.sleep(5000);
                }
            } catch (RemoteException | InterruptedException ex) {
                Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        private void cleanServerRegistry() {
            try {
                chat = new ChatService("");
                Registry registry = LocateRegistry.getRegistry();
                IChatService stub = (IChatService) UnicastRemoteObject.exportObject(chat, 0);

                registry.rebind("client", stub);
            } catch (RemoteException ex) {
                Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public class ChatClient extends Thread {

        public ChatClient() throws NotBoundException {

        }

        public void run() {
            try {
                chat = new ChatService("");

                Registry registry = LocateRegistry.getRegistry();

                IChatService stub = (IChatService) UnicastRemoteObject.exportObject(chat, 0);
                registry.rebind("client", stub);

                System.out.println("client ready, searching for server...");

                while (true) {
                    IChatService server;
                    try {
                        server = (IChatService) registry.lookup("server");
                        if (server != null && server.getName().length() > 0) {
                            txtAreaChat.appendText("Player 1: " + server.getName() + "\n");
                            cleanClientRegistry();
                        }
                    } catch (NotBoundException ex) {
                        System.out.println("still no server...");
                    } catch (AccessException ex) {
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (RemoteException ex) {
                Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        private void close() {

        }

        private void cleanClientRegistry() {
            try {
                chat = new ChatService("");
                Registry registry = LocateRegistry.getRegistry(1099);

                IChatService stub = (IChatService) UnicastRemoteObject.exportObject(chat, 0);
                registry.rebind("server", stub);
            } catch (RemoteException ex) {
                Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
