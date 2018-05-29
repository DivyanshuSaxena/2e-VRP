// File to take input from a text file, and run the algorithm
package vrp;
import java.util.*;
import java.io.*;

class Main {
    static int numCustomers, numNodes, numCarpark;
    static int numVehicles1, numVehicles2;
    static int nodesDistance[][];
    static Customer customers[];
    static Carpark carparks[];
    // route only contains the index of each carpark that is visited by the first level vehicle
    static Vector<Integer> route = new Vector<Integer>();

    public static void main(String args[]) throws IOException {
        // The Method reads the inputs from a file and initializes the data structures
        String filename = args[0];
        File file = new File(filename);
        Scanner sc = new Scanner(file);

        // Input parameters form the input file
        sc.nextLine(); // Name
        sc.nextLine(); // Comment
        sc.nextLine(); // Type
        numNodes = Integer.parseInt(sc.nextLine().split(" ")[2]); //Dimension 
        numCarpark = Integer.parseInt(sc.nextLine().split(" ")[2]); // Satellites
        numCustomers = Integer.parseInt(sc.nextLine().split(" ")[2]); // Customers

        sc.nextLine(); // Edge Weight Type
        sc.nextLine(); // L1Cap
        sc.nextLine(); // L1Cap
        sc.nextLine(); // L2Cap

        numVehicles1 = Integer.parseInt(sc.nextLine().split(" ")[1]); // L1 Fleet
        numVehicles2 = Integer.parseInt(sc.nextLine().split(" ")[1]); // L2 Fleet
        
        sc.nextLine(); // Edge Weight Section
        nodesDistance = new int[numNodes][numNodes];
        customers = new Customer[numCustomers];
        carparks = new Carpark[numCarpark];

        // Creation of distances matrix from the input file
        for (int i = 0; i < numNodes; i++) {
            for (int j = i; j < numNodes; j++) {
                nodesDistance[i][j] = sc.nextInt();
                nodesDistance[j][i] = nodesDistance[i][j];
            }
        }

        // Initialization of the customer array 
        sc.nextLine(); // Demand Section
        for(int i = 1; i < numNodes; i++) {
            // i = 0 is reserved for the main depot
            if (i <= numCarpark) {
                carparks[i] = new Carpark();
                carparks[i].setId(i);
            } else {
                customers[i] = new Customer();
                customers[i].setId(i);
                int demand = Integer.parseInt(sc.nextLine().split(" ")[1]);
                customers[i].setDemand(demand);            
            }
        }

        getInitialSoln();
        sc.close();
    }

    public static void getInitialSoln() {
        // Finds the initial solution and places it in the static variable route.
    }
}