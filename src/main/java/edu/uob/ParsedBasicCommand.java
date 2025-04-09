package edu.uob;

import java.util.*;

public class ParsedBasicCommand {
    private PlayersManagement playerInGame;
    private LoadedEntity loadedEntity;

    public ParsedBasicCommand(PlayersManagement playerInGame,
                              LoadedEntity loadedEntity) {
        this.playerInGame = playerInGame;
        this.loadedEntity = loadedEntity;
    }

    public String parsingBasicCommand(String command, Player currentPlayer) throws IllegalArgumentException {
        StringTokenizer stringTokenizer = new StringTokenizer(command);
        while(stringTokenizer.hasMoreTokens()) {
            String word = stringTokenizer.nextToken();
            if(word.equals("inventory") || word.equals("inv")) {
                return this.invCmd(command, currentPlayer);
            } else if (word.equals("look")) {
                return this.lookCmd(command, currentPlayer);
            } else if (word.equals("get")) {
                return this.getCmd(command, currentPlayer);
            } else if (word.equals("drop")) {
                return this.dropCmd(command, currentPlayer);
            } else if (word.equals("goto")) {
                return this.gotoCmd(command, currentPlayer);
            } else if (word.equals("health")) {
                return this.healthCmd(command, currentPlayer);
            }
        }

       return "Unrecognized Command\n";
    }

    private String invCmd(String command, Player currentPlayer) {
        boolean haveFoundInv = false;
        StringTokenizer stringTokenizer = new StringTokenizer(command);
        while(stringTokenizer.hasMoreTokens()) {
            String word = stringTokenizer.nextToken();
            if(this.isRecognizedBasicCommand(word)){
                if((!word.equals("inventory") && !word.equals("inv")) || haveFoundInv) {
                    throw new IllegalArgumentException("[ERROR]: Composite Commands\n");
                }
                haveFoundInv = true;
            } else if (this.isRecognizedEntity(word)) {
                throw new IllegalArgumentException("[ERROR]: Extraneous Entities\n");
            }
        }
        return currentPlayer.inventoryToString();
    }

    private String lookCmd(String command, Player currentPlayer) {
        boolean haveFoundLook = false;
        StringTokenizer stringTokenizer = new StringTokenizer(command);
        while(stringTokenizer.hasMoreTokens()) {
            String word = stringTokenizer.nextToken();
            if(this.isRecognizedBasicCommand(word)) {
                if(!word.equals("look") || haveFoundLook) {
                    throw new IllegalArgumentException("[ERROR]: Composite Commands\n");
                }
                haveFoundLook = true;
            } else if (this.isRecognizedEntity(word)) {
                throw new IllegalArgumentException("[ERROR]: Extraneous Entities\n");
            }
        }

        Location currentLocation = currentPlayer.getCurrentLocation();
        StringBuilder sb = new StringBuilder();
        sb.append(currentLocation.locationDescription());

        // see if there is other player in the same location
        HashMap<String, Player> playersMap = this.playerInGame.getPlayersMap();
        for(Player player : playersMap.values()) {
            if(!player.getName().equals(currentPlayer.getName())) {
                if(player.getCurrentLocation().equals(currentLocation)) {
                    sb.append("There are some other players in the same place, they are: ")
                      .append(player.getName())
                      .append("\n");
                }
            }
        }

        return sb.toString();
    }

    private String getCmd(String command, Player currentPlayer) {
        boolean haveFoundGet = false;
        int recognizedEntityCount = 0;
        String recognizedEntity = null;

        StringTokenizer stringTokenizer = new StringTokenizer(command);
        while(stringTokenizer.hasMoreTokens()) {
            String word = stringTokenizer.nextToken();
            if(this.isRecognizedBasicCommand(word)) {
                if(!word.equals("get") || haveFoundGet){
                    throw new IllegalArgumentException("[ERROR]: Composite Commands\n");
                }
                haveFoundGet = true;
            } else if (this.isRecognizedEntity(word)) {  // check if the word belongs to an 'entity' first
                recognizedEntityCount++;
                if(recognizedEntityCount > 1) {
                    throw new IllegalArgumentException("[ERROR]: Composite Commands\n");
                }
                recognizedEntity = word;
            }
        }
        if(recognizedEntityCount == 0) {
            throw new IllegalArgumentException("[ERROR]: Unable to recognize the artefact\n");
        }

        Location currentLocation = currentPlayer.getCurrentLocation();
        Artefacts artefactsToPickUp = null;
        for(Artefacts artefacts : currentLocation.getArtefacts()) {
            if(artefacts.getName().equalsIgnoreCase(recognizedEntity)) {   // then check if the entity is an artefact included in current location
                artefactsToPickUp = artefacts;
                break;
            }
        }

        if(artefactsToPickUp == null) {   // means it's probably a furniture or character or even a location
            return "[ERROR]: You can't pick it up\n";
        }
        // adding the artefact to player's inv and removing from location
        currentPlayer.addToInv(artefactsToPickUp);
        currentLocation.removeArtefact(artefactsToPickUp);

        StringBuilder sb = new StringBuilder();
        sb.append("You picked up: ").append(artefactsToPickUp.getName()).append("\n");
        return sb.toString();
    }

