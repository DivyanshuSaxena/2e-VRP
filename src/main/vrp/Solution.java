package vrp;

import java.util.Vector;

// Class to represent the solution
class Solution {
    Vector<Route> routes;
    int solutionCost;
    Solution() {
        routes = new Vector<Route>();
        solutionCost = 0;
    }
    public String toString() {
        // Change this function
        String sol = "";
        for (Route route : this.routes) {
            sol += "[";
            int prevcp = -1;
            for (int cp : route.route) {                    
                if (cp >= Main.numNodes) {
                    int originalcp = Main.routedCarparks.elementAt(cp-Main.numNodes).cpindex;
                    if (prevcp == originalcp) {
                        sol += ("{" + Main.routedCarparks.elementAt(cp-Main.numNodes).route + "}, ");
                    } else {
                        sol = sol + "\n\t" + originalcp + " : ";
                        sol += ("{" + Main.routedCarparks.elementAt(cp-Main.numNodes).route + "}, ");
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
    public int getCost() {
        // Function to evaluate the total costs of the solution.
        // Note :- This function should include the infeasibility costs
        // Presently, no infeasibility costs are taken into account
        int cost = 0;
        for (Route route : this.routes) {
            cost += route.getCost();
        }
        for (RouteCarpark cp : Main.routedCarparks) {
            cost += cp.route.getCost();
        }
        // Add the infeasibility costs here.
        this.solutionCost = cost;
        return cost;
    }
    public int getSwapCost(CustomerIndex ci1, CustomerIndex ci2) {
        Route swapRoute1 = Main.routedCarparks.elementAt(this.routes.elementAt(ci1.route).route.elementAt(ci1.routecp)-Main.numNodes).route;
        Route swapRoute2 = Main.routedCarparks.elementAt(this.routes.elementAt(ci2.route).route.elementAt(ci2.routecp)-Main.numNodes).route;
        int customer1 = swapRoute1.route.elementAt(ci1.index);
        int customer1prev = swapRoute1.route.elementAt(ci1.index);
        int customer1next = swapRoute1.route.elementAt(ci1.index);
        int customer2 = swapRoute2.route.elementAt(ci2.index);
        int customer2prev = swapRoute2.route.elementAt(ci2.index);
        int customer2next = swapRoute2.route.elementAt(ci2.index);
        int subtractCost1 = Main.nodesDistance[customer1][customer1next] + Main.nodesDistance[customer1prev][customer1];
        int subtractCost2 = Main.nodesDistance[customer2][customer2next] + Main.nodesDistance[customer2prev][customer2];
        int addCost1 = Main.nodesDistance[customer2][customer1next] + Main.nodesDistance[customer1prev][customer2];
        int addCost2 = Main.nodesDistance[customer1][customer2next] + Main.nodesDistance[customer2prev][customer1];
        return (this.solutionCost - subtractCost1 - subtractCost2 + addCost1 + addCost2);
    }
    public CustomerIndex getRandomCustomer() {
        // Function to get a random customer from the solution.
        // ***Remove zero choose options
        CustomerIndex cIndex = new CustomerIndex();
        int totalCustomers = 0;
        for (Route route : this.routes) {
            totalCustomers += route.route.size();
        }
        double chooseRoute = Math.random() * totalCustomers;
        Route chosen = new Route();
        int sum = 0;
        for (int i = 0; i < this.routes.size(); i++) {
            if(sum >= chooseRoute) {
                chosen = this.routes.elementAt(i);
                cIndex.route = i;
                break;
            }
            sum += this.routes.elementAt(i).route.size();
        }
        // chosen holds the Route from which the random customer is to be taken, choose the routeCarpark
        cIndex.routecp = chosen.route.elementAt(1 + ((int)Math.random() * (chosen.route.size()-2))); // Index of the selected routecarpark
        RouteCarpark chosenCarpark = Main.routedCarparks.elementAt(cIndex.routecp);
        cIndex.index = chosenCarpark.route.route.elementAt(1 + ((int)Math.random() * (chosenCarpark.route.route.size()-2))); // Index of the randomly chosen customer in the route 
        return cIndex; 
    }
    public Solution getBestNeighbor() {
        // Generate Neighborhood logic here
        Solution bestSolution = new Solution();
        // Apply the move operator on the Solution to get to a better solution
        boolean improved = false, change = false;
        int iterations = 0, maxMoveIterations = 100; // Hyper-Parameter
        int routeIndex = 0;
        Route changedRoute = new Route();
        while (!improved) {
            CustomerIndex ci = getRandomCustomer();
            changedRoute = Main.routedCarparks.elementAt(this.routes.elementAt(ci.route).route.elementAt(ci.routecp)).route.clone(); // Index of the selected routecarpark
            int customer = changedRoute.route.elementAt(ci.index); // Index of the selected random customer
            // This customer is to be placed in the best location, in the route of the given RouteCarpark
            int prevCustomer = changedRoute.route.elementAt(ci.index-1);
            int nextCustomer = changedRoute.route.elementAt(ci.index-1);
            int sameCost = solutionCost - Main.nodesDistance[prevCustomer][customer] - Main.nodesDistance[customer][nextCustomer] + Main.nodesDistance[prevCustomer][nextCustomer];
            int bestIndex = ci.index, bestCost = solutionCost;
            for (int i = 1; i < this.routes.elementAt(ci.route).route.size()-1; i++) {
                // Loop to iterate over the places in the current route
                if (i == ci.index) continue;
                int prev = changedRoute.route.elementAt(i-1);
                int next = changedRoute.route.elementAt(i);
                int newCost = sameCost + Main.nodesDistance[prev][customer] + Main.nodesDistance[customer][next] - Main.nodesDistance[prev][next];
                if (bestCost > newCost) {
                    bestCost = newCost;
                    bestIndex = i;
                }
            }
            // bestIndex contains the position of best insertion of the customer
            if (bestIndex != ci.index) {
                routeIndex = ci.route;
                changedRoute.removeCustomer(ci.index);  
                changedRoute.addCustomer(customer,bestIndex);
                improved = true;
                change = true;
            } else {
                iterations++;
            }
            // Random customer relocated to the best location in the route
            if (iterations == maxMoveIterations) break;
        }
        if (change) this.routes.set(routeIndex, changedRoute);
        System.out.println("Cost after improved move : " + this.getCost());

        // Iterated Swap Procedure
        improved = false;
        while(!improved) {
            CustomerIndex ci1 = getRandomCustomer();
            CustomerIndex ci2 = getRandomCustomer();
            if (solutionCost > getSwapCost(ci1, ci2)) {
                // Swap the two customers
                Route swapRoute1 = Main.routedCarparks.elementAt(this.routes.elementAt(ci1.route).route.elementAt(ci1.routecp)-Main.numNodes).route;
                Route swapRoute2 = Main.routedCarparks.elementAt(this.routes.elementAt(ci2.route).route.elementAt(ci2.routecp)-Main.numNodes).route;
                int customer1 = swapRoute1.route.elementAt(ci1.index);
                int customer2 = swapRoute2.route.elementAt(ci2.index);
                swapRoute1.setCustomer(customer2, ci1.index);
                swapRoute2.setCustomer(customer1, ci2.index);
                improved = true;
            }
        }
        System.out.println("Cost after iterated swap procedure : " + this.getCost());
        
        return bestSolution;
    }
}