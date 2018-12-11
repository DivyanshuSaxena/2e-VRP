package vrp;

import java.util.*;

class Vehicle {
    int cpindex; // The index of the actual carpark
    Route route;
    Vehicle() {
        cpindex = 0;
        route = new Route();
    }
    Vehicle(int index, Route route) {
        cpindex = index;
        this.route = route;
    }
}

class CustomerIndex {
    int route;
    int routecp;
    int index;
    public boolean isSameRoute(CustomerIndex ci) {
        return (ci.route==this.route && ci.routecp==this.routecp);
    }
}

class SolutionIterator implements Iterator<CustomerIndex> {
    int route = 0;
    int routecp = 1;
    int customerIndex = 1;
    Solution solution;
    public SolutionIterator(Solution solnobj) {
        solution = solnobj;
    }
    public boolean hasNext() {
        return (route < solution.routes.size());
    }
    public CustomerIndex next() {
        CustomerIndex cinext = new CustomerIndex();
        // System.out.println("Finding the next for " + solution + " at " + route + " " + routecp + " " + customerIndex); // Debug
        if (hasNext()) {
            // System.out.println("Next is available"); // Debug
            Route firstLevel = solution.routes.elementAt(route);
            cinext.route = route;
            cinext.routecp = firstLevel.route.elementAt(routecp);
            cinext.index = customerIndex;
            customerIndex++;
            Vehicle vehicle = Main.vehicles.elementAt(firstLevel.route.elementAt(routecp)-Main.numNodes);
            if (customerIndex >= vehicle.route.route.size()-1) {
                routecp++;
                customerIndex = 1;
                if (routecp >= firstLevel.route.size()-1) {
                    route++;
                    routecp = 1;
                }
            }
            return cinext;
        }
        throw new NoSuchElementException();
    }
    public void reset() {
        route = 0;
        routecp = 1;
        customerIndex = 1;    
    }
}

class GiantRoute {
    Vector<Integer> giantRoute;
    double cost;

    public GiantRoute() {
        giantRoute = new Vector<Integer>();
        cost = 0;
    }

    public Solution getSolution() {
        Solution solution = new Solution();
        Stack<Integer> stack = new Stack<Integer>();
        Vector<Integer> customerRoute = new Vector<Integer>();
        Vector<Integer> carparkRoute = new Vector<Integer>();
        int index = 0;
        for (int node : giantRoute) {
            if (node > Main.numCarpark) {
                // A customer on node
                customerRoute.add(node);
            } else if (node != 0) {
                // A carpark on node
                if (stack.size() != 0 && stack.peek() == node) {
                    Vehicle v = Main.vehicles.elementAt(index);
                    v.route.removeAllCustomers(1, v.route.route.size()-2);
                    v.route.addAllCustomers(customerRoute, 1);
                    v.cpindex = stack.pop();
                    v.route.setStart(v.cpindex);
                    v.route.setEnd(v.cpindex);
                    customerRoute = new Vector<Integer>();
                    carparkRoute.add(index + Main.numNodes);
                    index++;
                } else {
                    stack.push(node);
                }
            } else {
                // Main depot on the node
                carparkRoute.add(0);
                if (stack.size() != 0 && stack.peek() == 0) {
                    Route route = new Route();
                    route.addAllCustomers(carparkRoute, 0);
                    solution.routes.add(route);
                    carparkRoute = new Vector<Integer>();
                    stack.pop();
                } else if (stack.size() == 0) {
                    stack.push(0);
                }
            }
        }
        // System.out.println(solution); // Debug
        solution.updateCost();
        this.cost = solution.solutionCost;
        return solution;
    }

    public double updateCost() {
        this.getSolution();
        return this.cost;
    }

    public double getCustomerRemovalCost(int customer) {
        double rcost = 0;
        for (int i = 0; i < this.giantRoute.size(); i++) {
            if (this.giantRoute.elementAt(i) == customer) {
                rcost = Main.nodesDistance[giantRoute.elementAt(i-1)][giantRoute.elementAt(i)] + Main.nodesDistance[giantRoute.elementAt(i)][giantRoute.elementAt(i+1)];
                break;
            }
        }
        return rcost;
    }

    private boolean isInsertionFeasible(int customer, int index) {
        int vehicleDemand = Main.customers[customer-Main.numCarpark-1].demand;
        int routeDemand = vehicleDemand;
        boolean level2 = false; 
        for (int i = index-1; i > 0; i--) {
            int node = giantRoute.elementAt(i);
            if (!level2) {
                if (node > Main.numCarpark) {
                    vehicleDemand += Main.customers[node-Main.numCarpark-1].demand;
                } else {
                    level2 = true;
                } 
            } 

            if (node != 0) {
                if (node > Main.numCarpark)
                    routeDemand += Main.customers[node-Main.numCarpark-1].demand;
            } else break;
        }
        level2 = false;
        for (int i = index; i < giantRoute.size(); i++) {
            int node = giantRoute.elementAt(i); 

            if (!level2) {
                if (node > Main.numCarpark) {
                    vehicleDemand += Main.customers[node-Main.numCarpark-1].demand;
                } else {
                    level2 = true;
                } 
            } 

            if (node != 0) {
                if (node > Main.numCarpark)
                    routeDemand += Main.customers[node-Main.numCarpark-1].demand;
            } else break;
        }
        boolean l2feasibility = vehicleDemand <= Main.l2cap;
        boolean l1feasibility = routeDemand <= Main.l1cap;
        return l1feasibility && l2feasibility;
    }