    private String dropCmd(String command, Player currentPlayer) {
        boolean haveFoundDrop = false;
        String recognizedEntity = null;
        int recognizedEntityCount = 0;
        StringTokenizer stringTokenizer = new StringTokenizer(command);
        while(stringTokenizer.hasMoreTokens()) {
            String word = stringTokenizer.nextToken();
            if(this.isRecognizedBasicCommand(word)) {
                if(!word.equals("drop") || haveFoundDrop) {
                    throw new IllegalArgumentException("[ERROR]: Composite Commands\n");
                }
                haveFoundDrop = true;
            } else if (this.isRecognizedEntity(word)) {  // check if the word belongs to an 'entity'
                recognizedEntityCount++;
                if(recognizedEntityCount > 1) {
                    throw new IllegalArgumentException("[ERROR]: Composite Commands\n");
                }
                recognizedEntity = word;
            }
        }

        if(recognizedEntityCount == 0) {
            throw new IllegalArgumentException("[ERROR]: Unable to recognize the artefact\n");
        }
        Artefacts artefactsToDrop = null;
        for(Artefacts artefacts : currentPlayer.getInv()) {
            if(artefacts.getName().equalsIgnoreCase(recognizedEntity)) {  // then check if the entity belongs to an artefact included in current player's inv
                artefactsToDrop = artefacts;
                break;
            }
        }

        if(artefactsToDrop == null) {
            return "[ERROR]: You don't have this in your inventory\n";
        }

        // removing from player's inventory and adding it to location
        currentPlayer.removeFromInv(artefactsToDrop);
        currentPlayer.getCurrentLocation().addArtefact(artefactsToDrop);

        StringBuilder sb = new StringBuilder();
        sb.append("You dropped: ").append(artefactsToDrop.getName()).append("\n");
        return sb.toString();
    }

    private String gotoCmd(String command, Player currentPlayer) {
        boolean haveFoundGoto = false;
        String recognizedLocation = null;
        int recognizedLocationCount = 0;
        StringTokenizer stringTokenizer = new StringTokenizer(command);
        while(stringTokenizer.hasMoreTokens()) {
            String word = stringTokenizer.nextToken();
            if(this.isRecognizedBasicCommand(word)) {
                if(!word.equals("goto") || haveFoundGoto) {
                    throw new IllegalArgumentException("[ERROR]: Composite Commands\n");
                }
                haveFoundGoto = true;
            } else if (this.isRecognizedLocation(word)) {
                recognizedLocationCount++;
                if(recognizedLocationCount > 1) {
                    throw new IllegalArgumentException("[ERROR]: Composite Commands\n");
                }
                recognizedLocation = word;
            }
        }
        if(recognizedLocationCount == 0) {
            throw new IllegalArgumentException("[ERROR]: Unable to recognize the location\n");
        }
        LinkedList<Location> connectedLocations = currentPlayer.getCurrentLocation().getConnectedLocation();
        Location locationToGo = null;
        for(Location loc : connectedLocations) {
            if(loc.getName().equalsIgnoreCase(recognizedLocation)) {
                locationToGo = loc;
                break;
            }
        }
        if(locationToGo == null) {
            return "[ERROR]: There is no path to get there\n";
        }
        currentPlayer.setCurrentLocation(locationToGo);
        StringBuilder sb = new StringBuilder();
        sb.append("Go to ").append(locationToGo.getName()).append("\n");
        return sb.toString();
    }

    private String healthCmd(String command, Player currentPlayer) {
        boolean haveFoundHealth = false;
        StringTokenizer stringTokenizer = new StringTokenizer(command);
        while(stringTokenizer.hasMoreTokens()) {
            String word = stringTokenizer.nextToken();
            if(this.isRecognizedBasicCommand(word)) {
                if(!word.equals("health") || haveFoundHealth) {
                    throw new IllegalArgumentException("[ERROR]: Composite Commands\n");
                }
                haveFoundHealth = true;
            } else if (this.isRecognizedEntity(word)) {
                throw new IllegalArgumentException("[ERROR]: Extraneous Entities\n");
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(currentPlayer.getHealth()).append("\n");
        return sb.toString();
    }

    private boolean isRecognizedEntity(String word) {
        for(String key : this.loadedEntity.getAllEntitiesMap().keySet()) {
            if(key.equalsIgnoreCase(word)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRecognizedBasicCommand(String word) {
        if(word.equals("inventory") || word.equals("inv") || word.equals("get")
        || word.equals("drop") || word.equals("goto") || word.equals("look") || word.equals("health")) {
            return true;
        }
        return false;
    }

    private boolean isRecognizedLocation(String word) {
        for(String key : this.loadedEntity.getLocationsMap().keySet()) {
            if(key.equalsIgnoreCase(word)) {
                return true;
            }
        }
        return false;
    }

}
