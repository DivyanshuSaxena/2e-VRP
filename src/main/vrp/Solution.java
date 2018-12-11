package vrp;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

// Class to represent the solution
public class Solution implements Iterable<CustomerIndex> {
    Vector<Route> routes;
    double solutionCost;
    Solution() {
        routes = new Vector<Route>();
        solutionCost = 0.0;
    }

    public String toString() {
        // Change this function
        String sol = "";
        for (Route route : this.routes) {
            sol += "[";
            int prevcp = -1;
            for (int cp : route.route) {                    
                if (cp >= Main.numNodes) {
                    int originalcp = Main.vehicles.elementAt(cp-Main.numNodes).cpindex;
                    Route vehicleRoute = Main.vehicles.elementAt(cp-Main.numNodes).route;
                    if (prevcp == originalcp) {
                        sol += ("{" + vehicleRoute + "}: " + (vehicleRoute.route.size()-2));
                    } else {
                        sol = sol + "\n\t" + originalcp + " : ";
                        sol += ("{" + vehicleRoute + "}: " + (vehicleRoute.route.size()-2));
                        prevcp = originalcp;                        
                    }
                } else {
                    sol = sol + "\n\t" + cp + ",";
                }
            }
            sol += "\n],\n";
        }
        return sol;
    }

    public Iterator<CustomerIndex> iterator() {
        return new SolutionIterator(this);
    }

    // Access Methods
    public GiantRoute getGiantRoute() {
        GiantRoute gr = new GiantRoute();
        Vector<Integer> giantRoute = new Vector<Integer>();
        for (Route route : this.routes) {
            for (int depotIndex : route.route) {
                if (depotIndex < Main.numNodes) giantRoute.add(depotIndex);
                else {
                    for (int customer : Main.vehicles.elementAt(depotIndex-Main.numNodes).route.route) {
                        giantRoute.add(customer);
                    }
                }
            }
        }
        gr.giantRoute = giantRoute;
        gr.cost = this.solutionCost;
        return gr;
    }

    public double getCost() {
        return this.solutionCost;
    }

    public void updateFirstLevelDemands() {
        for (Route route : this.routes) {
            int firstLevelDemand = 0;
            for (int vehicle : route.route) {
                if (vehicle != 0) {
                    Route secondLevel = Main.vehicles.elementAt(vehicle-Main.numNodes).route;
                    int secondLevelDemand = 0;
                    for (int cust : secondLevel.route) {
                        if (cust > Main.numCarpark) {
                            secondLevelDemand += Main.customers[cust-Main.numCarpark-1].demand;
                        }
                    } 
                    firstLevelDemand += secondLevelDemand;
                }
            }
            route.demand = firstLevelDemand;
        }
    }

    public boolean isSwapFeasible(CustomerIndex ci1, CustomerIndex ci2) {
        int cp1 = ci1.routecp;
        int cp2 = ci2.routecp;
        Route swapRoute1 = Main.vehicles.elementAt(cp1-Main.numNodes).route;
        Route swapRoute2 = Main.vehicles.elementAt(cp2-Main.numNodes).route;
        Customer customer1 = Main.customers[swapRoute1.route.elementAt(ci1.index)-Main.numCarpark-1];
        Customer customer2 = Main.customers[swapRoute2.route.elementAt(ci2.index)-Main.numCarpark-1];

        int flag = 0;
        for (Route route : routes) {
            int firstLevelDemand = route.getDemand();
            if (route.route.contains(cp1)) {
                firstLevelDemand += (customer2.demand - customer1.demand);
                if (firstLevelDemand <= Main.l1cap) {
                    flag++;
                } else break;
            }
            if (route.route.contains(cp2)) {
                firstLevelDemand += (customer1.demand - customer2.demand);
                if (firstLevelDemand <= Main.l1cap) {
                    flag++;
                } else break;
            }
            if (flag == 2) break;
        }
        if (flag == 2) return true;
        return false;
    }

    public double updateCost() {
        // Function to evaluate the total costs of the solution.
        double cost = this.getActualCost();
        // Add the infeasibility costs here.
        this.solutionCost = cost;
        return cost;
    }

