// Entry Point for the Routing Problem
package vrp;

import java.util.*;
import java.io.*;

import io.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Main {
    public static int numCustomers, numNodes, numCarpark;
    public static int numVehicles1, numVehicles2;
    public static int l1cap, l2cap;
    public static Vector<Integer> x_coord, y_coord;
    public static double nodesDistance[][];
    public static Customer customers[];
    public static Carpark carparks[];
    static Vector<Vehicle> vehicles;
    static Scanner sc;

    // Change the name of this method suitably
    public static void main(String args[]) throws IOException {
        // The Method reads the inputs from a file and initializes the data structures
        String filename = args[0];
        File file = new File(filename);
        sc = new Scanner(file);
        Fileio fileio = new Fileio(sc);
        if (filename.contains("E-n13")) {
            fileio.setOneInput();
        } else {
            fileio.setTwoInput();
        }
        solve();
    }
    @SuppressWarnings("unchecked")
	public static JSONObject constructJSON(Solution solution) {
        JSONObject object = new JSONObject();
        JSONArray vehicles = new JSONArray();
        Vector<Integer> vehicleIndices = new Vector<Integer>();

        for (Route firstLevel : solution.routes) {
            JSONObject level1vehicle = new JSONObject();
            JSONArray level1route = new JSONArray();
            for (int node : firstLevel.route) {
                if (node == 0) {
                    level1route.add(node);
                } else {
                    int cp = Main.vehicles.elementAt(node-Main.numNodes).cpindex;
                    vehicleIndices.add(node-Main.numNodes);
                    level1route.add(cp);
                }
            }
            level1vehicle.put("route", level1route);
            level1vehicle.put("type", new Integer(1));
            level1vehicle.put("cost", new Double(firstLevel.routeCost));

            vehicles.add(level1vehicle);
        }

        for (int i : vehicleIndices) {
            Vehicle vehicle = Main.vehicles.elementAt(i);
            JSONObject level2vehicle = new JSONObject();
            JSONArray level2route = new JSONArray();
            for (int node : vehicle.route.route) {
                if (node <= Main.numCarpark) {
                    int cp = Main.vehicles.elementAt(node-Main.numNodes).cpindex;
                    level2route.add(cp);
                } else {
                    level2route.add(node-Main.numCarpark);
                }
            }
            level2vehicle.put("route", level2route);
            level2vehicle.put("type", new Integer(2));
            level2vehicle.put("cost", new Double(vehicle.route.routeCost));

            vehicles.add(level2vehicle);
        }

        object.put("vehicles", vehicles);
        object.put("cost", new Double(solution.getCost()));
        return object;
    }
    public static Solution solve() throws IOException {
        long startTime = System.currentTimeMillis();
        Solution initsol = getInitialSoln();
        double initCost = initsol.getCost();
        GiantRoute bestSolution = new GiantRoute();
        System.out.println("Initial Solution : " + initsol + " cost " + initCost);

        // Use initsol to develop the further solutions here.
        int numUselessIterations = Main.numCustomers/10; // Hyper-Parameter
        int iterations = 0;
        Solution bestFoundSoln = initsol;
        while (true) {
            boolean improvement = bestFoundSoln.updateBestNeighbor();
            System.out.println("Cost after local search : " + bestFoundSoln.solutionCost); // Debug
            GiantRoute bfs = bestFoundSoln.getGiantRoute();
            if (bfs.cost < bestSolution.cost || bestSolution.cost == 0) {
                bestSolution = bfs;
                improvement = true;
                System.out.println("Found better solution cost : " + bfs.cost);
            } else improvement = false;

            // Find the best solution of the generated neighborhood, and proceed with it further 
            bestFoundSoln = bestFoundSoln.perturb();
            System.out.println("Cost after perturb : " + bestFoundSoln.solutionCost);
            System.out.println("--------------------------------------------------"); // Debug
            
            if (improvement) {
                iterations = 0;
            } else {
                iterations++;
                if (iterations == numUselessIterations) break;
            }
        }
        long endTime = System.currentTimeMillis();
        Solution finalSolution = bestSolution.getSolution();
        System.out.println("Final Solution : " + finalSolution + " cost " + bestSolution.cost);        
        System.out.println("Running time : " + (endTime-startTime));
        sc.close();

        // Write the solution in solution.txt
        PrintWriter pWriter = new PrintWriter("./files/output/solution.txt", "UTF-8");
        for (int x : x_coord) {
            pWriter.print(x + " ");
        }
        pWriter.println();
        for (int y : y_coord) {
            pWriter.print(y + " ");
        }
        pWriter.println();
        pWriter.println(numNodes);
        pWriter.println(finalSolution);
        pWriter.close();

        // Report Percentage Improvement
        System.out.println("Percentage Improvement : " + ((initCost-finalSolution.getCost())/initCost*100));

        // Check Feasibility
        if (finalSolution.checkFeasibility())
            System.out.println("The solution is feasible");
        else
            System.out.println("The solution is not feasible");

        // Call the Python script to display the results
        String command = "cmd /c py ./files/display.py --eclipse";
        Process p = Runtime.getRuntime().exec(command);
        try {
            p.waitFor();
            BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line;
            while ((line = bri.readLine()) != null) {
                System.out.println(line);
            }
            bri.close();
            while ((line = bre.readLine()) != null) {
                System.out.println(line);
            }
            bre.close();
            p.waitFor();
            System.out.println("Done.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        p.destroy();

        return finalSolution;
    }
    public static Solution getInitialSoln() {
        // Finds the initial solution and places it in the static variable route.
        Solution initial = new Solution();
        
        // Assign each customer to the nearest carpark.
        for(int i = 0; i < numCustomers; i++) {
        	int customerOffset = numCarpark + 1;
            double minDistance = -1;
            int assigned = 0;
            for(int j = 1; j <= numCarpark; j++) {
                if (minDistance > nodesDistance[i+customerOffset][j] || minDistance == -1) {
                    minDistance = nodesDistance[i+customerOffset][j];
                    assigned = j;
                }
            }
            // System.out.println("Carpark assigned to " + customers[i].id + " is " + assigned); // Debug
            customers[i].assignedPark = assigned; // Customer at index i in customers array is assigned the carpark at index 'assigned' in nodes numbering 
            carparks[assigned-1].addCustomer(customers[i]); // The carpark also holds the customer now.
        }

        // Add the required Carparks to the initial solution
        Vector<Integer> carparksLevel1 = new Vector<Integer>();
        Vector<Route> appendRoutes = new Vector<Route>();
        for (Carpark cp : carparks) {
            if (cp.customers.size() > 0) {
                // Apply Clarke and Wright's Savings Algorithm for the second level routes (carparks)
                Vector<Integer> carparkCustomers = new Vector<Integer>();
                for (Customer customer : cp.customers) {
                    carparkCustomers.add(customer.id); // To get the vector of indices of customers assigned to the carpark.
                }
                cp.routes = savingSolution(carparkCustomers, cp.id, l2cap);
                // If the number of vehicles required is more than the number of vehicles available
                if (cp.routes.size() > numVehicles2) {
                    System.out.println("Routes for carpark " + cp.id + " is more than the limit"); // Debug
                    // Choose the route with the largest average distance from the carpark and remove the route
                    int routeIndex = -1, index = 0;
                    double maxAvgDistance = 0;
                    for (Route route : cp.routes) {
                        double avgDistance = 0;
                        for (int node : route.route) {
                            avgDistance += nodesDistance[cp.id][node];
                        }
                        avgDistance = avgDistance/(route.route.size()-2);
                        if (avgDistance > maxAvgDistance || routeIndex == -1) {
                            maxAvgDistance = avgDistance;
                            routeIndex = index;
                        }
                        index++;
                    }
                    appendRoutes.add(cp.routes.remove(routeIndex));
                }
                // We now have the routes for each carpark.
                carparksLevel1.add(cp.id);
            }
        }

        // Add the removed vehicles
        for (Route appendRoute : appendRoutes) {
            int bestCarpark = -1, index = 0;
            double minAvgDistance = 0;
            for (Carpark carpark : carparks) {
                if (carpark.routes.size() < numVehicles2) {
                    double avgDistance = 0;
                    for (int node : appendRoute.route) {
                        if (node > numCarpark)  avgDistance += nodesDistance[carpark.id][node];
                    }
                    avgDistance = avgDistance/(appendRoute.route.size()-2);
                    if (avgDistance < minAvgDistance || bestCarpark == -1) {
                        minAvgDistance = avgDistance;
                        bestCarpark = index;
                    }
                }
                index++;
            }
            appendRoute.setStart(carparks[bestCarpark].id);
            appendRoute.setEnd(carparks[bestCarpark].id);
            carparks[bestCarpark].routes.add(appendRoute);
            if (carparksLevel1.indexOf(carparks[bestCarpark].id) == -1) carparksLevel1.add(carparks[bestCarpark].id);
        }

        // Decompose the carparks into Vehicles for each route
        Vector<Integer> vehicles = new Vector<Integer>();
        Main.vehicles = new Vector<Vehicle>();
        int temp = Main.numNodes;
        for (int id : carparksLevel1) {
            for (Route route : carparks[id-1].routes) {
                Vehicle rc = new Vehicle(id, route);
                vehicles.add(temp);
                Main.vehicles.add(rc);
                temp++;
            }
        }
        // Apply Clarke and Wright's Savings Algorithm for the first level Vehicles
        initial.routes = savingSolution(vehicles, 0, l1cap);
        // System.out.println("Final Routes: " + initial.routes); // Debug
        initial.updateCost();
        return initial;
    }
    public static Vector<Route> savingSolution(Vector<Integer> customers, final int depot, int capacity) {
        // This function implements the Clarke and Wright's Saving Algortihm
    	// System.out.println("Applying C&W for " + depot + " with customers: " + customers); // Debug
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
                // Watch out for temporary carparks
                if (firsto1 >= Main.numNodes) firsto1 = Main.vehicles.elementAt(firsto1-Main.numNodes).cpindex;
                if (secondo1 >= Main.numNodes) secondo1 = Main.vehicles.elementAt(secondo1-Main.numNodes).cpindex;
                if (firsto2 >= Main.numNodes) firsto2 = Main.vehicles.elementAt(firsto2-Main.numNodes).cpindex;
                if (secondo2 >= Main.numNodes) secondo2 = Main.vehicles.elementAt(secondo2-Main.numNodes).cpindex;
                double savings1 = nodesDistance[depot][firsto1] + nodesDistance[depot][secondo1] - nodesDistance[firsto1][secondo1];
                double savings2 = nodesDistance[depot][firsto2] + nodesDistance[depot][secondo2] - nodesDistance[firsto2][secondo2];
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
                    int commonDemand = 0;
                    if (positionOfStart == -2) {
                        commonDemand = ((bestStart >= Main.numNodes)? Main.vehicles.elementAt(bestStart-Main.numNodes).route.demand : Main.customers[bestStart-numCarpark-1].demand);
                    } else {
                        commonDemand = ((bestEnd >= Main.numNodes)? Main.vehicles.elementAt(bestEnd-Main.numNodes).route.demand : Main.customers[bestEnd-numCarpark-1].demand);                        
                    }
                	if (newRoute.route.size() == 0) {
                		if ((bestSavings.demand + currRoute.demand - commonDemand) <= capacity) {
                            // System.out.println("Found a suitable merge, demand: " + (bestSavings.demand + currRoute.demand - commonDemand)); // Debug
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
                    		// System.out.println("Merging " + newRoute + " and " + currRoute);
                    		added = true;
                			discard = false;
                    		newRoute = newRoute.mergeRoute(currRoute);
                    		mergedIndex = j;
                    	} else {
                    		routeIndex = -1;
                    		discard = true;
                    		break;
                    	}
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