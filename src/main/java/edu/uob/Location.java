package edu.uob;

import java.util.Iterator;
import java.util.LinkedList;

public class Location extends GameEntity{
    private final LinkedList<Artefacts> artefacts;
    private final LinkedList<Furniture> furniture;
    private final LinkedList<Characters> characters;
    private final LinkedList<Location> connectedLocation;

    public Location(String name, String description) {
        super(name, description);
        this.artefacts = new LinkedList<>();
        this.furniture = new LinkedList<>();
        this.characters = new LinkedList<>();
        this.connectedLocation = new LinkedList<>();
    }

    public LinkedList<Characters> getCharacters() {
        return this.characters;
    }

    public LinkedList<Artefacts> getArtefacts() {
        return this.artefacts;
    }

    public LinkedList<Furniture> getFurniture() {
        return this.furniture;
    }

    public LinkedList<Location> getConnectedLocation() {
        return this.connectedLocation;
    }

    public void removePath(String targetLocName) {
        Iterator<Location> iterator = this.getConnectedLocation().iterator();
        while (iterator.hasNext()) {
            Location loc = iterator.next();
            if(loc.getName().equals(targetLocName)) {
                iterator.remove();
                break;
            }
        }
    }

    public void removeArtefact(Artefacts artefact) {
        Iterator<Artefacts> iterator = this.artefacts.iterator();
        while(iterator.hasNext()) {
            Artefacts a = iterator.next();
            if(a.getName().equalsIgnoreCase(artefact.getName())) {
                iterator.remove();
                break;
            }
        }
    }

    public void removeFurniture(Furniture furniture) {
        Iterator<Furniture> iterator = this.furniture.iterator();
        while(iterator.hasNext()) {
            Furniture f = iterator.next();
            if(f.getName().equalsIgnoreCase(furniture.getName())) {
                iterator.remove();
                break;
            }
        }
    }

    public void removeCharacter(Characters character) {
        Iterator<Characters> iterator = this.characters.iterator();
        while (iterator.hasNext()) {
            Characters c = iterator.next();
            if(c.getName().equalsIgnoreCase(character.getName())) {
                iterator.remove();
                break;
            }
        }
    }

    public void addArtefact(Artefacts artefacts) {
        this.artefacts.add(artefacts);
    }

    public void addFurniture(Furniture furniture) {
        this.furniture.add(furniture);
    }

    public void addCharacter(Characters character) {
        this.characters.add(character);
    }

    public void addConnectedPath(Location targetLocation) {
        this.connectedLocation.add(targetLocation);
    }

    public String locationDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("You are at the ")
          .append(this.getName())
          .append(" - ")
          .append(this.getDescription())
          .append("\n");

        // description of furniture
        if(this.furniture.isEmpty()) {
            sb.append("There is no furniture here");
        } else {
            sb.append("Furniture includes: ");
            Iterator<Furniture> iterator = this.furniture.iterator();
            while(iterator.hasNext()) {
                Furniture item = iterator.next();
                sb.append(item.getDescription());
                if(iterator.hasNext()) {
                    sb.append(", ");
                }
            }
        }
        sb.append("\n");

        // description of artefacts
        if(this.artefacts.isEmpty()) {
            sb.append("Nothing can be picked up");
        } else {
            sb.append("Items to pick up include: ");
            Iterator<Artefacts> iterator = this.artefacts.iterator();
            while(iterator.hasNext()) {
                Artefacts item = iterator.next();
                sb.append(item.getDescription());
                if(iterator.hasNext()) {
                    sb.append(", ");
                }
            }
        }
        sb.append("\n");

        // description of characters
        if(this.characters.isEmpty()) {
            sb.append("No other creatures or animals here, don't be afraid");
        } else {
            sb.append("Be careful, you can also see: ");
            Iterator<Characters> iterator = this.characters.iterator();
            while(iterator.hasNext()) {
                Characters item = iterator.next();
                sb.append(item.getDescription());
                if(iterator.hasNext()) {
                    sb.append(", ");
                }
            }
        }
        sb.append("\n");

        // description of paths
        if(this.connectedLocation.isEmpty()) {
            sb.append("There is no path leading to any location");
        } else {
            sb.append("There is a path leading to : ");
            Iterator<Location> iterator = this.connectedLocation.iterator();
            while(iterator.hasNext()) {
                Location item = iterator.next();
                sb.append(item.getName())
                  .append(" - ")
                  .append(item.getDescription());
                if(iterator.hasNext()) {
                    sb.append(", ");
                }
            }
        }
        sb.append("\n");
        return sb.toString();
    }
}