    public void insertAtBestLocation(int customer) {
        int lastCarpark = 0, bestIndex = -1;
        double bestCost = 0.0;
        for (int i = 0; i < this.giantRoute.size(); i++) {
            int node = giantRoute.elementAt(i);
            if (node != 0 && lastCarpark == 0) {
                // Insertion is possible at index (i+1)
                int nextNode = giantRoute.elementAt(i+1);
                double addCost = Main.nodesDistance[node][customer] + Main.nodesDistance[customer][nextNode] - Main.nodesDistance[node][nextNode];
                if (bestCost == 0 && this.isInsertionFeasible(customer, i+1))  {
                    bestCost = addCost;
                    bestIndex = i+1;
                } else if (addCost < bestCost && this.isInsertionFeasible(customer, i+1)) {
                    bestCost = addCost;
                    bestIndex = i+1;
                }
            } 
            if (node <= Main.numCarpark && node != 0)   
                lastCarpark = lastCarpark==0 ? node : 0;                
        }
        // System.out.println("Found best location for " + customer + " at " + bestIndex); // Debug
        if (bestIndex != -1) {
            this.addCustomer(customer, bestIndex);
        } else {
            // No Suitable vehicle found, where the customer can be assigned. Allot to a new vehicle.
            System.out.println("ALLOT A NEW VEHICLE");
            int minDistanceCarpark = 0;
            double minDistance = 0;
            for (Carpark cp : Main.carparks) {
                if (Main.nodesDistance[customer][cp.id] < minDistance || minDistance == 0) {
                    minDistance = Main.nodesDistance[customer][cp.id];
                    minDistanceCarpark = cp.id;
                }
            }
            this.addNewVehicle(customer, minDistanceCarpark);
        }
    }

    private void addNewVehicle(int customer, int carpark) {
        // Add a new vehicle in a route which has the carpark as a location. Add the customer to the vehicle.
        System.out.println("Before adding vehicle containing " + customer + " at " + carpark + " : " + this.giantRoute); // Debug
        Vector<Integer> route = new Vector<Integer>();
        route.add(carpark);
        route.add(customer);
        route.add(carpark);
        Route r = new Route();
        r.addAllCustomers(route, 0);
        Vehicle newVehicle = new Vehicle(carpark, r);
        Main.vehicles.add(newVehicle);

        // Route constructed for the customer at the given carpark. Now, add the route at a suitable position.
        int vehicleDemand = Main.customers[customer-Main.numCarpark-1].demand;
        int routeDemand = Main.l1cap+1;
        int cpNumber = 0;
        int index = -1;
        boolean flag = false;
        for (int i = 0; i < this.giantRoute.size(); i++) {
            int node = giantRoute.elementAt(i);
            if (node != 0) {
                if (node > Main.numCarpark) {
                    // Node is a customer. Add its demand in the route demand.
                    routeDemand += Main.customers[node-Main.numCarpark-1].demand;
                } else {
                    // Node is a carpark. Check if it is the desired carpark. 
                    cpNumber++;
                    if (cpNumber%2 == 0) {
                        if (node == carpark) {
                            index = i;
                            routeDemand += vehicleDemand;
                        }
                    }
                }
            } else {
                if (index != -1 && routeDemand <= Main.l1cap) {
                    System.out.println(routeDemand + " " + index);
                    flag = true;
                    break;
                }
                routeDemand = 0;
            }
        }

        if (flag) {
            System.out.println("Found a valid insertion point at " + index); // Debug
            this.giantRoute.addAll(index+1, route);  
        } else {
            System.out.println("ADD FIRST LEVEL ROUTE");
            Vector<Integer> firstLevel = new Vector<Integer>();
            firstLevel.add(0);
            firstLevel.addAll(route);
            firstLevel.add(0);
            this.giantRoute.addAll(firstLevel);
        }
        System.out.println("After adding "+ this.giantRoute); // Debug
    }

    public double getRegretCost(int customer) {
        int lastCarpark = 0;
        double best = 0.0, secondBest = 0.0;
        for (int i = 0; i < this.giantRoute.size(); i++) {
            if (giantRoute.elementAt(i) != 0 && lastCarpark == 0) {
                // Insertion is possible at index (i+1)
                double addCost = Main.nodesDistance[giantRoute.elementAt(i)][customer] + Main.nodesDistance[customer][giantRoute.elementAt(i+1)];
                if (best == 0)  best = addCost;
                else if (best > addCost) {
                    secondBest = best;
                    best = addCost;
                }
            }
        }
        return (best - secondBest);
    }

    public void removeCustomer(int customer) {
        this.giantRoute.remove((Integer)customer);
    }

    private void addCustomer(int customer, int index) {
        this.giantRoute.add(index, customer);
    }

    public void removeUnusedCarparks() {
        int currNode = giantRoute.elementAt(2);
        int prevNode = giantRoute.elementAt(1);
        int prevPrevNode = giantRoute.elementAt(0);
        for (int i = 2; i < giantRoute.size(); i++) {
            currNode = giantRoute.elementAt(i);
            if (currNode == prevNode && prevPrevNode == 0) {
                giantRoute.remove(i);
                giantRoute.remove(--i);
                if (i < giantRoute.size() && giantRoute.elementAt(i) == 0) {
                    giantRoute.remove(i);
                    giantRoute.remove(--i);
                }
                i--;
            } else if (currNode == prevNode && currNode == prevPrevNode) {
                giantRoute.remove(i);
                giantRoute.remove(--i);
                i--;
            }
            prevPrevNode = giantRoute.elementAt(i-1);
            prevNode = giantRoute.elementAt(i);
        }
    }
}