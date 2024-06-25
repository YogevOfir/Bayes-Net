
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Element;
import java.io.File;
import org.w3c.dom.NodeList;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;


public class BayesianNetwork {
    private HashMap<String, myNode> map;
    private String xmlName;

    public BayesianNetwork(String name) {
        map = new HashMap<>();
        xmlName = name;
    }

    public void networkBuild() {
        try {
            Document doc = parseXMLFile(xmlName);

            NodeList variables = doc.getElementsByTagName("VARIABLE");
            populateNodes(variables);

            NodeList definitions = doc.getElementsByTagName("DEFINITION");
            buildNetwork(definitions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Document parseXMLFile(String xmlName) throws Exception {
        File file = new File(xmlName);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();
        return doc;
    }

    private void populateNodes(NodeList variables) {
        for (int i = 0; i < variables.getLength(); i++) {
            Node node = variables.item(i);
            Element element = (Element) node;
            myNode vertex = new myNode(element.getElementsByTagName("NAME").item(0).getTextContent());
            createOutcomes(node, vertex);
            map.put(vertex.name, vertex);
        }
    }

    private void buildNetwork(NodeList definitions) {
        for (int i = 0; i < definitions.getLength(); i++) {
            Node node = definitions.item(i);
            Element element = (Element) node;

            myNode child = map.get(element.getElementsByTagName("FOR").item(0).getTextContent());
            NodeList parents = element.getElementsByTagName("GIVEN");
            String tableData = element.getElementsByTagName("TABLE").item(0).getTextContent();

            setupParents(child, parents);
            createTable(child, tableData);
        }
    }
    private void setupParents(myNode child, NodeList parents) {
        child.size = parents.getLength();
        int j = 0;
        while (j < parents.getLength()) {
            myNode parent = map.get(parents.item(j).getTextContent());
            parent.childrens.add(child);
            child.parents.add(parent);
            j++;
        }
    }

    private void createOutcomes(Node node, myNode vertex) {
        NodeList outcomeList = ((Element) node).getElementsByTagName("OUTCOME");
        int i = 0;
        while (i < outcomeList.getLength()) {
            vertex.outcome.add(outcomeList.item(i).getTextContent());
            i++;
        }
    }

    private void createTable(myNode node, String tableData) {
        String[] valuesArray = tableData.split(" ");
        int numValues = valuesArray.length;

        node.tableAdd(node.name);
        for (myNode parent : node.parents) {
            node.tableAdd(parent.name);
        }
        node.tableAdd("values");

        List<String> valuesList = node.getFromTable("values");
        Collections.addAll(valuesList, valuesArray);

        fillNodeOutcomes(node, numValues);
        fillParentOutcomes(node, numValues);
    }

    private void fillNodeOutcomes(myNode node, int numValues) {
        int outcomeIndex = 0;
        List<String> valuesList = node.getFromTable(node.name);
        String[] nodeOutcomes = node.getOutcome();
        int i = 0;
        while (i < numValues) {
            valuesList.add(nodeOutcomes[outcomeIndex]);
            outcomeIndex = (outcomeIndex + 1) % nodeOutcomes.length;
            i++;
        }
    }

    private void fillParentOutcomes(myNode node, int numValues) {
        int blockSize = node.getOutcome().length;
        LinkedList<myNode> parents = node.parents;
        Iterator<myNode> parentIterator = parents.descendingIterator();

        while (parentIterator.hasNext()) {
            myNode parentNode = parentIterator.next();
            List<String> valuesList = node.getFromTable(parentNode.name);
            String[] parentOutcomes = parentNode.getOutcome();
            int outcomeIndex = 0;

            int i = 0;
            while (i < numValues) {
                int j = 0;
                while (j < blockSize) {
                    valuesList.add(parentOutcomes[outcomeIndex]);
                    j++;
                }
                outcomeIndex = (outcomeIndex + 1) % parentOutcomes.length;
                i += blockSize;
            }
            blockSize = blockSize * parentOutcomes.length;
        }
    }

    public Set<String> getNodes() {
        return map.keySet();
    }

    public myNode findNode(String name) {
        return map.getOrDefault(name, null);
    }

    public Collection<myNode> getMap() {
        return map.values();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String key : map.keySet()) {
            myNode node = map.get(key);
            sb.append("node: ").append(key).append(" parents: ");
            for (myNode parent : node.parents) {
                sb.append(parent.name).append(" ");
            }
            sb.append("CPT: ");
            for (Double value : node.CPT.values()) {
                sb.append(value).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