    public double getActualCost() {
        double totalCost = 0.0;
        for (Route firstLevel : routes) {
            double firstLevelCost = 0.0;
            for (int cp = 0; cp < firstLevel.route.size()-1; cp++) {
                int vehicle = firstLevel.route.elementAt(cp);
                int vehicleNext = firstLevel.route.elementAt(cp+1);
                int originalcp = vehicle == 0 ? 0 : Main.vehicles.elementAt(vehicle-Main.numNodes).cpindex;
                int originalcpNext = vehicleNext == 0 ? 0 : Main.vehicles.elementAt(vehicleNext-Main.numNodes).cpindex;
                firstLevelCost += Main.nodesDistance[originalcp][originalcpNext];
                double secondLevelCost = 0.0;
                if (cp != 0) {
                    Vehicle v = Main.vehicles.elementAt(vehicle-Main.numNodes);
                    for (int cust = 0; cust < v.route.route.size()-1; cust++) {
                        int cust1 = v.route.route.elementAt(cust);
                        int cust2 = v.route.route.elementAt(cust+1);
                        secondLevelCost += Main.nodesDistance[cust1][cust2];
                    }
                    if (v.route.getCost() != secondLevelCost) {
                        System.out.println("Problem at vehicle with route " + v.route.route); // Debug
                    }
                    totalCost += secondLevelCost;
                }
            }
            if (firstLevel.getCost() != firstLevelCost) {
                System.out.println("Problem at I Level route " + firstLevel.route); // Debug
            }
            totalCost += firstLevelCost;
        }

        return totalCost;   
    }

    private double getSwapCost(CustomerIndex ci1, CustomerIndex ci2) {
        // This function returns the swap cost obtained on swapping customers at ci1 and ci2.
        Route swapRoute1 = Main.vehicles.elementAt(ci1.routecp-Main.numNodes).route;
        Route swapRoute2 = Main.vehicles.elementAt(ci2.routecp-Main.numNodes).route;
        double swapCost = 0.0;
        
        int customer1 = swapRoute1.route.elementAt(ci1.index);
        int customer1prev = swapRoute1.route.elementAt(ci1.index-1);
        int customer1next = swapRoute1.route.elementAt(ci1.index+1);
        
        int customer2 = swapRoute2.route.elementAt(ci2.index);
        int customer2prev = swapRoute2.route.elementAt(ci2.index-1);
        int customer2next = swapRoute2.route.elementAt(ci2.index+1);

        // System.out.println("Swapping " + customer1 + " in " + swapRoute1 + " with " + customer2 + " in " + swapRoute2); // Debug
        double subtractCost1 = Main.nodesDistance[customer1][customer1next] + Main.nodesDistance[customer1prev][customer1];
        double subtractCost2 = Main.nodesDistance[customer2][customer2next] + Main.nodesDistance[customer2prev][customer2];
        double addCost1 = Main.nodesDistance[customer2][customer1next] + Main.nodesDistance[customer1prev][customer2];
        double addCost2 = Main.nodesDistance[customer1][customer2next] + Main.nodesDistance[customer2prev][customer1];
        swapCost = (this.solutionCost - subtractCost1 - subtractCost2 + addCost1 + addCost2);
        if (ci1.index-ci2.index == 1 || ci1.index-ci2.index == -1) {
            // Adjacent nodes
        	swapCost = swapCost + 2*Main.nodesDistance[customer1][customer2];
        }
        // System.out.println("Cost of swapping " + customer1 + " and " + customer2 + " : " + swapCost); // Debug
        return swapCost;
    }

    public CustomerIndex getRandomCustomer() {
        // Function to get a random customer from the solution.
        CustomerIndex cIndex = new CustomerIndex();
        int totalVehicles = 0;
        for (Route route : this.routes) {
            totalVehicles += (route.route.size()-2);
        }
        double chooseRoute = Math.random() * totalVehicles;
        Route chosen = new Route();
        int sum = 0;
        for (int i = 0; i < this.routes.size(); i++) {
            if(chooseRoute <= sum + (this.routes.elementAt(i).route.size()-2)) {
                chosen = this.routes.elementAt(i);
                cIndex.route = i;
                break;
            }
            sum += (this.routes.elementAt(i).route.size()-2);
        }
        // chosen holds the Route from which the random customer is to be taken, choose the vehicle
        int in = (int)(Math.random() * (chosen.route.size()-2));
        // System.out.println(in + " at " + chosen); // Debug
        cIndex.routecp = chosen.route.elementAt(1 + in); // Index of the selected vehicle
        Vehicle chosenCarpark = Main.vehicles.elementAt(cIndex.routecp-Main.numNodes);
        in = (int)(Math.random() * (chosenCarpark.route.route.size()-2));
        cIndex.index = (1 + in); // Index of the randomly chosen customer in the route 
        // System.out.println(in + " at " + chosenCarpark.route); // Debug
        return cIndex; 
    }

