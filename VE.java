import java.util.*;

public class VE {
    private int num_sum;
    private int Product_num;
    private ArrayList<Factor> hiddenList;
    private BayesianNetwork bayesNet;
    private ArrayList<String> hiddenListOrder;
    private ArrayList<Factor> factorsList;
    String type;

    public VE(BayesianNetwork bayesNet) {
        this.bayesNet = bayesNet;
        type = "";
        Product_num = 0;
        num_sum = 0;
        hiddenListOrder = new ArrayList<>();
        hiddenList = new ArrayList<>();
        factorsList = new ArrayList<>();
    }

    public String calculate(String query) {
        query = query.replaceAll("\\s", "");
        String removeGarbage = query.substring(query.indexOf('(') + 1, query.indexOf(')'));
        type = query.substring(query.indexOf("=") + 1, query.indexOf("|"));
        HashMap<String, String> map = new HashMap<>();
        HashMap<myNode, Integer> bfsMap = new HashMap<>();
        removeGarbage = cleanseGarbage(removeGarbage, map, bfsMap);
        String hiddenListString = hiddenListAndOrder(query);
        String queryString = query.substring(2, query.indexOf("="));
        BFS.verifyAncestor(bayesNet.findNode(queryString), bayesNet, bfsMap);
        ArrayList<String> removalList = new ArrayList<>();
        for (Map.Entry<myNode, Integer> mapEntry : bfsMap.entrySet()) {
            if (mapEntry.getValue() == map.size() + 1) {
                removalList.add(mapEntry.getKey().name);
            }
        }
        double immediateAnswer = fetchDirectAnswer(map, queryString, type);
        String hiddenBB = "";
        int sizeIdx = 0;
        while (immediateAnswer > sizeIdx){
            return String.valueOf(immediateAnswer) + ",0,0";
            
        }
        for (myNode currentNode : bayesNet.getMap()) {
            Factor factorInstance = new Factor(currentNode, map);
            factorInstance.createFactor();
            factorsList.add(factorInstance);
        }
        updateHiddenList(hiddenListString); // removing hiddenList factorsList from factorsList and adding them to                        // hiddenList;
        removeSingleValueFactors();
        for (Map.Entry<String, String> mapEntry : map.entrySet()) {
            hiddenBB = hiddenBB + mapEntry.getKey() + "=" + mapEntry.getValue() + ",";
        }
        ArrayList<String> bbRemovalList = new ArrayList<>();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            hiddenBB += entry.getKey() + "=" + entry.getValue() + ",";
        }
        int i = 0;
        if (hiddenBB.length() > 0) {
            hiddenBB = hiddenBB.substring(0, hiddenBB.length() - 1);
        }
        buildBayesAnswer(queryString, hiddenBB, bbRemovalList, removalList, hiddenListOrder);
        Factor a = factorsList.get(0);
        if (hiddenList.isEmpty() != true) {
            a = getFactortoJoin(hiddenListOrder.get(0));
            if (factorsList.contains(a))
                factorsList.remove(a);
            else
                hiddenList.remove(a);
            for (;!hiddenListOrder.isEmpty();) {
                Factor factortoJoin = getFactortoJoin(hiddenListOrder.get(0));
                for (; factortoJoin != null;) {
                    a = joinhiddenList(a, factortoJoin);
                    Product_num += a.table.get(0).size() - 1;
                    if (hiddenList.contains(factortoJoin)){
                        hiddenList.remove(factortoJoin);
                    }else{
                        factorsList.remove(factortoJoin);
                    }
                    factortoJoin = getFactortoJoin(hiddenListOrder.get(0));
                }
                Factor factorAfterSum;
                if (a.table.size() == 2)
                    factorAfterSum = a;
                else {
                    factorAfterSum = sumAndEliminate(a, hiddenListOrder.get(0));
                }
                hiddenListOrder.remove(0);
                factorsList.add(factorAfterSum);
                if (!hiddenListOrder.isEmpty())
                    a = getFactortoJoin(hiddenListOrder.get(0));
            }
        }
        Collections.sort(factorsList);
        if (factorsList.size() == 1) {
            a = factorsList.get(0);
        } else {
            while (factorsList.size() > 1) {
                a = factorsList.remove(0);
                a = joinhiddenList(a, factorsList.get(0));
                Product_num += a.table.get(0).size() - 1;
                factorsList.remove(0);
                factorsList.add(0, a);
            }
        }
        num_sum += a.table.get(0).size() - 2;
        normalize(a);
        String finalValue = "";

