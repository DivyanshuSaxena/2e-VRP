package vrp;

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
    public void addRoute(Route route) {
        for (int cust : route.route) {
            addCustomer(Main.customers[cust-Main.numCarpark-1]);
        }
        this.routes.add(route);
    }
}

class RouteCarpark {
    int cpindex; // The index of the actual carpark
    Route route;
    RouteCarpark() {
        cpindex = 0;
        route = new Route();
    }
    RouteCarpark(int index, Route route) {
        cpindex = index;
        this.route = route;
    }
}

class Solution {
    Vector<Route> routes;
    Solution() {
        routes = new Vector<Route>();
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
        return cost;
    }
}