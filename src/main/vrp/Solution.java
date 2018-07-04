package vrp;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

// Class to represent the solution
class Solution implements Iterable<CustomerIndex> {
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
    public Iterator<CustomerIndex> iterator() {
        return new SolutionIterator(this);
    }
    public GiantRoute getGiantRoute() {
        GiantRoute gr = new GiantRoute();
        Vector<Integer> giantRoute = new Vector<Integer>();
        for (Route route : this.routes) {
            for (int depotIndex : route.route) {
                if (depotIndex < Main.numNodes) giantRoute.add(depotIndex);
                else {
                    for (int customer : Main.routedCarparks.elementAt(depotIndex-Main.numNodes).route.route) {
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
    public double updateCost() {
        // Function to evaluate the total costs of the solution.
        double cost = 0;
        for (Route route : this.routes) {
            cost += route.getCost();
        }
        for (Vehicle cp : Main.routedCarparks) {
            cost += cp.route.getCost();
        }
        // Add the infeasibility costs here.
        this.solutionCost = cost;
        return cost;
    }
    public SwapCostType getSwapCost(CustomerIndex ci1, CustomerIndex ci2) {
        // This function returns the best swap neighbour obtained on swapping customers at ci1 and ci2.
        Route swapRoute1 = Main.routedCarparks.elementAt(ci1.routecp-Main.numNodes).route;
        Route swapRoute2 = Main.routedCarparks.elementAt(ci2.routecp-Main.numNodes).route;
        int type = 1;
        double swapCost = 0.0, bestCost = 0.0;
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
        double subtractCost1 = Main.nodesDistance[customer1][customer1next] + Main.nodesDistance[customer1prev][customer1];
        double subtractCost2 = Main.nodesDistance[customer2][customer2next] + Main.nodesDistance[customer2prev][customer2];
        double addCost1 = Main.nodesDistance[customer2][customer1next] + Main.nodesDistance[customer1prev][customer2];
        double addCost2 = Main.nodesDistance[customer1][customer2next] + Main.nodesDistance[customer2prev][customer1];
        swapCost = (this.solutionCost - subtractCost1 - subtractCost2 + addCost1 + addCost2);
        if (ci1.index-ci2.index == 1 || ci1.index-ci2.index == -1) {
        	swapCost = swapCost + 2*Main.nodesDistance[customer1][customer2];
        }
        // System.out.println("Cost of swapping " + customer1 + " and " + customer2 + " : " + swapCost); // Debug
        // Checkout alternatives
        double costType2 = 0, costType3 = 0, costType4 = 0, costType5 = 0;
        if (customer1nextnext != -1) {
            costType2 = Main.nodesDistance[customer1prev][customer1next] + Main.nodesDistance[customer2][customer1nextnext] - Main.nodesDistance[customer1prev][customer2] - Main.nodesDistance[customer1next][customer1nextnext];
            if (costType2 < bestCost) {
                bestCost = costType2;
                type = 2;
            }  
        }   
        // System.out.println("Cost of change, type 2 : " + costType2); // Debug
        if (customer1prevprev != -1) {
            costType3 = Main.nodesDistance[customer1prev][customer1next] + Main.nodesDistance[customer1prevprev][customer2] - Main.nodesDistance[customer2][customer1next] - Main.nodesDistance[customer1prevprev][customer1prev];
            if (costType3 < bestCost) {
                bestCost = costType3;
                type = 3;
            }  
        }   
        // System.out.println("Cost of change, type 3 : " + costType2); // Debug
        if (customer2prevprev != -1) {
            costType4 = Main.nodesDistance[customer2prev][customer2next] + Main.nodesDistance[customer2prevprev][customer1] - Main.nodesDistance[customer1][customer2next] - Main.nodesDistance[customer2prevprev][customer2prev];
            if (costType4 < bestCost) {
                bestCost = costType4;
                type = 4;
            } 
        }   
        // System.out.println("Cost of change, type 4 : " + costType2); // Debug
        if (customer2nextnext != -1) {
            costType5 = Main.nodesDistance[customer2prev][customer2next] + Main.nodesDistance[customer1][customer2nextnext] - Main.nodesDistance[customer2prev][customer1] - Main.nodesDistance[customer2next][customer2nextnext];
            if (costType5 < bestCost) {
                bestCost = costType5;
                type = 5;
            } 
        }   
        // System.out.println("Cost of change, type 5 : " + costType2); // Debug
        SwapCostType sct = new SwapCostType();
        sct.swapCost = swapCost + bestCost;
        sct.type = type;
        return sct;
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
        Vehicle chosenCarpark = Main.routedCarparks.elementAt(cIndex.routecp-Main.numNodes);
        in = (int)(Math.random() * (chosenCarpark.route.route.size()-2));
        cIndex.index = (1 + in); // Index of the randomly chosen customer in the route 
        // System.out.println(in + " at " + chosenCarpark.route); // Debug
        return cIndex; 
    }
    public int getRandomVehicle() {
        int index = (int)((Main.routedCarparks.size()+1) * Math.random());
        if (index == Main.routedCarparks.size()+1)  return 0;
        return index;
    }
    private boolean moveOperator(CustomerIndex ci) {
        boolean improvement = false;
        Route clonedRoute = Main.routedCarparks.elementAt(ci.routecp-Main.numNodes).route; // Index of the selected vehicle
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
            if (bestCost > newCost) {
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
            if (bestIndex < ci.index)   clonedRoute.removeCustomer(ci.index+1);
            else    clonedRoute.removeCustomer(ci.index);
            Main.routedCarparks.elementAt(vehicleIndex-Main.numNodes).route = clonedRoute;
            improvement = true;
            this.updateCost();
        }
        // Random customer relocated to the best location in the route
        return improvement;
    }
    private boolean iteratedSwapOperator(CustomerIndex ci1, CustomerIndex ci2) {
        boolean improvement = false;
        SwapCostType swapCostType = getSwapCost(ci1,ci2);
        double swapCost = swapCostType.swapCost;
        int type = swapCostType.type;
        if (this.solutionCost > swapCost) {
            // Swap the two customers
            // System.out.println("Found lower cost : " + swapCost + " and type : " + type); // Debug
            Route swapRoute1 = Main.routedCarparks.elementAt(ci1.routecp-Main.numNodes).route;
            Route swapRoute2 = Main.routedCarparks.elementAt(ci2.routecp-Main.numNodes).route;
            int customer1 = swapRoute1.route.elementAt(ci1.index);
            int customer2 = swapRoute2.route.elementAt(ci2.index);
            if (swapRoute1.isSwapFeasible(customer1, customer2) && swapRoute2.isSwapFeasible(customer2, customer1)) {
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
                improvement = true;
                this.updateCost();
            } else {
                // All infeasible but good quality solutions are received here
                // System.out.println("Infeasible Solution");
            }
        }
        return improvement;
    }
    private boolean exchangeOperator(CustomerIndex ci1, CustomerIndex ci2) {
        boolean improvement = false;
        if (ci1.routecp==ci2.routecp) {
            return improvement;
        }
        Route exchangeRoute1 = Main.routedCarparks.elementAt(ci1.routecp-Main.numNodes).route;
        Route exchangeRoute2 = Main.routedCarparks.elementAt(ci2.routecp-Main.numNodes).route;
        int customer1 = exchangeRoute1.route.elementAt(ci1.index);
        int customer1prev = exchangeRoute1.route.elementAt(ci1.index-1);
        int route1last = exchangeRoute1.route.elementAt(exchangeRoute1.route.size()-2);
        int route1depot = Main.routedCarparks.elementAt(ci1.routecp-Main.numNodes).cpindex;
        int customer2 = exchangeRoute2.route.elementAt(ci2.index);
        int customer2prev = exchangeRoute2.route.elementAt(ci2.index-1);
        int route2last = exchangeRoute2.route.elementAt(exchangeRoute2.route.size()-2);
        int route2depot = Main.routedCarparks.elementAt(ci2.routecp-Main.numNodes).cpindex;
        double addCost1 = Main.nodesDistance[customer1prev][customer2] + Main.nodesDistance[customer2prev][customer1] + Main.nodesDistance[route2last][route1depot] + Main.nodesDistance[route1last][route2depot];
        double subtractCost1 = Main.nodesDistance[customer1prev][customer1] + Main.nodesDistance[customer2prev][customer2] + Main.nodesDistance[route1last][route1depot] + Main.nodesDistance[route2last][route2depot];
        // Exchange the customers if better.
        if (addCost1-subtractCost1 < 0) {
            Vector<Integer> seg1 = exchangeRoute1.getSubRoute(ci1.index, exchangeRoute1.route.size()-2);
            Vector<Integer> seg2 = exchangeRoute2.getSubRoute(ci2.index, exchangeRoute2.route.size()-2);
            if (exchangeRoute1.isExchangeFeasible(seg2, ci1.index) && exchangeRoute2.isExchangeFeasible(seg1, ci2.index)) {
                // System.out.println("Exchanging " + customer1 + " from " + exchangeRoute1 + " with " + customer2 + " from " + exchangeRoute2); // Debug
                exchangeRoute1.addAllCustomers(seg2, ci1.index);
                exchangeRoute2.addAllCustomers(seg1, ci2.index);
                exchangeRoute1.removeAllCustomers(ci1.index+seg2.size(), exchangeRoute1.route.size()-2);
                exchangeRoute2.removeAllCustomers(ci2.index+seg1.size(), exchangeRoute2.route.size()-2);    
                // System.out.println("New Routes : " + exchangeRoute1 + " and " + exchangeRoute2); // Debug
                improvement = true;
                this.updateCost();
                // System.out.println("Updated Cost : " + this.solutionCost); // Debug
            } 
        }
        return improvement;
    }
    public boolean updateBestNeighbor() {
        // Generate Neighborhood logic here
        // Apply the move operator on the Solution to get to a better solution
        boolean improvement = false;
        int iterations = 0;
        int ispIterations = Main.numCustomers; // Hyper-Parameter
        SolutionIterator iter = new SolutionIterator(this);
        while (iter.hasNext()) {
            CustomerIndex ci = iter.next();
            improvement = this.moveOperator(ci);
        }
        if (improvement) System.out.println("After improved move, solution cost: " + this.getCost()); // Debug

        // Iterated Swap Procedure
        iter.reset();
        while (iter.hasNext()) {
            CustomerIndex ci1 = iter.next();
            SolutionIterator innerIterator = new SolutionIterator(this);
            while (innerIterator.hasNext()) {
                CustomerIndex ci2 = innerIterator.next();
                improvement = improvement || this.iteratedSwapOperator(ci1, ci2);
            }
        }
        // iterations = 0;
        // while(iterations < ispIterations) {
        //     CustomerIndex ci1 = getRandomCustomer();
        //     CustomerIndex ci2 = getRandomCustomer();
        //     boolean ispimprove = this.iteratedSwapOperator(ci1, ci2);
        //     if (!ispimprove) {
        //         iterations++;
        //     }
        //     improvement = improvement || ispimprove;
        // }
        if (improvement) System.out.println("After iterated swap procedure, solution cost: " + this.getCost());
        
        // Segment Exchange Operator
        iter.reset();
        while (iter.hasNext()) {
            CustomerIndex ci1 = iter.next();
            SolutionIterator innerIterator = new SolutionIterator(this);
            while (innerIterator.hasNext()) {
                CustomerIndex ci2 = innerIterator.next();
                improvement = improvement || this.exchangeOperator(ci1, ci2);
            }
        }
        if (improvement) System.out.println("After exchange operator, solution cost: " + this.getCost());
        return improvement;
    }
    public Solution perturb() {
        // Perturb the local best found solution to get a new solution altogether
        final GiantRoute gr = this.getGiantRoute();
        Solution perturbSoln = new Solution();
        
        // Worst Removal
        Vector<Integer> customerPool = new Vector<Integer>(); // Holds the Customer Ids that have been removed 
        Vector<Double> normRemovalCost = new Vector<Double>();
        int q = 5;
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
        
        // Regret Insertion
        customerPool.sort(new Comparator<Integer>() {
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
        System.out.println("Customer Pool : " + customerPool); // Debug
        int count = 0;
        while (count < q) {
            int customer = customerPool.elementAt(count);
            gr.insertAtBestLocation(customer);
            count++;
        }
        // Check the solution for any removed carparks
        gr.removeUnusedCarparks();
        // System.out.println("Giant Route after regret insertion : " + gr.giantRoute); // Debug
        perturbSoln = gr.getSolution();
        return perturbSoln;        
    }
}