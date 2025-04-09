package edu.uob;

import java.util.*;

public class ParsedCustomCommand {
    private LoadedEntity loadedEntity;
    private LoadedAction loadedAction;

    public ParsedCustomCommand(LoadedEntity loadedEntity, LoadedAction loadedAction) {
        this.loadedEntity = loadedEntity;
        this.loadedAction = loadedAction;
    }

    public String parsingCustomCommand(String command, Player currentPlayer) {
        command = command.toLowerCase().trim();

        Location currentLocation = currentPlayer.getCurrentLocation();
        LinkedList<String> triggersInCommand = new LinkedList<>();
        LinkedList<String> entitiesInCommand = new LinkedList<>();

        StringTokenizer stringTokenizer = new StringTokenizer(command);
        while(stringTokenizer.hasMoreTokens()) {
            String word = stringTokenizer.nextToken();
            if(this.isTrigger(word)) {
                triggersInCommand.add(word);
            } else if (this.isRecognizedEntity(word)) {
                entitiesInCommand.add(word);
            }
        }

        if(triggersInCommand.isEmpty()) {
            throw new IllegalArgumentException("[ERROR]: Cannot recognize the command, neither basic command nor custom command\n");
        }
        if(entitiesInCommand.isEmpty()) {
            throw new IllegalArgumentException("[ERROR]: No subject entity recognized\n");
        }

        LinkedList<GameAction> candidateAction = new LinkedList<>();   // store each action that meets all the conditions
        LinkedList<GameAction> extraneousEntitiesAction = new LinkedList<>();   // store each action that includes extraneous entities
        for(GameAction gameAction : this.loadedAction.getActionList()) {
            boolean foundTrigger = false;
            for(String trigger : triggersInCommand) {
                if(gameAction.getTriggers().contains(trigger)) {
                    foundTrigger = true;
                    break;
                }
            }
            if(!foundTrigger) continue;

            // if we find one trigger in an action, then we try to match the subjects then
            // entitiesInCommand should be a subset of this action's subjectsList
            if(this.isSubset(gameAction.getSubjects(), entitiesInCommand)) {
                candidateAction.add(gameAction);   // if it's a subset, add the action into candidate list
            } else {
                extraneousEntitiesAction.add(gameAction);  // which means we find an entity that not matches the action
            }
        }
        if(candidateAction.isEmpty()) {
            if(!extraneousEntitiesAction.isEmpty()) {
                throw new IllegalArgumentException("[ERROR]: Invalid action: extraneous entities\n");
            } else {
                throw new IllegalArgumentException("[ERROR]: Invalid action: no trigger matched\n");
            }
        } else if (candidateAction.size() > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[ERROR]: there is more than one ' ");
            for(String trigger : triggersInCommand) {
                stringBuilder.append(trigger).append(" ");
            }
            stringBuilder.append(" action possible - which one do you want to perform?\n");
            throw new IllegalArgumentException(stringBuilder.toString());
        }

        // now I get the only one matched action
        // next, we need to check if all subjects in this action are available
        GameAction matchedAction = candidateAction.get(0);
        String result = this.areAvailableSubjects(matchedAction.getSubjects(), currentPlayer);
        if(!result.equals("YES")) {
            StringBuilder sb = new StringBuilder();
            sb.append("[ERROR]: ").append(result).append("is/are not available\n");
            throw new IllegalArgumentException(sb.toString());
        }

        LinkedHashMap<String, GameEntity> allEntities = this.loadedEntity.getAllEntitiesMap();
        // Logic: Consumed
        // each consumed entity should be removed to storeroom except the location
        LinkedList<String> consumedEntities = matchedAction.getConsumed();
        boolean died = false;
        for(String itemName : consumedEntities) {
            boolean isLocation = false;
            if(itemName.equalsIgnoreCase("health")) {
                currentPlayer.reduceHealth();
                if(!currentPlayer.isAlive()) {
                    currentPlayer.loseInvWhenDead();
                    currentPlayer.resetHealth();
                    currentPlayer.setCurrentLocation(this.loadedEntity.getStartLocation());
                    died = true;
                }
                continue;  // see next consumed entity
            }
            // if the consumed entity is a location, delete the one-way path from current location to the 'consumed' location
            for(String locName : this.loadedEntity.getLocationsMap().keySet()) {
                if(locName.equalsIgnoreCase(itemName)) {
                    currentLocation.removePath(itemName);
                    isLocation = true;
                    break;
                }
            }
            if(isLocation) continue;

            // if the consumed entity is not a location, then we need to delete it from current place
            // first, we need to figure out the class of the consumed entity
            GameEntity entity = allEntities.get(itemName);
            if(entity instanceof Artefacts) {
                boolean haveFound = false;
                // iterate through the location to find this artefact
                for(Location loc : this.loadedEntity.getLocationsMap().values()) {
                    for(Artefacts a : loc.getArtefacts()) {
                        if(a.getName().equalsIgnoreCase(itemName)) {
                            loc.removeArtefact(a);
                            this.loadedEntity.getStoreroom().addArtefact(a);
                            haveFound = true;
                            break;
                        }
                    }
                    if(haveFound) break;
                }
                if(haveFound) continue;
                // then, iterate through the inv to find
                for(Artefacts a : currentPlayer.getInv()) {
                    if(a.getName().equalsIgnoreCase(itemName)) {
                        currentPlayer.removeFromInv(a);
                        this.loadedEntity.getStoreroom().addArtefact(a);
                        break;
                    }
                }
            } else if (entity instanceof Furniture) {
                for(Location loc : this.loadedEntity.getLocationsMap().values()) {
                    for(Furniture f : loc.getFurniture()) {
                        if(f.getName().equalsIgnoreCase(itemName)) {
                            loc.removeFurniture(f);
                            this.loadedEntity.getStoreroom().addFurniture(f);
                        }
                    }
                }
            } else if (entity instanceof Characters) {
                for(Location loc : this.loadedEntity.getLocationsMap().values()) {
                    for(Characters c : loc.getCharacters()) {
                        if(c.getName().equalsIgnoreCase(itemName)) {
                            loc.removeCharacter(c);
                            this.loadedEntity.getStoreroom().addCharacter(c);
                        }
                    }
                }
            }
        }

        // Logic: Produced
        // each produced entity should be removed from storeroom and added to current location
        LinkedList<String> producedEntities = matchedAction.getProduced();
        for (String itemName : producedEntities) {
            if (itemName.equalsIgnoreCase("health")) {
                currentPlayer.increaseHealth();
                continue;
            }
            boolean isLocation = false;
            // if the produced entity is a location, add the one-way path from current location to the 'produced' location
            for (String locName : this.loadedEntity.getLocationsMap().keySet()) {
                if (locName.equals(itemName)) {
                    Location addingLoc = this.loadedEntity.getLocationsMap().get(itemName);
                    currentLocation.addConnectedPath(addingLoc);
                    isLocation = true;
                    break;
                }
            }
            if (isLocation) continue;

            GameEntity entity = allEntities.get(itemName);
            if (entity instanceof Artefacts) {
                this.loadedEntity.getStoreroom().removeArtefact((Artefacts) entity);
                currentLocation.addArtefact((Artefacts) entity);
            } else if (entity instanceof Furniture) {
                this.loadedEntity.getStoreroom().removeFurniture((Furniture) entity);
                currentLocation.addFurniture((Furniture) entity);
            } else if (entity instanceof Characters) {
                this.loadedEntity.getStoreroom().removeCharacter((Characters) entity);
                currentLocation.addCharacter((Characters) entity);
            }
        }

        if(died) {
            return "you died and lost all of your items, you must return to the start of the game\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(matchedAction.getNarration()).append("\n");
        return sb.toString();
    }

    private boolean isTrigger(String word) {
        for(String item : this.loadedAction.getAllTriggersList()) {
            if(item.equalsIgnoreCase(word)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRecognizedEntity(String word) {
        for(String item : this.loadedEntity.getAllEntitiesMap().keySet()) {
            if(item.equalsIgnoreCase(word)) {
                return true;
            }
        }
        return false;
    }


    private boolean isSubset(LinkedList<String> list1, LinkedList<String> list2) {
        return list1.containsAll(list2);
    }

    private String areAvailableSubjects(LinkedList<String> matchedActionSubjects, Player currentPlayer) {
        Location currentLocation = currentPlayer.getCurrentLocation();
        // subjects should be in player's inv, or in current location, or location itself
        HashSet<String> availableNames = new HashSet<>();
        availableNames.add(currentLocation.getName());

        for(Artefacts a : currentLocation.getArtefacts()) {
            availableNames.add(a.getName().toLowerCase());
        }
        for(Furniture f : currentLocation.getFurniture()) {
            availableNames.add(f.getName().toLowerCase());
        }
        for(Characters c : currentLocation.getCharacters()) {
            availableNames.add(c.getName().toLowerCase());
        }
        for(Artefacts item : currentPlayer.getInv()) {
            availableNames.add(item.getName().toLowerCase());
        }

        StringBuilder sb = new StringBuilder();
        for(String subjectName : matchedActionSubjects) {
            if(!availableNames.contains(subjectName)) {
                sb.append("'").append(subjectName).append("' ");
            }
        }
        if(sb.isEmpty()) {
            return "YES";
        } else {
            return sb.toString();
        }
    }

}
