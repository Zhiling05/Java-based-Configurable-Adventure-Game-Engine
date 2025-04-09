package edu.uob;

import java.io.IOException;
import java.io.File;
import java.util.LinkedList;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class LoadedAction {
    private final LinkedList<GameAction> actionList;
    private final LinkedList<String> allTriggersList;

    public LoadedAction() {
        this.actionList = new LinkedList<>();
        this.allTriggersList = new LinkedList<>();
    }

    public void loadActions(File actionsFile) throws ParserConfigurationException, IOException, SAXException, DOMException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(actionsFile);
        Element root = document.getDocumentElement(); // get the <actions>

        // get all <action> node
        NodeList actions = root.getElementsByTagName("action");
        for (int i = 0; i < actions.getLength(); i++) {    // iterate through each <action>
            Node actionNode = actions.item(i);    // extract one <action>, and perform subsequent operations on this <action>
            if(actionNode.getNodeType() != Node.ELEMENT_NODE) continue;

            Element action = (Element) actionNode;
            GameAction gameAction = new GameAction();

            // get <triggers>, <subjects>, <consumed>, <produced> of each <action>
            NodeList actionElements = action.getChildNodes();
            for (int j = 0; j < actionElements.getLength(); j++) {
                Node actionElementNode = actionElements.item(j);
                // extract only the nodes belonging to the ELEMENT, removing line breaks and tabs
                if(actionElementNode.getNodeType() != Node.ELEMENT_NODE) continue;

                Element actionElement = (Element) actionElementNode;
                String elementName = actionElement.getNodeName();
                if (elementName.equalsIgnoreCase("triggers")) {
                    NodeList triggerElements = actionElement.getChildNodes();
                    for (int k = 0; k < triggerElements.getLength(); k++) {
                        Node triggerElementNode = triggerElements.item(k);
                        if(triggerElementNode.getNodeType() != Node.ELEMENT_NODE) continue;

                        Element triggerElement = (Element) triggerElementNode;
                        if (triggerElement.getNodeName().equalsIgnoreCase("keyphrase")) {
                            String phrase = triggerElement.getTextContent().trim();
                            gameAction.addTrigger(phrase.toLowerCase());
                        }
                    }
                } else if (elementName.equalsIgnoreCase("subjects")) {
                    NodeList subjectElements = actionElement.getChildNodes();
                    for (int k = 0; k < subjectElements.getLength(); k++) {
                        Node subjectElementNode = subjectElements.item(k);
                        if(subjectElementNode.getNodeType() != Node.ELEMENT_NODE) continue;

                        Element subjectElement = (Element) subjectElementNode;
                        if (subjectElement.getNodeName().equalsIgnoreCase("entity")) {
                            String entity = subjectElement.getTextContent().trim();
                            gameAction.addSubject(entity.toLowerCase());
                        }
                    }
                } else if (elementName.equalsIgnoreCase("consumed")) {
                    NodeList consumedElements = actionElement.getChildNodes();
                    for (int k = 0; k < consumedElements.getLength(); k++) {
                        Node consumedElementNode = consumedElements.item(k);
                        if(consumedElementNode.getNodeType() != Node.ELEMENT_NODE) continue;

                        Element consumedElement = (Element) consumedElementNode;
                        if (consumedElement.getNodeName().equalsIgnoreCase("entity")) {
                            String entity = consumedElement.getTextContent().trim();
                            gameAction.addConsumed(entity.toLowerCase());
                        }
                    }
                } else if (elementName.equalsIgnoreCase("produced")) {
                    NodeList producedElements = actionElement.getChildNodes();
                    for (int k = 0; k < producedElements.getLength(); k++) {
                        Node producedElementNode = producedElements.item(k);
                        if(producedElementNode.getNodeType() != Node.ELEMENT_NODE) continue;

                        Element producedElement = (Element) producedElementNode;
                        if (producedElement.getNodeName().equalsIgnoreCase("entity")) {
                            String entity = producedElement.getTextContent().trim();
                            gameAction.addProduced(entity.toLowerCase());
                        }
                    }
                } else if (elementName.equalsIgnoreCase("narration")) {
                    String narration = actionElement.getTextContent().trim();
                    gameAction.setNarration(narration);
                }
            }
            actionList.add(gameAction);
        }

        for(GameAction action : actionList) {
            this.allTriggersList.addAll(action.getTriggers());
        }
    }

    public LinkedList<GameAction> getActionList() {
        return this.actionList;
    }

    public LinkedList<String> getAllTriggersList() {
        return this.allTriggersList;
    }

}
