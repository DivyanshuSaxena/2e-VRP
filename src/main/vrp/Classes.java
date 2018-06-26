package vrp;

import java.util.Stack;
import java.util.Vector;

// Class for the representation of the customers
class Customer {
    int id; // This id is the id of the customer as in the overall nodes (This must be used for getting the distances).        
    int demand;
    int assignedPark; // This is the id of the carpark object that is assigned to this customer.
    boolean hasCar;  // Has not been used yet
    public void setId(int id) {
        this.id = id;
    }
    public void setDemand(int demand) {
        this.demand = demand;
    }
}

class Carpark {
    // This id is the id of the car park as in the overall nodes (This must be used for getting the distances)
    int id; 
    Vector<Customer> customers;
    Vector<Route> routes;
    Vector<Integer> vehicles;
    int totalDemand;
    public Carpark() {
        this.id = 0;
        this.totalDemand = 0;
        customers = new Vector<Customer>();
        routes = new Vector<Route>();
    }
    public String toString() {
        String cp = this.id + " : ";
        for (Route r : this.routes) {
            cp = cp + "[" + r + "],\n";
        }
        return cp;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void addCustomer(Customer c) {
        customers.add(c);
        totalDemand = totalDemand + c.demand;
    }
    public void removeCustomer(Customer c) {
        customers.remove(c);
        totalDemand = totalDemand - c.demand;
    }
}

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

class GiantRoute {
    Vector<Integer> giantRoute;
    int cost;
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
                    Vehicle v = Main.routedCarparks.elementAt(index);
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
        return solution;
    }
    public int getCustomerRemovalCost(int customer) {
        int rcost = 0;
        for (int i = 0; i < this.giantRoute.size(); i++) {
            if (this.giantRoute.elementAt(i) == customer) {
                rcost = Main.nodesDistance[giantRoute.elementAt(i-1)][giantRoute.elementAt(i)] + Main.nodesDistance[giantRoute.elementAt(i)][giantRoute.elementAt(i+1)];
                break;
            }
        }
        return rcost;
    }
    public boolean isInsertionFeasible(int customer, int index) {
        int vehicleDemand = Main.customers[customer-Main.numCarpark-1].demand;
        for (int i = index-1; i > 0; i--) {
            int node = giantRoute.elementAt(i); 
            if (node > Main.numCarpark) {
                vehicleDemand += Main.customers[node-Main.numCarpark-1].demand;
            } else break;
        }
        for (int i = index; i < giantRoute.size(); i++) {
            int node = giantRoute.elementAt(i); 
            if (node > Main.numCarpark) {
                vehicleDemand += Main.customers[node-Main.numCarpark-1].demand;
            } else break;
        }
        // System.out.println("Demand :" + vehicleDemand); // Debug
        return (vehicleDemand <= Main.l2cap);
    }
    public void insertAtBestLocation(int customer) {
        int lastCarpark = 0, bestCost = 0, bestIndex = 0;
        for (int i = 0; i < this.giantRoute.size(); i++) {
            int node = giantRoute.elementAt(i);
            if (node != 0 && lastCarpark == 0) {
                // Insertion is possible at index (i+1)
                int addCost = Main.nodesDistance[node][customer] + Main.nodesDistance[customer][giantRoute.elementAt(i+1)];
                if (bestCost == 0 && this.isInsertionFeasible(customer, i+1))  {
                    bestCost = addCost;
                    bestIndex = i+1;
                } else if (addCost < bestCost && this.isInsertionFeasible(customer, i+1)) {
                    bestCost = addCost;
                    bestIndex = i+1;
                }
            } 
            if (node <= Main.numCarpark && node != 0)   lastCarpark = lastCarpark==0?node:0;                
        }
        this.addCustomer(customer, bestIndex);
        // System.out.println("Giant Route after regret insertion : " + this.giantRoute); // Debug
    }
    public int getRegretCost(int customer) {
        int lastCarpark = 0, best = 0, secondBest = 0;
        for (int i = 0; i < this.giantRoute.size(); i++) {
            if (giantRoute.elementAt(i) != 0 && lastCarpark == 0) {
                // Insertion is possible at index (i+1)
                int addCost = Main.nodesDistance[giantRoute.elementAt(i)][customer] + Main.nodesDistance[customer][giantRoute.elementAt(i+1)];
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
        int index = this.giantRoute.indexOf(customer);
        this.cost -= (Main.nodesDistance[giantRoute.elementAt(index-1)][giantRoute.elementAt(index)] + Main.nodesDistance[giantRoute.elementAt(index)][giantRoute.elementAt(index+1)]);
        this.giantRoute.remove((Integer)customer);
    }
    public void addCustomer(int customer, int index) {
        this.cost += (Main.nodesDistance[giantRoute.elementAt(index-1)][customer] + Main.nodesDistance[customer][giantRoute.elementAt(index)]);
        this.giantRoute.add(index, customer);
    }
}