    public int getRandomVehicle() {
        int index = (int)((Main.vehicles.size()) * Math.random());
        if (index == Main.vehicles.size())  return 0;
        return index;
    }

    // First Level Methods
    
    // Second Level Methods
    private boolean moveOperator(CustomerIndex ci) {
        boolean improvement = false;
        Route clonedRoute = Main.vehicles.elementAt(ci.routecp-Main.numNodes).route; // Index of the selected vehicle
        int customer = clonedRoute.route.elementAt(ci.index); // Index of the selected random customer
        // This customer is to be placed in the best location, in the route of the given Vehicle
        int prevCustomer = clonedRoute.route.elementAt(ci.index-1);
        int nextCustomer = clonedRoute.route.elementAt(ci.index+1);
        // System.out.println("For route: "+ clonedRoute + " " + clonedRoute.getCost() + " " + customer + " " + prevCustomer + " " + nextCustomer); // Debug
        double sameCost = solutionCost - Main.nodesDistance[prevCustomer][customer] - Main.nodesDistance[customer][nextCustomer] + Main.nodesDistance[prevCustomer][nextCustomer];
        int bestIndex = ci.index;
        double bestCost = this.solutionCost;
        for (int i = 1; i < clonedRoute.route.size()-1; i++) {
            // Loop to iterate over the places in the current route
            if (i == ci.index || i == ci.index+1) continue;
            int prev = clonedRoute.route.elementAt(i-1);
            int next = clonedRoute.route.elementAt(i);
            double newCost = sameCost + Main.nodesDistance[prev][customer] + Main.nodesDistance[customer][next] - Main.nodesDistance[prev][next];
            if (bestCost - newCost > 2) {
                // System.out.println(bestCost + " " + newCost); // Debug
                bestCost = newCost;
                bestIndex = i;
            }
        }
        // bestIndex contains the position of best insertion of the customer
        if (bestIndex != ci.index) {
            // System.out.println("Found a better solution, relocate customer " + customer + " in route " + clonedRoute + " at " + bestIndex); // Debug
            int vehicleIndex = ci.routecp;
            clonedRoute.addCustomer(customer,bestIndex);
            if (bestIndex < ci.index) clonedRoute.removeCustomer(ci.index+1);
            else clonedRoute.removeCustomer(ci.index);
            Main.vehicles.elementAt(vehicleIndex-Main.numNodes).route = clonedRoute;
            improvement = true;
            this.updateCost();
            this.updateFirstLevelDemands();
        }
        // Random customer relocated to the best location in the route
        return improvement;
    }
    private boolean iteratedSwapOperator(CustomerIndex ci1, CustomerIndex ci2) {
        boolean improvement = false;
        double swapCost  = getSwapCost(ci1,ci2);
        
        if (this.solutionCost - swapCost > 2) {
            double prevCost = this.solutionCost;
            // Swap the two customers
            Route swapRoute1 = Main.vehicles.elementAt(ci1.routecp-Main.numNodes).route;
            Route swapRoute2 = Main.vehicles.elementAt(ci2.routecp-Main.numNodes).route;
            int customer1 = swapRoute1.route.elementAt(ci1.index);
            int customer2 = swapRoute2.route.elementAt(ci2.index);
            if (swapRoute1.isSwapFeasible(customer1, customer2) && swapRoute2.isSwapFeasible(customer2, customer1)) {
                // Check first level demands consistency
                if (this.isSwapFeasible(ci1, ci2)) {
                    System.out.println("Swapping " + customer1 + " and " + customer2 + " in " + swapRoute1); // Debug
                    swapRoute1.setCustomer(customer2, ci1.index);
                    swapRoute2.setCustomer(customer1, ci2.index);
                    improvement = true;
                    this.updateCost();
                    this.updateFirstLevelDemands();
                    if (Math.abs(swapCost-this.solutionCost) > 0.1) System.out.println("PROBLEM IN SWAPPING HERE"); // Debug
                }
            } else {
                // All infeasible but good quality solutions are received here
                // System.out.println("Infeasible Solution");
            }
        }
        return improvement;
    }
    private double exchangeOperator(CustomerIndex ci1, CustomerIndex ci2) {
        if (ci1.routecp==ci2.routecp) {
            return 0;
        }
        Route exchangeRoute1 = Main.vehicles.elementAt(ci1.routecp-Main.numNodes).route;
        Route exchangeRoute2 = Main.vehicles.elementAt(ci2.routecp-Main.numNodes).route;
        int customer1 = exchangeRoute1.route.elementAt(ci1.index);
        int customer1prev = exchangeRoute1.route.elementAt(ci1.index-1);
        int route1last = exchangeRoute1.route.elementAt(exchangeRoute1.route.size()-2);
        int route1depot = Main.vehicles.elementAt(ci1.routecp-Main.numNodes).cpindex;
        int customer2 = exchangeRoute2.route.elementAt(ci2.index);
        int customer2prev = exchangeRoute2.route.elementAt(ci2.index-1);
        int route2last = exchangeRoute2.route.elementAt(exchangeRoute2.route.size()-2);
        int route2depot = Main.vehicles.elementAt(ci2.routecp-Main.numNodes).cpindex;
        double addCost1 = Main.nodesDistance[customer1prev][customer2] + Main.nodesDistance[customer2prev][customer1] + Main.nodesDistance[route2last][route1depot] + Main.nodesDistance[route1last][route2depot];
        double subtractCost1 = Main.nodesDistance[customer1prev][customer1] + Main.nodesDistance[customer2prev][customer2] + Main.nodesDistance[route1last][route1depot] + Main.nodesDistance[route2last][route2depot];
        // Exchange the customers if better.
        return (addCost1-subtractCost1);
        // return improvement;
    }
    public boolean updateBestNeighbor() {
        // Generate Neighborhood logic here
        // Apply the move operator on the Solution to get to a better solution
        System.out.println("Before Local Search " + this.getCost()); // Debug
        if (!this.checkFeasibility()) {
            System.out.println("Perturb Operator Problem"); // Debug
        }

        boolean improvement = false, localImp = false;
        SolutionIterator iter = new SolutionIterator(this);
        while (iter.hasNext()) {
            CustomerIndex ci = iter.next();
            localImp = localImp || this.moveOperator(ci);
        }
        // if (localImp) System.out.println("After improved move, solution cost: " + this.getCost()); // Debug
        improvement = improvement || localImp;
        if (!this.checkFeasibility()) {
            System.out.println("Move Operator Problem"); // Debug
        }

        // Iterated Swap Procedure
        iter.reset();
        localImp = false;
        while (iter.hasNext()) {
            CustomerIndex ci1 = iter.next();
            SolutionIterator innerIterator = new SolutionIterator(this);
            while (innerIterator.hasNext()) {
                CustomerIndex ci2 = innerIterator.next();
                if (ci1.isSameRoute(ci2)) continue;
                localImp = localImp || this.iteratedSwapOperator(ci1, ci2);
            }
        }
        // if (localImp) System.out.println("After iterated swap procedure, solution cost: " + this.getCost()); // Debug
        improvement = improvement || localImp;
        if (!this.checkFeasibility()) {
            System.out.println("ISP Operator Problem"); // Debug
        }

        // Segment Exchange Operator
        iter.reset();
        localImp = false;
        CustomerIndex bestPair1 = new CustomerIndex();
        CustomerIndex bestPair2 = new CustomerIndex();
        double bestCost = 0;
        while (iter.hasNext()) {
            CustomerIndex ci1 = iter.next();
            SolutionIterator innerIterator = new SolutionIterator(this);
            while (innerIterator.hasNext()) {
                CustomerIndex ci2 = innerIterator.next();
                if (ci1.isSameRoute(ci2)) continue;
                double cost = this.exchangeOperator(ci1, ci2);
                if (cost - bestCost > 2) {
                    bestCost = cost;
                    bestPair1 = ci1;
                    bestPair2 = ci2;
                }
            }
        }
        if (bestCost < 0) {
            Route exchangeRoute1 = Main.vehicles.elementAt(bestPair1.routecp-Main.numNodes).route;
            Route exchangeRoute2 = Main.vehicles.elementAt(bestPair2.routecp-Main.numNodes).route;
            Vector<Integer> seg1 = exchangeRoute1.getSubRoute(bestPair1.index, exchangeRoute1.route.size()-2);
            Vector<Integer> seg2 = exchangeRoute2.getSubRoute(bestPair2.index, exchangeRoute2.route.size()-2);
            if (exchangeRoute1.isExchangeFeasible(seg2, bestPair1.index) && exchangeRoute2.isExchangeFeasible(seg1, bestPair2.index)) {
                // System.out.println("Exchanging " + customer1 + " from " + exchangeRoute1 + " with " + customer2 + " from " + exchangeRoute2); // Debug
                exchangeRoute1.addAllCustomers(seg2, bestPair1.index);
                exchangeRoute2.addAllCustomers(seg1, bestPair2.index);
                exchangeRoute1.removeAllCustomers(bestPair1.index+seg2.size(), exchangeRoute1.route.size()-2);
                exchangeRoute2.removeAllCustomers(bestPair2.index+seg1.size(), exchangeRoute2.route.size()-2);    
                // System.out.println("New Routes : " + exchangeRoute1 + " and " + exchangeRoute2); // Debug
                localImp = true;
                this.updateCost();
                this.updateFirstLevelDemands();
                // System.out.println("Updated Cost : " + this.solutionCost); // Debug
            }           
        }
        // if (localImp) System.out.println("After exchange operator, solution cost: " + this.getCost()); // Debug
        improvement = improvement || localImp;
        if (!this.checkFeasibility()) {
            System.out.println("Exchange Operator Problem"); // Debug
        }

        // System.out.println("After Local Search " + this.getCost()); // Debug
        return improvement;
    }
    private Vector<Integer> routeRemoval(GiantRoute gr, int vehicleIndex) {
        Vector<Integer> customers = new Vector<Integer>();
        Vector<Integer> selectedRoute = Main.vehicles.elementAt(vehicleIndex).route.route;
        for (int customer : selectedRoute) {
            if (customer > Main.numCarpark) {
                customers.add(customer);
                gr.removeCustomer(customer);
            }
        }
        // System.out.println(customers); // Debug
        return customers;
    }
    private Vector<Integer> worstRemoval(GiantRoute gr, int q) {
        Vector<Integer> customerPool = new Vector<Integer>(); // Holds the Customer Ids that have been removed 
        Vector<Double> normRemovalCost = new Vector<Double>(); // Normalized Removal Cost
        for (Customer c : Main.customers) {
            int total = 0;
            for (double dist : Main.nodesDistance[c.id]) {
                total += dist;
            }
            double avgIncomingCost = (double)(total/Main.numNodes);
            normRemovalCost.add(gr.getCustomerRemovalCost(c.id)/avgIncomingCost);
        }
        for (int i = 0; i < q; i++) {
            double rcost1 = normRemovalCost.elementAt(0);
            int highest = -1;
            for (int j = 0; j < normRemovalCost.size(); j++) {
                if (customerPool.indexOf(j+Main.numCarpark+1) == -1) {
                    double rcost2 = normRemovalCost.elementAt(j);
                    if (rcost1 < rcost2 || highest == -1) {
                        rcost1 = rcost2;
                        highest = j;
                    }
                }
            }
            customerPool.add(highest+Main.numCarpark+1);
            gr.removeCustomer(highest+Main.numCarpark+1);
            // System.out.println("Customer Pool : " + customerPool); // Debug
            // System.out.println("Giant Route after worst removal : " + gr.giantRoute); // Debug
        }
        return customerPool;
    }

