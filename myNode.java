import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class myNode {
   
    LinkedList<myNode> parents;
    LinkedList<myNode> childrens;
    boolean visitedByParent;
    boolean visitedByChild;
    boolean FromParent;
    boolean FromChild;
    boolean color;
    HashMap<Integer, Double> CPT;
    ArrayList<ArrayList<String>> table;
    ArrayList<String> outcome;
    int size;
    boolean visitedByBFS;
    String name;

    public myNode(String name) {
        parents = new LinkedList<>();
        childrens = new LinkedList<>();
        FromChild = false;
        FromParent = false;
        table = new ArrayList<>();
        outcome = new ArrayList<>();
        size = 2;
        visitedByBFS = false;
        color = false;
        this.name = name;
        CPT = new HashMap<>();
    }

    public String[] getOutcome() {
        return outcome.toArray(new String[outcome.size()]);
    }

    public void tableAdd(String s) {
        ArrayList<String> list = new ArrayList<>();
        list.add(s);
        table.add(list);
    }

    public ArrayList<String> getFromTable(String s) {
        int i = 0;
        while (i < table.size()) {
            if (table.get(i).get(0) == s)
                return table.get(i);
            i++;
        }
        return null;
    }

    public int getAsciiSize() {
        int sum = 0;
        int i = 0;
        while (i < table.size() - 1) {
            sum = sum + table.get(i).get(0).charAt(0);
            i++;
        }
        return sum;
    }

}