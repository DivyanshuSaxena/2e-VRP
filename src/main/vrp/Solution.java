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
        Route swapRoute1 = Main.routedCarparks.elementAt(ci1.routecp-Main.numNodes).route;
        Route swapRoute2 = Main.routedCarparks.elementAt(ci2.routecp-Main.numNodes).route;
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
        int totalRouteCarparks = 0;
        for (Route route : this.routes) {
            totalRouteCarparks += (route.route.size()-2);
        }
        double chooseRoute = Math.random() * totalRouteCarparks;
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
        // chosen holds the Route from which the random customer is to be taken, choose the routeCarpark
        int in = (int)(Math.random() * (chosen.route.size()-2));
        // System.out.println(in + " at " + chosen); // Debug
        cIndex.routecp = chosen.route.elementAt(1 + in); // Index of the selected routecarpark
        RouteCarpark chosenCarpark = Main.routedCarparks.elementAt(cIndex.routecp-Main.numNodes);
        in = (int)(Math.random() * (chosenCarpark.route.route.size()-2));
        cIndex.index = (1 + in); // Index of the randomly chosen customer in the route 
        // System.out.println(in + " at " + chosenCarpark.route); // Debug
        return cIndex; 
    }
    public Solution getBestNeighbor() {
        // Generate Neighborhood logic here
        Solution bestSolution = new Solution();
        // Apply the move operator on the Solution to get to a better solution
        int iterations = 0, maxMoveIterations = 100; // Hyper-Parameter
        int maxIspIterations = 10; // Hyper-Parameter
        int routeCarparkIndex = 0;
        Route clonedRoute = new Route();
        while (iterations < maxMoveIterations) {
            CustomerIndex ci = getRandomCustomer();
            clonedRoute = Main.routedCarparks.elementAt(ci.routecp-Main.numNodes).route; // Index of the selected routecarpark
            int customer = clonedRoute.route.elementAt(ci.index); // Index of the selected random customer
            // This customer is to be placed in the best location, in the route of the given RouteCarpark
            int prevCustomer = clonedRoute.route.elementAt(ci.index-1);
            int nextCustomer = clonedRoute.route.elementAt(ci.index+1);
            // System.out.println("For route: "+ clonedRoute + " " + clonedRoute.getCost() + " " + customer + " " + prevCustomer + " " + nextCustomer); // Debug
            int sameCost = solutionCost - Main.nodesDistance[prevCustomer][customer] - Main.nodesDistance[customer][nextCustomer] + Main.nodesDistance[prevCustomer][nextCustomer];
            int bestIndex = ci.index, bestCost = this.solutionCost;
            for (int i = 1; i < clonedRoute.route.size()-1; i++) {
                // Loop to iterate over the places in the current route
                if (i == ci.index || i == ci.index+1) continue;
                int prev = clonedRoute.route.elementAt(i-1);
                int next = clonedRoute.route.elementAt(i);
                int newCost = sameCost + Main.nodesDistance[prev][customer] + Main.nodesDistance[customer][next] - Main.nodesDistance[prev][next];
                if (bestCost > newCost) {
                	// System.out.println(bestCost + " " + newCost); // Debug
                    bestCost = newCost;
                    bestIndex = i;
                }
            }
            // bestIndex contains the position of best insertion of the customer
            if (bestIndex != ci.index) {
                routeCarparkIndex = ci.routecp;
                clonedRoute.addCustomer(customer,bestIndex);
                // System.out.println("Updated Route: " + clonedRoute + " " + clonedRoute.getCost()); // Debug
                Main.routedCarparks.elementAt(routeCarparkIndex-Main.numNodes).route = clonedRoute;
            } else {
                iterations++;
            }
            // Random customer relocated to the best location in the route
        }
        System.out.println("After improved move, solution : " + this.toString() + " with cost: " + this.getCost()); // Debug

        // Iterated Swap Procedure
        boolean improved = false;
        iterations = 0;
        while(!improved) {
            // System.out.println(this.routes + " at " + iterations); // Debug
            CustomerIndex ci1 = getRandomCustomer();
            CustomerIndex ci2 = getRandomCustomer();
            if (solutionCost > getSwapCost(ci1, ci2)) {
                // Swap the two customers
                Route swapRoute1 = Main.routedCarparks.elementAt(ci1.routecp-Main.numNodes).route;
                Route swapRoute2 = Main.routedCarparks.elementAt(ci2.routecp-Main.numNodes).route;
                int customer1 = swapRoute1.route.elementAt(ci1.index);
                int customer2 = swapRoute2.route.elementAt(ci2.index);
                swapRoute1.setCustomer(customer2, ci1.index);
                swapRoute2.setCustomer(customer1, ci2.index);
                improved = true;
            } else {
                iterations++;
            }
            if (iterations == maxIspIterations) {
                break;
            }
        }
        System.out.println("After iterated swap procedure, solution : " + this.toString() + " with cost: " + this.getCost());
        
        return bestSolution;
    }
}