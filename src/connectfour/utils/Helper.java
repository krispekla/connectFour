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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author pekla
 */
public class Helper {

    public static final String FILENAME = "connectState";
    public static final String XMLDOCUMENT = "xmlState.xml";

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

    public static void createAndSaveXMLofGameState(GameState gs) throws IOException, TransformerException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();
            if (!Files.exists(Paths.get(Helper.XMLDOCUMENT))) {
                File file = new File(Helper.XMLDOCUMENT);
                file.createNewFile();
            }

            Element gameStates = doc.createElement("gameStates");
            doc.appendChild(gameStates);

            List<GameState> listStates = readXMLGameStates();

            listStates.add(gs);
            for (GameState st : listStates) {
                Element stateElement = createNewStateElement(doc, st);
                gameStates.appendChild(stateElement);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(XMLDOCUMENT));

            transformer.transform(source, result);
            transformer.transform(source, new StreamResult(System.out));

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private static Element createNewStateElement(Document doc, GameState st) {
        Element gameState = doc.createElement("state");

        Element winner = doc.createElement("winner");
        winner.appendChild(doc.createTextNode(st.getWinner() ? "true" : "false"));
        gameState.appendChild(winner);

        Element recording = doc.createElement("recording");
        recording.appendChild(doc.createTextNode(st.getRecording() ? "true" : "false"));
        gameState.appendChild(recording);

        Element player1 = doc.createElement("player1");

        Element py1Name = doc.createElement("name");
        py1Name.appendChild(doc.createTextNode(st.getPlayer1().getName()));
        player1.appendChild(py1Name);

        Element py1Number = doc.createElement("number");
        py1Number.appendChild(doc.createTextNode(Integer.toString(st.getPlayer1().getPlayerNumber())));
        player1.appendChild(py1Number);

        gameState.appendChild(player1);

        Element player2 = doc.createElement("player2");

        Element py2Name = doc.createElement("name");
        py2Name.appendChild(doc.createTextNode(st.getPlayer2().getName()));
        player2.appendChild(py2Name);

        Element py2Number = doc.createElement("number");
        py2Number.appendChild(doc.createTextNode(Integer.toString(st.getPlayer2().getPlayerNumber())));
        player2.appendChild(py2Number);

        gameState.appendChild(player2);

        Element gridFields = doc.createElement("grid");
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                Element temp = doc.createElement("node");
                temp.setAttribute("row", Integer.toString(i));
                temp.setAttribute("col", Integer.toString(j));
                temp.appendChild(doc.createTextNode(Integer.toString(st.getGridFieldValue(j, i))));
                gridFields.appendChild(temp);
            }
        }
        gameState.appendChild(gridFields);

        Element currentPl = doc.createElement("currentPlayer");
        currentPl.appendChild(doc.createTextNode(Integer.toString(st.getCurrentPlayer().getPlayerNumber())));
        gameState.appendChild(currentPl);

        return gameState;
    }

    public static List<GameState> readXMLGameStates() {
        List<GameState> games = new ArrayList<>();
        try {

            File file = new File(XMLDOCUMENT);

            if (file.length() == 0) {
                games.add(new GameState(6, "Player 1", "Player 2"));
                return games;
            }

            File fXmlFile = new File(XMLDOCUMENT);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            NodeList nList = doc.getElementsByTagName("state");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                GameState tempState = new GameState(6, "Player 1", "Player 2");

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    tempState.setRecording(eElement.getElementsByTagName("recording").item(0).getTextContent() == "true");
                    tempState.setWinner(eElement.getElementsByTagName("winner").item(0).getTextContent() == "true");
                    tempState.setCurrentPlayer(eElement.getElementsByTagName("currentPlayer").item(0).getTextContent() == "1" ? tempState.getPlayer1() : tempState.getPlayer2());

                    NodeList gridList = eElement.getElementsByTagName("node");

                    for (int tt = 0; tt < gridList.getLength(); tt++) {
                        Node elNode = gridList.item(tt);

                        if (elNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element el = (Element) elNode;
                            tempState.setGridFieldValue(Integer.parseInt(el.getAttribute("row")), Integer.parseInt(el.getAttribute("col")), Integer.parseInt(el.getTextContent()));
                        }
                    }

                }
                games.add(tempState);
            }

        } catch (SAXException | IOException | ParserConfigurationException ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return games;
    }
}