    private void regretInsertion(final GiantRoute gr, Vector<Integer> customers) {
        customers.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) { 
                double regcost1 = gr.getRegretCost(o1);
                double regcost2 = gr.getRegretCost(o2);
                if (regcost1 < regcost2) {
                    return -1;
                }
                return 1;
            }
        });
        // System.out.println("Customer Pool : " + customers); // Debug
        int count = 0;
        while (count < customers.size()) {
            int customer = customers.elementAt(count);
            gr.insertAtBestLocation(customer);
            count++;
        }
    }

    public Solution perturb() {
        // Perturb the local best found solution to get a new solution altogether
        final GiantRoute gr = this.getGiantRoute();
        System.out.println(gr.giantRoute); // Debug
        Solution perturbSoln = new Solution();
        int q = Main.numCustomers/10;

        double p = Math.random();
        Vector<Integer> customerPool = new Vector<Integer>(); 
        if (p <= 0.2) {
            customerPool = this.worstRemoval(gr,q); // Worst Removal
        } else {
            int vehicleIndex1 = this.getRandomVehicle();
            int cp = Main.vehicles.elementAt(vehicleIndex1).cpindex;
            System.out.println("Randomly Removed Vehicle: " + vehicleIndex1 + " " + cp); // Debug
            customerPool = this.routeRemoval(gr, vehicleIndex1); // Route Removal
        }
        System.out.println("Size of customer pool: " + customerPool.size());
        System.out.println(gr.giantRoute); // Debug
        this.regretInsertion(gr, customerPool); // Regret Insertion

        System.out.println(gr.giantRoute); // Debug
        // Check the solution for any removed carparks
        gr.removeUnusedCarparks();
        
        // System.out.println("Giant Route after regret insertion : " + gr.giantRoute); // Debug
        System.out.println(gr.giantRoute); // Debug
        perturbSoln = gr.getSolution();
        if (Math.abs(gr.cost - perturbSoln.solutionCost) > 0.0001) {
            System.out.println(gr.cost + " " + perturbSoln.solutionCost); // Debug
            System.out.println("COST INCONSISTENT 2"); // Debug
        }
        return perturbSoln;        
    }

    public boolean checkCostConsistency() {
        double updateCost = 0;
        for (Route route : this.routes) {
            updateCost += route.getCost();
        }
        for (Vehicle cp : Main.vehicles) {
            updateCost += cp.route.getCost();
        }

        double totalCost = this.getActualCost();

        if (totalCost-updateCost < -0.0001) {
            System.out.println("INCONSISTENT UPDATE COST");
            return false;
        }

        if (totalCost-this.solutionCost < 0.0001)
            return true;
        return false;
    }
    
    public boolean checkFeasibility() {
        // System.out.println("\n---------------------------------------------------");
        // System.out.println("---------------Checking Feasibility----------------");
        // System.out.println("Level 1 Capacity: " + Main.l1cap);
        // System.out.println("Level 2 Capacity: " + Main.l2cap);
        
        int customers = 0;
        for (Route route : this.routes) {
            int firstLevelDemand = 0;
            // System.out.println("----------------------------------------");
            for (int vehicle : route.route) {
                if (vehicle != 0) {
                    Route secondLevel = Main.vehicles.elementAt(vehicle-Main.numNodes).route;
                    int secondLevelDemand = 0;
                    for (int cust : secondLevel.route) {
                        if (cust > Main.numCarpark) {
                            customers++;
                            secondLevelDemand += Main.customers[cust-Main.numCarpark-1].demand;
                        }
                    }
                    // if (secondLevel.demand != secondLevelDemand) {
                    //     System.out.println("Demand inconsistent at II route(1) " + vehicle);
                    // }
                    if (secondLevelDemand > Main.l2cap) {
                        System.out.println("Demand inconsistent at II route(2) " + secondLevel.route);
                        System.out.println(secondLevelDemand + " " + secondLevel.demand);
                        return false;
                    } 
                    // System.out.println("Adding " + secondLevelDemand + " for " + secondLevel);
                    firstLevelDemand += secondLevelDemand;
                }
            }   
            // if (firstLevelDemand != route.demand) {
            //     System.out.println("Demand inconsistent at I route(1)");
            // }
            if (firstLevelDemand > Main.l1cap) {
                System.out.println("Demand inconsistent at I route(2)");
                System.out.println(firstLevelDemand + " " + route.demand);
                return false;
            }
        }
        if (customers == Main.numCustomers) 
            return true;
        return false;
    }
}