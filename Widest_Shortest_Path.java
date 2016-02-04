
import java.io.*;
import java.util.*;
import java.lang.Math;
 
public class Widest_Shortest_Path
{
    private int          distances[];
    private int          width[];
    private Set<Integer> settled;
    private Set<Integer> unsettled;
    private int          number_of_nodes;
    private int          adjacencyMatrix[][];
    private int          adjacencyMatrix2[][];
    private int 		 prev[];
 
    public Widest_Shortest_Path(int number_of_nodes)
    {
        this.number_of_nodes = number_of_nodes;
        distances = new int[number_of_nodes + 1];
        width = new int[number_of_nodes + 1];
        prev = new int[number_of_nodes + 1];
        settled = new HashSet<Integer>();
        unsettled = new HashSet<Integer>();
        adjacencyMatrix = new int[number_of_nodes + 1][number_of_nodes + 1];
        adjacencyMatrix2 = new int[number_of_nodes + 1][number_of_nodes + 1];
    }
 
    public void widest_algorithm(int adjacency_matrix[][], int adjacency_matrix2[][], int source)
    {
        int evaluationNode;
        for (int i = 1; i <= number_of_nodes; i++) 
            for (int j = 1; j <= number_of_nodes; j++) {
                adjacencyMatrix[i][j] = adjacency_matrix[i][j];
                adjacencyMatrix2[i][j] = adjacency_matrix2[i][j];
            }
                
 
        for (int i = 1; i <= number_of_nodes; i++)
        {
        	prev[i] = 0;
            width[i] = Integer.MIN_VALUE;
        }
 
        unsettled.add(source);
        width[source] = Integer.MAX_VALUE;
        distances[source] = 0;
        prev[source] = 0;
        while (!unsettled.isEmpty())
        {
            evaluationNode = getNodeWithMaximumWidthFromUnsettled();
            unsettled.remove(evaluationNode);
            settled.add(evaluationNode);
            //System.out.println("node " + evaluationNode + " is added to settled");
            evaluateNeighbours(evaluationNode);
        }
    }
 
    private int getNodeWithMaximumWidthFromUnsettled()
    {
        int max;
        int node = 0;
 
        Iterator<Integer> iterator = unsettled.iterator();
        node = iterator.next();
        max = width[node];
        for (int i = 1; i <= distances.length; i++)
        {
            if (unsettled.contains(i))
            {
                if (width[i] >  max)
                {
                    max = width[i];
                    node = i;
                    //System.out.println("max = " + max + " node number = " + node);
                }
            }
        }
        return node;
    }
 
    private void evaluateNeighbours(int evaluationNode)
    {
        int edgeWidth = -1;
        int newWidth = -1;
        int edgeDistance = -1;
        int newDistance = -1;
 
        for (int destinationNode = 1; destinationNode <= number_of_nodes; destinationNode++)
        {
            if (!settled.contains(destinationNode))
            {
                if (adjacencyMatrix[evaluationNode][destinationNode] != Integer.MAX_VALUE)
                {
                    edgeWidth = adjacencyMatrix[evaluationNode][destinationNode];
                    newWidth = Math.min(edgeWidth, width[evaluationNode]);
                    edgeDistance = adjacencyMatrix2[evaluationNode][destinationNode];
                    newDistance = distances[evaluationNode] + edgeDistance;
                     
                    if (newWidth > width[destinationNode]) {
                        width[destinationNode] = newWidth; 
                        distances[destinationNode] = newDistance;
                        prev[destinationNode] = evaluationNode;
                    }

                    if (newWidth == width[destinationNode]) {
                        distances[destinationNode] = Math.min(newDistance, distances[destinationNode]);
                        if (newDistance < distances[destinationNode]) {
                        	prev[destinationNode] = evaluationNode;
                        }
                    }
                    // System.out.println("width[" + destinationNode + "] = " + width[destinationNode]);
                    // System.out.println("distances[" + destinationNode + "] = " + distances[destinationNode]);
                    
                    unsettled.add(destinationNode);
                    // System.out.println("node " + destinationNode + " is added to unsettled");
                }
            }
        }
    }
 
    public ArrayList<ArrayList<Integer>> run (int adjacency_matrix[][], int adjacency_matrix2[][], int number_of_nodes, int source)
    {

        ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
        // try {
        //     BufferedReader in = new BufferedReader(new FileReader("topo_config.txt"));
        //     String line;
        //     while((line = in.readLine()) != null)
        //     {
        //         ArrayList<Integer> temp = new ArrayList<Integer>();
        //         for (String element : line.split(" ")) {
        //             temp.add(Integer.parseInt(element));
        //         }
        //         result.add(temp);
        //     }
        //     in.close();
        // } catch (IOException e) {
            
        // }

        // int adjacency_matrix[][];
        // int adjacency_matrix2[][];
        // int number_of_vertices;
        // int source = 0, destination = 0;
        // Scanner scan = new Scanner(System.in);
        try
        {
            //System.out.println("The number of vertices is " + result.get(0).get(0));
            //number_of_vertices = result.get(0).get(0);
            // adjacency_matrix = new int[number_of_vertices + 1][number_of_vertices + 1];
            // adjacency_matrix2 = new int[number_of_vertices + 1][number_of_vertices + 1];
 
            // for (int i = 1; i < result.size(); i++) {
            //     adjacency_matrix[result.get(i).get(0)][result.get(i).get(1)] = result.get(i).get(2);
            //     adjacency_matrix[result.get(i).get(1)][result.get(i).get(0)] = result.get(i).get(2); 
            //     adjacency_matrix2[result.get(i).get(0)][result.get(i).get(1)] = result.get(i).get(3);
            //     adjacency_matrix2[result.get(i).get(1)][result.get(i).get(0)] = result.get(i).get(3); 
            // }


            for (int i = 1; i <= number_of_vertices; i++) {
                for (int j = 1; j <= number_of_vertices; j++) {
                    if (adjacency_matrix[i][j] == 0) {
                        adjacency_matrix[i][j] = Integer.MAX_VALUE;
                    }
                    if (adjacency_matrix2[i][j] == 0) {
                        adjacency_matrix2[i][j] = Integer.MAX_VALUE;
                    }
                    //System.out.print(adjacency_matrix2[i][j] + " ");

                }
                //System.out.println();
            }
 
            // System.out.println("Enter the source ");
            // source = scan.nextInt();
 
            // System.out.println("Enter the destination ");
            // destination = scan.nextInt();
 
            Widest_Shortest_Path widestAlgorithm = new Widest_Shortest_Path(
                    number_of_nodes);
            widestAlgorithm.widest_algorithm(adjacency_matrix, adjacency_matrix2, source);
 
            // System.out.println("The Widest Path from " + source + " to " + destination + " is: ");
            // for (int i = 1; i <= widestAlgorithm.width.length - 1; i++)
            // {
            //     if (i == destination) {
            //         System.out.println("The widest from " + source + " to " + i + " is "
            //                 + widestAlgorithm.width[i]);
            //         System.out.println("The shortest form " + source + " to " + i + " is "
            //                 + widestAlgorithm.distances[i]);
            //         System.out.println("The previous node of the destinationNode is " + widestAlgorithm.prev[i]);


            //     }
                    

            // }
        } catch (InputMismatchException inputMismatch)
        {
            System.out.println("Wrong Input Format");
        }
        //scan.close();
        result.add(widestAlgorithm.width);
        result.add(widestAlgorithm.distances);
        result.add(widestAlgorithm.prev);
        return result;
    }
}