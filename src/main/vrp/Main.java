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

        Solution initsol = getInitialSoln();
        // Use initsol to develop the further solutions.
        sc.close();
    }

    public static Solution getInitialSoln() {
        // Finds the initial solution and places it in the static variable route.
        Solution initial = new Solution();
        
        // Assign each customer to the nearest carpark.
        for(int i = 0; i < numCustomers; i++) {
            int minDistance = -1;
            int assigned = 0;
            for(int j = 1; j <= numCarpark; j++) {
                if (minDistance > nodesDistance[i][j] || minDistance == -1) {
                    minDistance = nodesDistance[i][j];
                    assigned = j;
                }
            }
            customers[i].assignedPark = assigned; // Customer at index i in customers array is assigned the carpark at index 'assigned' in nodes numbering 
            carparks[assigned-1].addCustomer(customers[i]); // The carpark also holds the customer now.
        }

        // Now apply Clarke and Wright's Savings Algorithm for the first level

        return initial;
    }

    public static Vector<Route> savingSolution(Vector<Customer> customers, int depot) {
        Vector<Route> routes = new Vector<Route>();
        Vector<Route> list = new Vector<Route>();
        for(int i = 0; i < customers.size()-1; i++) {
            Route single = new Route();
            single.addCustomer(depot);
            single.addCustomer(customers.elementAt(i).id);
            single.addCustomer(depot);        
            routes.add(single); // For adding the single customer routes
            for(int j = i+1; j < customers.size(); j++) {
                Route r = new Route();
                r.addCustomer(depot);
                r.addCustomer(customers.elementAt(i).id);
                r.addCustomer(customers.elementAt(j).id);
                r.addCustomer(depot);
                list.add(r);
            }
        }
        list.sort(new Comparator<Route>() {
            @Override
            public int compare(Route o1, Route o2) {
                int firsto1 = o1.route.elementAt(1);
                int firsto2 = o2.route.elementAt(1);
                int secondo1 = o1.route.elementAt(2);
                int secondo2 = o2.route.elementAt(2);
                int savings1 = nodesDistance[depot][firsto1] + nodesDistance[depot][secondo1] - nodesDistance[firsto1][secondo1];
                int savings2 = nodesDistance[depot][firsto2] + nodesDistance[depot][secondo2] - nodesDistance[firsto2][secondo2];
                if (savings1 > savings2 ) {
                    return 1;
                } else if (savings1 == savings2 && o1.demand < o2.demand) {
                    return 1;
                }
                return -1;
            }
        });
        // Now we have the sorted list, arranged in descending order as per the savings
        for(int i = 0; i < list.size(); i++) {
            Route bestSavings = list.elementAt(i); // This is the yet best merge for two delivery locations
            for(int j = 0; j < routes.size(); j++) {
                Route currRoute = routes.elementAt(j);
                
                if(currRoute.route.firstElement() == bestSavings.route.firstElement())
            }
        } 
        return routes;
    }
}
