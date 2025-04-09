package edu.uob;

import com.alexmerz.graphviz.ParseException;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.StringTokenizer;

public final class GameServer {

    private static final char END_OF_TRANSMISSION = 4;


    private final LoadedEntity loadedEntity;
    private final LoadedAction loadedAction;
    private PlayersManagement playersInGame;

    public static void main(String[] args) throws IOException {
        try {
            StringBuilder entitiesPathBuilder = new StringBuilder();
            entitiesPathBuilder.append("config")
                    .append(File.separator)
                    .append("extended-entities.dot");
            File entitiesFile = Paths.get(entitiesPathBuilder.toString()).toAbsolutePath().toFile();

            StringBuilder actionsPathBuilder = new StringBuilder();
            actionsPathBuilder.append("config")
                    .append(File.separator)
                    .append("extended-actions.xml");
            File actionsFile = Paths.get(actionsPathBuilder.toString()).toAbsolutePath().toFile();

            GameServer server = new GameServer(entitiesFile, actionsFile);
            server.blockingListenOn(8888);
        } catch (RuntimeException | IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

    }

    /**
    *
    * @param entitiesFile The game configuration file containing all game entities to use in your game
    * @param actionsFile The game configuration file containing all game actions to use in your game
    */
    public GameServer(File entitiesFile, File actionsFile) {
        // TODO implement your server logic here
        // loading entities
        this.loadedEntity = new LoadedEntity();
        this.loadedAction = new LoadedAction();
        try {
            this.loadedEntity.loadEntities(entitiesFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to find the entities file");
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Error loading entities file");
        }

        // loading actions
        try {
            this.loadedAction.loadActions(actionsFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to find the actions file");
        } catch (ParserConfigurationException | IOException | SAXException | DOMException e) {
            throw new RuntimeException("Error loading actions file");
        }

        // initialise player hashmap
        this.playersInGame = new PlayersManagement();
    }

    /**
    * This method handles all incoming game commands and carries out the corresponding actions.</p>
    *
    * @param command The incoming command to be processed
    */
    public String handleCommand(String command) {
        // TODO implement your server logic here
        command = command.trim();
        int firstColonIndex = command.indexOf(':');

        String username = command.substring(0, firstColonIndex).trim();
        if(!this.ifIsValidUsername(username)) return "[ERROR]: Invalid username\n";

        if(!this.playersInGame.ifHasPlayer(username)) {
            Location startPoint = this.loadedEntity.getStartLocation();
            Player newPlayer = new Player(username, "A new player", startPoint);
            this.playersInGame.addPlayer(username, newPlayer);
        }

        Player currentPlayer = this.playersInGame.getPlayer(username);
        int actionCommandStartIndex = firstColonIndex + 1;
        String actionCommand = command.substring(actionCommandStartIndex).replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase().trim();

        StringTokenizer stringTokenizer = new StringTokenizer(actionCommand);
        while(stringTokenizer.hasMoreTokens()) {
            String word = stringTokenizer.nextToken();
            if(word.equals("inventory") || word.equals("inv") || word.equals("get") || word.equals("drop")
            || word.equals("look") || word.equals("goto") || word.equals("health")) {
                ParsedBasicCommand parsedBasicCommand = new ParsedBasicCommand(this.playersInGame, this.loadedEntity);
                try {
                    return parsedBasicCommand.parsingBasicCommand(actionCommand, currentPlayer);
                } catch (IllegalArgumentException e) {
                    return e.getMessage();
                }
            }
        }
        // if it's not a built-in command, then try custom command
        try {
            ParsedCustomCommand parsedCustomCommand = new ParsedCustomCommand(this.loadedEntity, this.loadedAction);
            return parsedCustomCommand.parsingCustomCommand(actionCommand, currentPlayer);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    public boolean ifIsValidUsername(String username) {
        for(int i = 0; i < username.length(); i++) {
            char c = username.charAt(i);
            boolean isValidChar = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <='z')
                    || (c == '\'') || (c == '-') || (c == ' ');
            if(!isValidChar) return false;
        }
        return true;
    }

    /**
    * Starts a *blocking* socket server listening for new connections.
    *
    * @param portNumber The port to listen on.
    * @throws IOException If any IO related operation fails.
    */
    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Server listening on port ");
            stringBuilder.append(portNumber);
            System.out.println(stringBuilder.toString());
            while (!Thread.interrupted()) {
                try {
                    this.blockingHandleConnection(s);
                } catch (IOException e) {
                    System.out.println("Connection closed");
                }
            }
        }
    }

    /**
    * Handles an incoming connection from the socket server.
    *
    * @param serverSocket The client socket to read/write from.
    * @throws IOException If any IO related operation fails.
    */
    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            System.out.println("Connection established");
            String incomingCommand = reader.readLine();
            if(incomingCommand != null) {
                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append("Received message from ");
                messageBuilder.append(incomingCommand);
                System.out.println(messageBuilder.toString());
                String result = this.handleCommand(incomingCommand);
                writer.write(result);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("\n");
                stringBuilder.append(END_OF_TRANSMISSION);
                stringBuilder.append("\n");
                writer.write(stringBuilder.toString());
                writer.flush();
            }
        }
    }
}
