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
                } else if (stack.size() == 0) {
                    stack.push(0);
                }
            }
        }
        solution.updateCost();
        return solution;
    }
}