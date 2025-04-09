package edu.uob;

import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.*;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedHashMap;


public class LoadedEntity {
    private Location storeroom = null;
    private final LinkedHashMap<String, Location> locationsMap;   // include all locations in the game, storeroom is not included
    private final LinkedHashMap<String, GameEntity> allEntities;  // include everything except the items in the storeroom


    public LoadedEntity() {
        this.locationsMap = new LinkedHashMap<>();
        this.allEntities = new LinkedHashMap<>();
    }

    // loading entities from .dot file
    public void loadEntities(File entitiesFile) throws ParseException, IOException {
        Parser parser = new Parser();
        try (FileInputStream file = new FileInputStream(entitiesFile)) {
            InputStreamReader reader = new InputStreamReader(file);
            boolean result = parser.parse(reader);
            if (!result) throw new ParseException("Failed to load entities.");
        }

        for (Graph topGraph : parser.getGraphs()) {
            // loading the subgraph of the top graph
            for (Graph subgraph : topGraph.getSubgraphs()) {
                if (subgraph.getId() != null) {
                    if (subgraph.getId().getId().equalsIgnoreCase("locations")) {
                        this.parsedLocationsSubgraph(subgraph);
                    } else if (subgraph.getId().getId().equalsIgnoreCase("paths")) {
                        this.parsedPathsSubgraph(subgraph);
                    }
                }
            }
        }

        if(!this.locationsMap.containsKey("storeroom")) {
            this.storeroom = new Location("storeroom", "Storage for any entities not placed in the game");
        } else {
            this.locationsMap.remove("storeroom");
        }

        this.allEntities.putAll(locationsMap);
    }

    private void parsedLocationsSubgraph(Graph locationsSubgraph) {
        for (Graph clusterSubgraph : locationsSubgraph.getSubgraphs()) {
            this.parsedClusterSubgraph(clusterSubgraph);
        }
    }

    private void parsedClusterSubgraph(Graph clusterSubgraph) {
        Iterator<Node> nodeIterator = clusterSubgraph.getNodes(false).iterator();
        if(!nodeIterator.hasNext()) return;

        Node locationNode = nodeIterator.next();
        // get the name and description of the location
        String locationName = locationNode.getId().getId();
        String locationDescription = locationNode.getAttribute("description");
        if(locationDescription == null) {
            locationDescription = "";
        }

        Location newLoc = new Location(locationName, locationDescription);
        this.locationsMap.put(locationName.toLowerCase(), newLoc);

        for (Graph innerGraph : clusterSubgraph.getSubgraphs()) {
            // get the name of the entity class
            String entityClass = innerGraph.getId().getId();

            // add the included entities into the location
            for (Node entityNode : innerGraph.getNodes(false)) {
                String entityName = entityNode.getId().getId();
                String entityDescription = entityNode.getAttribute("description");
                if (entityDescription == null) {
                    entityDescription = "";
                }
                if (entityClass.equalsIgnoreCase("artefacts")) {
                    Artefacts artefact = new Artefacts(entityName.toLowerCase(), entityDescription);
                    newLoc.addArtefact(artefact);
                    this.allEntities.put(entityName.toLowerCase(), artefact);
                } else if (entityClass.equalsIgnoreCase("furniture")) {
                    Furniture furniture = new Furniture(entityName.toLowerCase(), entityDescription);
                    newLoc.addFurniture(furniture);
                    this.allEntities.put(entityName.toLowerCase(), furniture);
                } else if (entityClass.equalsIgnoreCase("characters")) {
                    Characters character = new Characters(entityName.toLowerCase(), entityDescription);
                    newLoc.addCharacter(character);
                    this.allEntities.put(entityName.toLowerCase(), character);
                }
            }
        }
        if(locationName.equalsIgnoreCase("storeroom")) {
            this.storeroom = newLoc;
        }
    }

    public void parsedPathsSubgraph(Graph pathsSubgraph) {
        for (Edge edge : pathsSubgraph.getEdges()) {
            String sourceName = edge.getSource().getNode().getId().getId();
            String targetName = edge.getTarget().getNode().getId().getId();
            Location sourceLocation = this.locationsMap.get(sourceName.toLowerCase());
            Location targetLocation = this.locationsMap.get(targetName.toLowerCase());
            if (sourceLocation != null && targetLocation != null) {
                sourceLocation.addConnectedPath(targetLocation);
            }
        }
    }

    public LinkedHashMap<String, Location> getLocationsMap() {
        return this.locationsMap;
    }

    public Location getStartLocation() {
        return this.locationsMap.values().iterator().next();
    }

    public LinkedHashMap<String, GameEntity> getAllEntitiesMap() {
        return this.allEntities;
    }

    public Location getStoreroom() {
        return this.storeroom;
    }
}
