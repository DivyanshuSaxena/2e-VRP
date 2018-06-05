// File to take input from a text file, and run the algorithm
package vrp;
import java.util.*;
import java.io.*;

class Main {
    static int numCustomers, numNodes, numCarpark;
    static int numVehicles1, numVehicles2;
    static int l1cap, l2cap;
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
        sc.nextLine(); // Fleet Section
        l1cap = Integer.parseInt(sc.nextLine().split(" ")[2]); // L1 Cap
        l2cap = Integer.parseInt(sc.nextLine().split(" ")[2]); // L2 Cap

        numVehicles1 = Integer.parseInt(sc.nextLine().split(" ")[1]); // L1 Fleet
        numVehicles2 = Integer.parseInt(sc.nextLine().split(" ")[1]); // L2 Fleet
        
        sc.nextLine(); // Edge Weight Section
        nodesDistance = new int[numNodes][numNodes];
        customers = new Customer[numCustomers];
        carparks = new Carpark[numCarpark];

        // Creation of distances matrix from the input file
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                nodesDistance[i][j] = sc.nextInt();
            }
        }

        // Empty Lines
        sc.nextLine();
        sc.nextLine();
        
        // Initialization of the customer array 
        sc.nextLine(); // Demand Section
        sc.nextLine(); // Demand of Main Depot	
        for(int i = 1; i < numNodes; i++) {
            // i = 0 is reserved for the main depot
            if (i <= numCarpark) {
            	sc.nextLine(); // Demand of ith carpark 
                carparks[i-1] = new Carpark();
                carparks[i-1].setId(i);
            } else {
                int offset = numCarpark + 1;
                customers[i-offset] = new Customer();
                customers[i-offset].setId(i);
                int demand = Integer.parseInt(sc.nextLine().split(" ")[1]);
                customers[i-offset].setDemand(demand);            
            }
        }

        Solution initsol = getInitialSoln();
        // Use initsol to develop the further solutions here.
        System.out.println(initsol);
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

        // Add the required Carparks to the initial solution
        Vector<Integer> carparksLevel1 = new Vector<Integer>();
        for (Carpark cp : carparks) {
            if (cp.customers.size() > 0) {
                // Apply Clarke and Wright's Savings Algorithm for the second level routes (carparks)
                Vector<Integer> carparkCustomers = new Vector<Integer>();
                for (Customer customer : cp.customers) {
                    carparkCustomers.add(customer.id); // To get the vector of indices of customers assigned to the carpark.
                }
                cp.routes = savingSolution(carparkCustomers, cp.id, l2cap);
                initial.firstLevel.add(cp);
                carparksLevel1.add(cp.id);
            }
        }

        // Apply Clarke and Wright's Savings Algorithm for the first level
        initial.routes = savingSolution(carparksLevel1, 0, l1cap);
        return initial;
    }

    public static Vector<Route> savingSolution(Vector<Integer> customers, final int depot, int capacity) {
        // This function implements the Clarke and Wright's Saving Algortihm
    	// System.out.println("Applying C&W for " + depot + " with customers: " + customers.size()); // Debug
        Vector<Route> routes = new Vector<Route>();
        Vector<Route> savingsList = new Vector<Route>(); //  To hold all the two location routes
        for(int i = 0; i < customers.size()-1; i++) {
        	// Single routes shall be added after all other routes have been added
            for(int j = i+1; j < customers.size(); j++) {
                Route r = new Route();
                r.addCustomer(depot);
                r.addCustomer(customers.elementAt(i));
                r.addCustomer(customers.elementAt(j));
                r.addCustomer(depot);
                savingsList.add(r);
            }
        }
        savingsList.sort(new Comparator<Route>() {
            @Override
            public int compare(Route o1, Route o2) {
                int firsto1 = o1.route.elementAt(1);
                int firsto2 = o2.route.elementAt(1);
                int secondo1 = o1.route.elementAt(2);
                int secondo2 = o2.route.elementAt(2);
                int savings1 = nodesDistance[depot][firsto1] + nodesDistance[depot][secondo1] - nodesDistance[firsto1][secondo1];
                int savings2 = nodesDistance[depot][firsto2] + nodesDistance[depot][secondo2] - nodesDistance[firsto2][secondo2];
                if (savings1 > savings2 ) {
                    return -1;
                } else if (savings1 == savings2 && o1.demand < o2.demand) {
                    return -1;
                }
                return 1;
            }
        });
        // Now we have the sorted list, arranged in descending order as per the savings
        for(int i = 0; i < savingsList.size(); i++) {
            Route bestSavings = savingsList.elementAt(i); // This is the yet best merge for two delivery locations
            // System.out.println("Best Savings Route: " + bestSavings); // Debug
            int bestStart = bestSavings.route.elementAt(1);
            int bestEnd = bestSavings.route.elementAt(2);
            if (routes.size() == 0) {
            	routes.add(bestSavings);
            	continue;
            }
            Route newRoute = new Route();
            // routeIndex keeps track of the index at which the new route is to kept and mergedIndex removes the merged route.
            int routeIndex = -1, mergedIndex = -1;
            // added keeps track whether the bestSavings Route was added in any route or not. discard shows if the route is to be discarded
            boolean added = false, discard = false;
            for(int j = 0; j < routes.size(); j++) {
                // routes contains all routes, and bestsavings may be merged with any of them.
                Route currRoute = routes.elementAt(j);
                // System.out.println(j + " " + currRoute); // Debug
                int positionOfStart = currRoute.positionOf(bestStart);
                int positionOfEnd = currRoute.positionOf(bestEnd);
                // Now check if the positions are valid for merging or not
                if (positionOfStart >= 0 || positionOfEnd >= 0) {
                	routeIndex = -1;
                	discard = true;
                    break;                    
                } else if ((positionOfStart == -2 && positionOfEnd == -1) || (positionOfStart == -1 && positionOfEnd == -2)) {
                	// Remove the demand of the common customer
                	int commonDemand = ((positionOfStart == -2)? Main.customers[bestStart-numCarpark-1].demand : Main.customers[bestEnd-numCarpark-1].demand);
                	if (newRoute.route.size() == 0) {
                		if ((bestSavings.demand + currRoute.demand - commonDemand) <= capacity) {
                			added = true;
                			discard = false;
                			newRoute = currRoute.mergeRoute(bestSavings);
                        	routeIndex = j;
                		} else {
                			discard = true;
                			break;
                		}
                    } else if (newRoute.route.size() > 0) {
                    	if ((newRoute.demand + currRoute.demand - commonDemand) <= capacity) {
                    		added = true;
                			discard = false;
                    		newRoute = newRoute.mergeRoute(currRoute);
                    		mergedIndex = j;
                    	} else break;
                    }
                } else if (positionOfStart == -2 || positionOfEnd == -2) {
                	routeIndex = -1;
                	discard = true;
                    break;
                }
            }
            if (routeIndex != -1)  {
            	// System.out.println("New Route: " + newRoute); // Debug
            	routes.set(routeIndex, newRoute);
            }
            if (mergedIndex != -1)  routes.remove(mergedIndex);
            if (!added && !discard)	{
            	// System.out.println("No suitable route found, adding: " + bestSavings); // Debug
            	routes.add(bestSavings);
            }
        }
        // All routes in savings list that could be incorporated in an existing route have been taken. Now include single routes.
        for (int cust : customers) {
            boolean present = false;
            for (Route r : routes) {
                if (r.route.contains(cust)) {
                    present = true;
                    break;
                }
            }
            if (!present) {
                Route single = new Route();
                single.addCustomer(depot);
                single.addCustomer(cust);
                single.addCustomer(depot);        
                routes.add(single); // For adding the single customer routes
            }
        }
        // System.out.println(routes); // Debug
        return routes;
    }
}
