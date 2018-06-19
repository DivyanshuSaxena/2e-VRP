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
        return this.solutionCost;
    }

    public int updateCost() {
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
        int swapCost = 0, type = 1, bestCost = 0;
        int customer1nextnext = -1, customer2nextnext = -1, customer1prevprev = -1, customer2prevprev = -1;
        
        int customer1 = swapRoute1.route.elementAt(ci1.index);
        int customer1prev = swapRoute1.route.elementAt(ci1.index-1);
        int customer1next = swapRoute1.route.elementAt(ci1.index+1);
        if (ci1.index < swapRoute1.route.size()-2)  customer1nextnext = swapRoute1.route.elementAt(ci1.index+2);
        if (ci1.index > 1)  customer1prevprev = swapRoute1.route.elementAt(ci1.index-2);
        
        int customer2 = swapRoute2.route.elementAt(ci2.index);
        int customer2prev = swapRoute2.route.elementAt(ci2.index-1);
        int customer2next = swapRoute2.route.elementAt(ci2.index+1);
        if (ci2.index < swapRoute2.route.size()-2)  customer2nextnext = swapRoute2.route.elementAt(ci2.index+2);
        if (ci2.index > 1)  customer2prevprev = swapRoute2.route.elementAt(ci2.index-2);
        if (ci1.isSameRoute(ci2) && ci2.index-ci1.index==2) {
            customer1nextnext = customer1;
            customer2prevprev = customer2;
        } else if (ci1.isSameRoute(ci2) && ci1.index-ci2.index==2) {
            customer1prevprev = customer1;
            customer2nextnext = customer2;
        }
        // System.out.println("Swapping " + customer1 + " in " + swapRoute1 + " with " + customer2 + " in " + swapRoute2); // Debug
        int subtractCost1 = Main.nodesDistance[customer1][customer1next] + Main.nodesDistance[customer1prev][customer1];
        int subtractCost2 = Main.nodesDistance[customer2][customer2next] + Main.nodesDistance[customer2prev][customer2];
        int addCost1 = Main.nodesDistance[customer2][customer1next] + Main.nodesDistance[customer1prev][customer2];
        int addCost2 = Main.nodesDistance[customer1][customer2next] + Main.nodesDistance[customer2prev][customer1];
        swapCost = (this.solutionCost - subtractCost1 - subtractCost2 + addCost1 + addCost2);
        if (ci1.index-ci2.index == 1 || ci1.index-ci2.index == -1) {
        	swapCost = swapCost + 2*Main.nodesDistance[customer1][customer2];
        }
        // System.out.println("Cost of swapping " + customer1 + " and " + customer2 + " : " + swapCost); // Debug
        // Checkout alternatives
        int costType2 = 0, costType3 = 0, costType4 = 0, costType5 = 0;
        // Type 2
        if (customer1nextnext != -1)    costType2 = Main.nodesDistance[customer1prev][customer1next] + Main.nodesDistance[customer2][customer1nextnext] - Main.nodesDistance[customer1prev][customer2] - Main.nodesDistance[customer1next][customer1nextnext];
        else  costType2 = Main.nodesDistance[customer1prev][customer1next] - Main.nodesDistance[customer1prev][customer2];
        // System.out.println("Cost of change, type 2 : " + costType2); // Debug
        if (costType2 < bestCost) {
            bestCost = costType2;
            type = 2;
        }  
        // Type 3
        if (customer1prevprev != -1)    costType3 = Main.nodesDistance[customer1prev][customer1next] + Main.nodesDistance[customer1prevprev][customer2] - Main.nodesDistance[customer2][customer1next] - Main.nodesDistance[customer1prevprev][customer1prev];
        else    costType3 = Main.nodesDistance[customer1prev][customer1next] - Main.nodesDistance[customer2][customer1next];
        // System.out.println("Cost of change, type 3 : " + costType2); // Debug
        if (costType3 < bestCost) {
            bestCost = costType3;
            type = 3;
        }  
        // Type 4
        if (customer2prevprev != -1)    costType4 = Main.nodesDistance[customer2prev][customer2next] + Main.nodesDistance[customer2prevprev][customer1] - Main.nodesDistance[customer1][customer2next] - Main.nodesDistance[customer2prevprev][customer2prev];
        else  costType4 = Main.nodesDistance[customer2prev][customer2next] - Main.nodesDistance[customer1][customer2next];
        // System.out.println("Cost of change, type 4 : " + costType2); // Debug
        if (costType4 < bestCost) {
            bestCost = costType4;
            type = 4;
        }  
        // Type 5
        if (customer2nextnext != -1)    costType5 = Main.nodesDistance[customer2prev][customer2next] + Main.nodesDistance[customer1][customer2nextnext] - Main.nodesDistance[customer2prev][customer1] - Main.nodesDistance[customer2next][customer2nextnext];
        else    costType5 = Main.nodesDistance[customer2prev][customer2next] - Main.nodesDistance[customer2prev][customer1];
        // System.out.println("Cost of change, type 5 : " + costType2); // Debug
        if (costType5 < bestCost) {
            bestCost = costType5;
            type = 5;
        }  
        int returnCost = (swapCost + bestCost)*10 + type;
        // System.out.println("Returned swap cost type : " + returnCost); // Debug
        return returnCost;
    }

    public CustomerIndex getRandomCustomer() {
        // Function to get a random customer from the solution.
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

    public void updateBestNeighbor() {
        // Generate Neighborhood logic here
        // Apply the move operator on the Solution to get to a better solution
        int iterations = 0, maxMoveIterations = 100; // Hyper-Parameter
        int maxIspIterations = 10 * Main.numCustomers; // Hyper-Parameter
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
                this.updateCost();
            } else {
                iterations++;
            }
            // Random customer relocated to the best location in the route
        }
        System.out.println("After improved move, solution : " + this.toString() + " with cost: " + this.getCost()); // Debug

        // Iterated Swap Procedure
        iterations = 0;
        while(iterations < maxIspIterations) {
            // System.out.println(this.routes + " at " + iterations); // Debug
            CustomerIndex ci1 = getRandomCustomer();
            CustomerIndex ci2 = getRandomCustomer();
            int swapCostType = getSwapCost(ci1,ci2);
            int swapCost = swapCostType/10;
            int type = swapCostType%10;
            if (this.solutionCost > swapCost) {
                // Swap the two customers
            	System.out.println("Found lower cost : " + swapCost + " and type : " + type); // Debug
                Route swapRoute1 = Main.routedCarparks.elementAt(ci1.routecp-Main.numNodes).route;
                Route swapRoute2 = Main.routedCarparks.elementAt(ci2.routecp-Main.numNodes).route;
                int customer1 = swapRoute1.route.elementAt(ci1.index);
                int customer2 = swapRoute2.route.elementAt(ci2.index);
                swapRoute1.setCustomer(customer2, ci1.index);
                swapRoute2.setCustomer(customer1, ci2.index);
                
                if (type == 2) {
                    swapRoute1.setCustomer(swapRoute1.route.elementAt(ci1.index+1), ci1.index);
                    swapRoute1.setCustomer(customer2, ci1.index+1);
                } else if (type == 3) {
                    swapRoute1.setCustomer(swapRoute1.route.elementAt(ci1.index-1), ci1.index);
                    swapRoute1.setCustomer(customer2, ci1.index-1);                    
                } else if (type == 4) {
                    swapRoute2.setCustomer(swapRoute2.route.elementAt(ci2.index-1), ci2.index);
                    swapRoute2.setCustomer(customer1, ci2.index-1);                    
                } else if (type == 5) {
                    swapRoute2.setCustomer(swapRoute2.route.elementAt(ci2.index+1), ci2.index);
                    swapRoute2.setCustomer(customer1, ci2.index+1);
                }
                this.updateCost();
                System.out.println("Updated Solution Cost : " + this.getCost()); // Debug
            } else {
                iterations++;
            }
        }
        System.out.println("After iterated swap procedure, solution : " + this.toString() + " with cost: " + this.getCost());
    }

    public Solution perturb() {
        // Perturb the local best found solution to get a new solution altogether
    	Solution perturbSoln = new Solution();
    	return perturbSoln;        
    }
}