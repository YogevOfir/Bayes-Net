import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Scanner;
import java.util.LinkedList;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Ex1 {
    public static void main(String[] args) {
        try {
            // Initialize the file and scanner
            File file = new File("input.txt");
            Scanner sc = new Scanner(file);

            // Create lists to store the parsed data
            LinkedList<Integer> list = new LinkedList<>();
            LinkedList<String> queryList = new LinkedList<>();

            // Read the first line to get the XML name
            String xmlName = sc.nextLine();
            System.out.println("XML Name: " + xmlName); // Debugging statement

            // Read and process the rest of the file
            while (sc.hasNext()) {
                String line = sc.nextLine();
                queryList.add(line);
                if (line.charAt(0) == 'P' && line.charAt(1) == '(') {
                    list.add(1); // Add 1 for eliminate variable queries
                } else {
                    list.add(0); // Add 0 for bayes ball queries
                }
            }
            sc.close(); // Close the scanner
            int index = 0;

            // Ensure queryList has entries before proceeding
            if (queryList.isEmpty()) {
                System.out.println("Error: No queries found in the input file.");
                return;
            }

            try {
                // Initialize FileWriter and BufferedWriter for output
                FileWriter fWriter = new FileWriter("output.txt");
                BufferedWriter bufferedWriter = new BufferedWriter(fWriter);
                // Process each query in the list
                for (Integer key : list) {
                    // Initialize the Bayesian network
                    BayesianNetwork bayesNet = new BayesianNetwork(xmlName);
                    bayesNet.networkBuild();
                    // Ensure index is within bounds of queryList
                    if (index >= queryList.size()) {
                        System.out.println(
                                "Error: Index " + index + " out of bounds for queryList size " + queryList.size());
                        break;
                    }
                    if (key == 0) {
                        // Process bayes ball queries
                        BayesBall bb = new BayesBall(bayesNet, queryList.get(index));
                        String result = bb.BayesBallQuery();
                        bufferedWriter.write(result);
                        bufferedWriter.newLine();
                    } else {
                        // Process eliminate variable queries
                        VE ve = new VE(bayesNet);
                        String result = ve.calculate(queryList.get(index));
                        bufferedWriter.write(result);
                        bufferedWriter.newLine();
                    }
                    index++; // Move to the next query
                }
                // Close the BufferedWriter
                bufferedWriter.close();
            } catch (IOException e) {
                System.out.println("Error writing to output file: " + e.getMessage());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found - " + e.getMessage());
        }
    }
}
