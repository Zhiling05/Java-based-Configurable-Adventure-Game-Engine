package edu.uob;

import java.util.LinkedList;

public class GameAction {
    private LinkedList<String> triggers;
    private LinkedList<String> subjects;
    private LinkedList<String> consumed;
    private LinkedList<String> produced;
    private String narration;

    public GameAction() {
        this.triggers = new LinkedList<>();
        this.subjects = new LinkedList<>();
        this.consumed = new LinkedList<>();
        this.produced = new LinkedList<>();
        this.narration = "";
    }

    public LinkedList<String> getTriggers() {
        return this.triggers;
    }

    public void addTrigger(String keyphrase) {
        this.triggers.add(keyphrase);
    }

    public LinkedList<String> getSubjects() {
        return this.subjects;
    }

    public void addSubject(String entity) {
        this.subjects.add(entity);
    }

    public LinkedList<String> getConsumed() {
        return this.consumed;
    }

    public void addConsumed(String entity) {
        this.consumed.add(entity);
    }

    public LinkedList<String> getProduced() {
        return this.produced;
    }

    public void addProduced(String entity) {
        this.produced.add(entity);
    }

    public String getNarration() {
        return this.narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }
}
