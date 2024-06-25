import java.util.*;

public class BFS {

    public static void verifyAncestor(myNode start, BayesianNetwork bayesNet, HashMap<myNode, Integer> map) {
        resetVisitedFlags(bayesNet);
        performBFS(start);
        updateAncestorMap(bayesNet, map);
    }

    private static void resetVisitedFlags(BayesianNetwork bayesNet) {
        for (myNode node : bayesNet.getMap()) {
            node.visitedByBFS = false;
        }
    }

    private static void performBFS(myNode start) {
        Queue<myNode> queue = new LinkedList<>();
        queue.add(start);
        start.visitedByBFS = true;

        while (!queue.isEmpty()) {
            myNode currNode = queue.poll();
            for (myNode parent : currNode.parents) {
                if (!parent.visitedByBFS) {
                    parent.visitedByBFS = true;
                    queue.add(parent);
                }
            }
        }
    }

    private static void updateAncestorMap(BayesianNetwork bayesNet, HashMap<myNode, Integer> map) {
        for (myNode node : bayesNet.getMap()) {
            if (!node.visitedByBFS) {
                map.put(node, map.getOrDefault(node, 0) + 1);
            }
            node.visitedByBFS = false;
        }
    }
}
