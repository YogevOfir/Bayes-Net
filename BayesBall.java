import java.util.LinkedList;
import java.util.Queue;

public class BayesBall {
    private myNode start, end;

    public BayesBall(BayesianNetwork bayesNet, String line) {
        resetNodes(bayesNet);
        initializeStartEndNodes(bayesNet, line);
        setEvidenceNodes(bayesNet, line);
    }

    private void resetNodes(BayesianNetwork bayesNet) {
        for (myNode node : bayesNet.getMap()) {
            node.FromChild = false;
            node.visitedByChild = false;
            node.FromParent = false;
            node.visitedByParent = false;
            node.color = false;
        }
    }

    private void initializeStartEndNodes(BayesianNetwork bayesNet, String line) {
        String[] parts = line.split("\\|")[0].split("-");
        start = bayesNet.findNode(parts[0]);
        end = bayesNet.findNode(parts[1]);
    }

    private void setEvidenceNodes(BayesianNetwork bayesNet, String line) {
        if (line.indexOf("|") == line.length() - 1) return;
        String evidenceString = line.split("\\|")[1];
        for (String key : evidenceString.split(",")) {
            bayesNet.findNode(key.split("=")[0]).color = true;
        }
    }

    public String BayesBallQuery() {
        if (start.childrens.contains(end) || start.parents.contains(end)) {
            return "no";
        }

        Queue<myNode> queue = new LinkedList<>();
        enqueueChildrenAndParents(start, queue);

        while (!queue.isEmpty()) {
            myNode node = queue.poll();

            if (node == end) {
                return "no";
            }

            if (!node.color) {
                processUncoloredNode(node, queue);
            } else if (node.FromParent) {
                processColoredNodeFromParent(node, queue);
            }
        }

        return "yes";
    }

    private void enqueueChildrenAndParents(myNode node, Queue<myNode> queue) {
        for (myNode child : node.childrens) {
            child.FromParent = true;
            queue.add(child);
        }
        for (myNode parent : node.parents) {
            parent.FromChild = true;
            queue.add(parent);
        }
    }

    private void processUncoloredNode(myNode node, Queue<myNode> queue) {
        if (node.FromChild) {
            enqueueChildren(node, queue);
            enqueueParents(node, queue);
        } else {
            enqueueChildren(node, queue);
        }
        node.FromChild = false;
        node.FromParent = false;
    }

    private void processColoredNodeFromParent(myNode node, Queue<myNode> queue) {
        enqueueParents(node, queue);
        node.FromParent = false;
    }

    private void enqueueChildren(myNode node, Queue<myNode> queue) {
        for (myNode child : node.childrens) {
            if (!child.visitedByParent) {
                child.visitedByParent = true;
                child.FromParent = true;
                queue.add(child);
            }
        }
    }

    private void enqueueParents(myNode node, Queue<myNode> queue) {
        for (myNode parent : node.parents) {
            if (!parent.visitedByChild) {
                parent.visitedByChild = true;
                parent.FromChild = true;
                queue.add(parent);
            }
        }
    }
}
