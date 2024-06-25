import java.util.ArrayList;
import java.util.HashMap;

public class Factor implements Comparable<Factor> {

    public String name;
    public myNode node;
    public HashMap<String, String> map;
    public ArrayList<ArrayList<String>> table;

    public Factor(myNode node, HashMap<String, String> map) {
        this.map = map;
        this.node = node;
        this.name = node.name;
        this.table = new ArrayList<>();
    }

    public Factor(String a, String b) {
        this.table = new ArrayList<>();
        this.map = new HashMap<>();
        this.name = a + b;
    }

    public void createFactor() {
        ArrayList<ArrayList<String>> nodeTable = node.table;
        HashMap<Integer, String> valuesToRemove = new HashMap<>();

        for (ArrayList<String> row : nodeTable) {
            ArrayList<String> temp = new ArrayList<>(row);
            table.add(temp);
        }

        int i = 0;
        while (i < table.size() - 1) {
            if (map.containsKey(table.get(i).get(0))) {
                valuesToRemove.put(i, map.get(table.get(i).get(0)));
            }
            i++;
        }

        int k = 1;
        while (k < table.get(0).size()) {
            for (Integer key : valuesToRemove.keySet()) {
                if (!table.get(key).get(k).equals(valuesToRemove.get(key))) {
                    for (ArrayList<String> row : table) {
                        row.remove(k);
                    }
                    k--;
                    break;
                }
            }
            k++;
        }
        
        int j = 0;
        while (j < table.size()) {
            if (map.containsKey(table.get(j).get(0))) {
                table.remove(j);
                j--;
            }
            j++;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name).append(" factor\n");
        for (int i = 0; i < table.get(0).size(); i++) {
            for (ArrayList<String> row : table) {
                sb.append(row.get(i)).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private int getAsciiSum(Factor factor) {
        int sum = 0;
        int i = 0;
        while (i < table.size() - 1) {
            sum = sum + table.get(i).get(0).charAt(0);
            i++;
        }
        return sum;
    }

    @Override
    public int compareTo(Factor other) {
        int sizeComparison = Integer.compare(this.table.size(), other.table.size());
        if (sizeComparison != 0) {
            return sizeComparison;
        }
        return Integer.compare(getAsciiSum(this), getAsciiSum(other));
    }
}
