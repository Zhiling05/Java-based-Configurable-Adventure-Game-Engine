package edu.uob;

import java.util.Iterator;
import java.util.LinkedList;

public class Player extends GameEntity {
    private Location currentLocation;
    private final LinkedList<Artefacts> inv;
    private int health;


    public Player(String username, String description, Location startLocation) {
        super(username, description);
        this.currentLocation = startLocation;
        this.inv = new LinkedList<>();
        this.health = 3;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public LinkedList<Artefacts> getInv() {
        return inv;
    }

    public int getHealth() {
        return health;
    }

    public void reduceHealth() {
        this.health--;
    }

    public void increaseHealth() {
        if(this.health < 3) {
            this.health++;
        }
    }

    public void addToInv(Artefacts artefacts) {
        this.inv.add(artefacts);
    }


    public void removeFromInv(Artefacts artefact) {
        Iterator<Artefacts> iterator = this.inv.iterator();
        while(iterator.hasNext()) {
            Artefacts a = iterator.next();
            if(a.getName().equalsIgnoreCase(artefact.getName())) {
                iterator.remove();
                break;
            }
        }
    }

    public String inventoryToString() {
        StringBuilder sb = new StringBuilder();
        if(this.inv.isEmpty()) {
            return "Your inventory is empty\n";
        } else {
            sb.append("You currently have: ");
            Iterator<Artefacts> iterator = this.inv.iterator();
            while(iterator.hasNext()) {
                Artefacts item = iterator.next();
                sb.append(item.getDescription());
                if(iterator.hasNext()) {
                    sb.append(",");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public boolean isAlive() {
        return this.health > 0;
    }

    public void loseInvWhenDead() {
        LinkedList<Artefacts> itemToLose = new LinkedList<>(this.inv);

        this.inv.clear();
        for(Artefacts artefacts : itemToLose) {
            this.currentLocation.addArtefact(artefacts);
        }
    }

    public void resetHealth() {
        this.health = 3;
    }
}