        i = 1;
        while (i < a.table.get(0).size()) {
            if (type.equals(a.table.get(0).get(i)))
                finalValue = a.table.get(1).get(i);
            i++;
        }
        if (Product_num == 0) {
            num_sum = 0;
        }
        finalValue = String.format("%.5f", Double.valueOf(finalValue));
        finalValue += "," + String.valueOf(num_sum) + "," + String.valueOf(Product_num);
        return finalValue;
    }

    public String cleanseGarbage(String removeGarbage, Map<String, String> map, HashMap<myNode, Integer> bfsMap) {
        String cleanedGarbage = removeGarbage.substring(removeGarbage.indexOf("|") + 1);
        cleanedGarbage = cleanedGarbage.replaceAll("=", ",");
        String[] cleanQ = cleanedGarbage.split(",");
        int i = 0;
        while (i < cleanQ.length - 1) {
            map.put(cleanQ[i], cleanQ[i + 1]);
            i += 2;
        }
        for (String node : map.keySet()) {
            myNode currentNode = bayesNet.findNode(node);
            if (currentNode != null) {
                BFS.verifyAncestor(currentNode, bayesNet, bfsMap);
            }
        }
        return cleanedGarbage;
    }

    public String hiddenListAndOrder(String query) {
        String hiddenListString = "";
        if (query.length() - query.indexOf(')') + 1 > 0)
            hiddenListString = query.substring(query.indexOf(')') + 1);
        String orderForHiiden = "";
        if (query.substring(query.indexOf(')') + 1).length() > 0) {
            orderForHiiden = query.substring(query.indexOf(')') + 1);
            create_order(orderForHiiden);
        }
        return hiddenListString;
    }

    public Factor joinfactorsList(ArrayList<Factor> factorsList, int Product_num, Factor a) {
        if (factorsList.size() == 1)
            a = factorsList.get(0);
        for (int i = factorsList.size(); i > 1; i = factorsList.size()) {
            a = factorsList.get(0);
            factorsList.remove(0);
            a = joinhiddenList(a, factorsList.get(0));
            Product_num += a.table.get(0).size() - 1;
            factorsList.remove(0);
            factorsList.add(0, a);
        }
        return a;
    }

    public void buildBayesAnswer(String queryValue, String hiddenBB, ArrayList<String> hiddenListToRemoveFromBB,
            ArrayList<String> valuesToRemove, ArrayList<String> hiddenListOrder) {
        for (String s : valuesToRemove) {
            removeFromAllfactorsList(s);
        }
        for (String hiddenListBayesBall : hiddenListOrder) {
            String bb = hiddenListBayesBall + "-" + queryValue + "|" + hiddenBB;
            BayesBall bayes = new BayesBall(bayesNet, bb);
            if (bayes.BayesBallQuery().equals("yes"))
                hiddenListToRemoveFromBB.add(hiddenListBayesBall);
        }
        for (String s : hiddenListToRemoveFromBB) {
            removeFromAllfactorsList(s);
        }
        removeSingleValueFactors();
    }

    private double fetchDirectAnswer(HashMap<String, String> map, String s, String type) {
        myNode node = bayesNet.findNode(s);
        if (node.table.size() == 2)
            return -1;
        ArrayList<String> values = new ArrayList<>();
        int i = 0;
        while (i < node.table.size() - 1) {
            if (!node.table.get(i).get(0).equals(s)) {
                values.add(node.table.get(i).get(0));
            }
            i++;
        }
        ArrayList<String> arrayOfValuesToSearch = new ArrayList<>();
        arrayOfValuesToSearch.add(type);
        for (String key : values) {
            if (!map.containsKey(key))
                return -1;
            arrayOfValuesToSearch.add(map.get(key));
        }
        i = 1;
        while (i < node.table.get(0).size()) {
            int getIndex = 0;
            int j = 0;
            while (j < node.table.size() - 1) {
                if (!node.table.get(j).get(i).equals(arrayOfValuesToSearch.get(j)))
                    break;
                getIndex++;
                j++;
            }
            if (getIndex == node.table.size() - 1) {
                return Double.valueOf(node.table.get(node.table.size() - 1).get(i));
            }
            i++;
        }
        return -1;
    }

    private void create_order(String query) {
        List<String> splittedQuery = Arrays.asList(query.split("-"));
        hiddenListOrder.addAll(splittedQuery);
    }

    public Factor joinhiddenList(Factor a, Factor b) {
        ArrayList<String> aVariables = extractVariables(a);
        ArrayList<String> bVariables = extractVariables(b);
        boolean aNotInB = checkVariablesNotInList(aVariables, bVariables);
        boolean bayesNetotInA = checkVariablesNotInList(bVariables, aVariables);
        if ((aNotInB && bayesNetotInA)) {
            return join(a, b);
        }
        Factor newFactor = new Factor(a.name, b.name);
        ArrayList<String> indexes = new ArrayList<>();
        Factor smaller;
        Factor bigger;
        if (a.table.size() <= b.table.size()) {
            smaller = a;
            bigger = b;
        } else {
            smaller = b;
            bigger = a;
        }
        getIndexesNotContained(0, bigger, indexes, newFactor);

        ArrayList<String> temp = new ArrayList<>();
        temp.add("values");
        newFactor.table.add(temp);
        HashMap<String, Integer> indexesToCopy = new HashMap<>();
        creatable(bigger, smaller, newFactor);
        ArrayList<String> outcome = new ArrayList<>();

        addIndexesToCopy(0, smaller, indexesToCopy, outcome);
        copyIndexesToNewFactor(0, bigger, indexesToCopy);
        int i = 1;
        while (i < smaller.table.get(0).size()) {
            HashMap<String, String> mergeValues = new HashMap<>();
            String prob = smaller.table.get(smaller.table.size() - 1).get(i);
            int j = 0;
            while (j < smaller.table.size() - 1) {
                mergeValues.put(smaller.table.get(j).get(0), smaller.table.get(j).get(i));
                j++;
            }
            int k = 1;
            while (k < bigger.table.get(0).size()) {
                int sum = 0;
                int m = 0;
                while (m < mergeValues.size()) {
                    if (!bigger.table.get(indexesToCopy.get(outcome.get(m))).get(k)
                            .equals(mergeValues.get(outcome.get(m)))) {
                        break;
                    }
                    sum++;
                    m++;
                }
                if (sum == outcome.size()) {
                    String prob2 = bigger.table.get(bigger.table.size() - 1).get(k);
                    String result = String.valueOf(Double.valueOf(prob2) * Double.valueOf(prob));
                    newFactor.table.get(newFactor.table.size() - 1).remove(k);
                    newFactor.table.get(newFactor.table.size() - 1).add(k, result);
                }
                k++;
            }
            i++;
        }

        return newFactor;
    }

    public void getIndexesNotContained(int index, Factor bigger, ArrayList<String> indexes, Factor newFactor) {
        int i = index;
        while (i < bigger.table.size() - 1) {
            if (!indexes.contains(bigger.table.get(i).get(0))) {
                ArrayList<String> temp = new ArrayList<>();
                temp.add(bigger.table.get(i).get(0));
                newFactor.table.add(temp);
            }
            i++;
        }
    }

    public void addIndexesToCopy(int index, Factor smaller, HashMap<String, Integer> indexesToCopy,
            ArrayList<String> outcome) {
        int i = index;
        while (i < smaller.table.size() - 1) {
            indexesToCopy.put(smaller.table.get(i).get(0), i);// taking indexes from smaller
            outcome.add(smaller.table.get(i).get(0));
            i++;
        }
    }

    public void copyIndexesToNewFactor(int index, Factor bigger, HashMap<String, Integer> indexesToCopy) {
        int i = index;
        while (i < bigger.table.size() - 1) {
            if (indexesToCopy.containsKey(bigger.table.get(i).get(0)))// changing the indexes to bigger
                indexesToCopy.put(bigger.table.get(i).get(0), i);
            i++;
        }
    }

    public void creatable(Factor a, Factor b, Factor newfactor) {
        if (a.table.get(0).size() >= b.table.get(0).size()) {
            int i = 0;
            while (i < newfactor.table.size()) {
                int j = 1;
                while (j < a.table.get(i).size()) {
                    newfactor.table.get(i).add(a.table.get(i).get(j));
                    j++;
                }
                i++;
            }
        } else {
            int i = 0;
            while (i < newfactor.table.size() - 1) {
                int j = 0;
                while (j < b.table.get(i).size()) {
                    newfactor.table.get(i).add(b.table.get(i).get(j));
                    j++;
                }
                i++;
            }
        }
    }

    public Factor sumAndEliminate(Factor a, String key) {
        int modulo = 0;
        ArrayList<String> newValues = extractRemainingVariables(a, key);
        Factor b = new Factor(a.name, "");
        int tableSize = calculateulateTableSize(newValues);
        ArrayList<String> firstValues = new ArrayList<>();
        firstValues.add(newValues.get(0));
        String[] firstValueArr = bayesNet.findNode(newValues.get(0)).getOutcome();

        int i = 0;
        while (i < tableSize) {
            firstValues.add(firstValueArr[modulo]);
            modulo = (modulo + 1) % firstValueArr.length;
            i++;
        }
        modulo = 0;
        ArrayList<ArrayList<String>> newTable = new ArrayList<>();
        newTable.add(firstValues);
        newValues.remove(0);
        int sumForEveryVar = firstValueArr.length;
        for (; !newValues.isEmpty();) {
            myNode currTableNode = bayesNet.findNode(newValues.get(0));
            ArrayList<String> valuesList = new ArrayList<>();
            newValues.remove(0);
            valuesList.add(currTableNode.name);
            int j = 0;
            String[] currTableNodeOutcome = currTableNode.getOutcome();
            while (j < tableSize) {
                int k = 0;
                while (k < sumForEveryVar) {
                    valuesList.add(currTableNodeOutcome[modulo]);
                    k++;
                }
                modulo = (modulo + 1) % currTableNodeOutcome.length;
                j += sumForEveryVar;
            }
            sumForEveryVar = sumForEveryVar * currTableNodeOutcome.length;
            newTable.add(valuesList);
        }
        b.table = newTable;
        String[] trueAndFalse = new String[b.table.size()];
        String[] valuesFromOldTable = new String[b.table.size()];
        double sum = 0;
        ArrayList<String> probabilty = new ArrayList<>();
        probabilty.add("values");

        i = 1;
        while (i < b.table.get(0).size()) {
            int j = 0;
            while (j < b.table.size()) {
                trueAndFalse[j] = b.table.get(j).get(i);
                j++;
            }
            sum = 0;
            j = 1;
            while (j < a.table.get(0).size()) {
                int index = 0;
                int k = 0;
                while (k < a.table.size() - 1) {
                    if (a.table.get(k).get(0).equals(key)) {
                        k++;
                        continue;
                    }
                    valuesFromOldTable[index++] = a.table.get(k).get(j);
                    k++;
                }
                if (Arrays.deepEquals(trueAndFalse, valuesFromOldTable)) {
                    sum = sum + Double.valueOf(a.table.get(a.table.size() - 1).get(j));
                }
                j++;
            }
            probabilty.add(String.valueOf(sum));
            i++;
        }
        num_sum = num_sum + (bayesNet.findNode(key).getOutcome().length - 1) * (b.table.get(0).size() - 1);
        newTable.add(probabilty);
        return b;
    }

    private int calculateulateTableSize(ArrayList<String> variables) {
        int size = 1;
        for (String variable : variables) {
            size *= bayesNet.findNode(variable).getOutcome().length;
        }
        return size;
    }

    private ArrayList<String> extractRemainingVariables(Factor factor, String key) {
        ArrayList<String> remainingVariables = new ArrayList<>();
        int tableSize = factor.table.size();
        int index = 0;
        while (index < tableSize - 1) {
            if (!factor.table.get(index).get(0).equals(key)) {
                remainingVariables.add(factor.table.get(index).get(0));
            }
            index++;
        }
        return remainingVariables;
    }

    private ArrayList<String> extractVariables(Factor factor) {
        ArrayList<String> variables = new ArrayList<>();
        int tableSize = factor.table.size();
        int index = 0;
        while (index < tableSize - 1) {
            variables.add(factor.table.get(index).get(0));
            index++;
        }
        return variables;
    }

    private boolean checkVariablesNotInList(ArrayList<String> listA, ArrayList<String> listB) {
        boolean flag = false;
        int index = 0;
        while (index < listA.size()) {
            String key = listA.get(index);
            if (!listB.contains(key)) {
                flag = true;
                break;
            }
            index++;
        }
        return flag;
    }

    public void addVariablesToTable(Factor a, Factor b, ArrayList<String> doNotRepeat,
            ArrayList<ArrayList<String>> table) {
        int i = 0;
        while (i < a.table.size() - 1) {
            ArrayList<String> temp = new ArrayList<>();
            temp.add(a.table.get(i).get(0));
            doNotRepeat.add(a.table.get(i).get(0));
            table.add(temp);
            i++;
        }
        i = 0;
        while (i < b.table.size() - 1) {
            if (doNotRepeat.contains(b.table.get(i).get(0))) {
                i++;
                continue;
            }
            ArrayList<String> temp = new ArrayList<>();
            temp.add(b.table.get(i).get(0));
            table.add(temp);
            i++;
        }
    }

    public void insertOutcome(int tableSize, ArrayList<ArrayList<String>> table, int sumForEveryVar, int modulu,
            String[] outcome) {
        int i = 0;
        while (i < tableSize) {
            table.get(0).add(outcome[modulu]);
            modulu = (modulu + 1) % outcome.length;
            i++;
        }
        i = 1;
        while (i < table.size() - 1) {
            String[] curroutcome = bayesNet.findNode(table.get(i).get(0)).getOutcome();
            int moduleIndex = 0;
            int j = 0;
            while (j < tableSize) {
                int k = 0;
                while (k < sumForEveryVar) {
                    table.get(i).add(curroutcome[moduleIndex]);
                    k++;
                }
                moduleIndex = (moduleIndex + 1) % curroutcome.length;
                j += sumForEveryVar;
            }
            sumForEveryVar = sumForEveryVar * curroutcome.length;
            i++;
        }
    }

    public int getTableSize(ArrayList<ArrayList<String>> table) {
        int tableSize = 1;
        for (int i = 0; i < table.size(); i++) {
            tableSize *= bayesNet.findNode(table.get(i).get(0)).getOutcome().length;
        }
        return tableSize;
    }

    public Factor join(Factor a, Factor b) {
        ArrayList<String> doNotRepeat = new ArrayList<>();
        ArrayList<ArrayList<String>> table = new ArrayList<>();
        // add the variables to the new table
        addVariablesToTable(a, b, doNotRepeat, table);
        ArrayList<String> values = new ArrayList<>();
        values.add("values");
        int tableSize = getTableSize(table);
        int i = 0;
        while (i < tableSize) {
            values.add("0");
            i++;
        }
        table.add(values);
        int sumForEveryVar = bayesNet.findNode(table.get(0).get(0)).getOutcome().length;
        int modulu = 0;
        String[] outcome = bayesNet.findNode(table.get(0).get(0)).getOutcome();
        insertOutcome(tableSize, table, sumForEveryVar, modulu, outcome);
        ArrayList<Integer> indexesForA = new ArrayList<>();
        ArrayList<Integer> indexesForAInAtable = new ArrayList<>();
        i = 0;
        while (i < a.table.size() - 1) {
            String var = a.table.get(i).get(0);
            int j = 0;
            while (j < table.size() - 1) {
                String tableVar = table.get(j).get(0);
                if (var.equals(tableVar)) {
                    indexesForA.add(j);
                    indexesForAInAtable.add(i);
                    break;
                }
                j++;
            }
            i++;
        }
        Factor newFactor = new Factor(a.name, b.name);
        calculateulateJoin(a, b, newFactor, table, tableSize, indexesForA, indexesForAInAtable);
        newFactor.table = table;
        return newFactor;
    }

    public void calculateulateJoin(Factor a, Factor b, Factor newFactor, ArrayList<ArrayList<String>> table,
            int tableSize,
            ArrayList<Integer> indexesForA, ArrayList<Integer> indexesForAInAtable) {
        int i = 1;
        while (i < a.table.get(0).size()) {
            ArrayList<String> valuesToFill = new ArrayList<>();
            int j = 0;
            while (j < indexesForAInAtable.size()) {
                valuesToFill.add(a.table.get(indexesForAInAtable.get(j)).get(i));
                j++;
            }
            int m = 1;
            while (m < tableSize + 1) {
                int Asize = 0;
                int k = 0;
                while (k < indexesForA.size()) {
                    if (valuesToFill.get(k).equals(table.get(indexesForA.get(k)).get(m)))
                        Asize++;
                    else
                        break;
                    k++;
                }
                if (Asize == indexesForA.size()) {
                    table.get(table.size() - 1).remove(m);
                    table.get(table.size() - 1).add(m, a.table.get(a.table.size() - 1).get(i));
                }
                m++;
            }
            i++;
        }
        ArrayList<Integer> indexesForB = new ArrayList<>();
        i = 0;
        while (i < b.table.size() - 1) {
            String var = b.table.get(i).get(0);
            int j = 0;
            while (j < table.size() - 1) {
                String tableVar = table.get(j).get(0);
                if (var.equals(tableVar)) {
                    indexesForB.add(j);
                    break;
                }
                j++;
            }
            i++;
        }
        i = 1;
        while (i < b.table.get(0).size()) {
            ArrayList<String> valuesToFill = new ArrayList<>();
            int j = 0;
            while (j < b.table.size() - 1) {
                valuesToFill.add(b.table.get(j).get(i));
                j++;
            }
            int m = 1;
            while (m < tableSize + 1) {
                int Bsize = 0;
                int k = 0;
                while (k < indexesForB.size()) {
                    if (valuesToFill.get(k).equals(table.get(indexesForB.get(k)).get(m)))
                        Bsize++;
                    else
                        break;
                    k++;
                }
                if (Bsize == indexesForB.size()) {
                    double x = Double.valueOf(table.get(table.size() - 1).get(m));
                    double y = Double.valueOf(b.table.get(b.table.size() - 1).get(i));
                    table.get(table.size() - 1).remove(m);
                    table.get(table.size() - 1).add(m, String.valueOf(x * y));
                }
                m++;
            }
            i++;
        }
    }

    public Factor getFactortoJoin(String f) {
        List<Factor> list = new ArrayList<>();
        for (Factor factor : factorsList) {
            int i = 0;
            while (i < factor.table.size() - 1) {
                if (f.equals(factor.table.get(i).get(0)))
                    list.add(factor);
                i++;
            }
        }
        for (Factor factor : hiddenList) {
            int i = 0;
            while (i < factor.table.size() - 1) {
                if (f.equals(factor.table.get(i).get(0)))
                    list.add(factor);
                i++;
            }
        }
        Collections.sort(list);
        if (list.size() == 0)
            return null;
        if (hiddenList.contains(list.get(0)))
            hiddenList.remove(list.get(0));
        else
            factorsList.remove(list.get(0));
        return list.get(0);
    }

    private void normalize(Factor factor) {
        double sum = 0;
        ArrayList<String> lastRow = factor.table.get(factor.table.size() - 1);
        for (int i = 1; i < lastRow.size(); i++) {
            sum += Double.parseDouble(lastRow.get(i));
        }
        ArrayList<String> normalizedValues = new ArrayList<>();
        normalizedValues.add("values");
        for (int i = 1; i < lastRow.size(); i++) {
            double normalizedValue = Double.parseDouble(lastRow.get(i)) / sum;
            normalizedValues.add(String.valueOf(normalizedValue));
        }
        factor.table.remove(factor.table.size() - 1);
        factor.table.add(normalizedValues);
    }

    public void updateHiddenList(String s) {
        if (s.isEmpty()) {
            return;
        }
        String[] splittedhiddenList = s.split("-");
        Iterator<String> varIterator = Arrays.asList(splittedhiddenList).iterator();
        while (varIterator.hasNext()) {
            String var = varIterator.next();
            Iterator<Factor> factorsListIterator = factorsList.iterator();
            while (factorsListIterator.hasNext()) {
                Factor factor = factorsListIterator.next();
                if (var.equals(factor.name)) {
                    hiddenList.add(factor);
                    factorsListIterator.remove();
                }
            }
        }
    }

    private void removeSingleValueFactors() {
        Iterator<Factor> factorsListIterator = factorsList.iterator();
        while (factorsListIterator.hasNext()) {
            Factor factor = factorsListIterator.next();
            if (factor.table.size() == 1) {
                factorsListIterator.remove();
            }
        }
    }

    private void removeFromAllfactorsList(String s) {
        int i = 0;
        while (i < hiddenList.size()) {
            Factor factor = hiddenList.get(i);
            int j = 0;
            while (j < factor.table.size() - 1) {
                if (factor.table.get(j).get(0).equals(s)) {
                    hiddenListOrder.remove(factor.name);
                    hiddenList.remove(i);
                    break;
                }
                j++;
            }
            i++;
        }
        int k = 0;
        while (k < factorsList.size()) {
            Factor factor = factorsList.get(k);
            int j = 0;
            while (j < factor.table.size() - 1) {
                if (factor.table.get(j).get(0).equals(s)) {
                    factorsList.remove(k);
                    break;
                }
                j++;
            }
            k++;
        }
    }
}
