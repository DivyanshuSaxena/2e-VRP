package vrp;

import java.util.Vector;

// Class for the representation of the customers
class Customer {
    int id; // This id is the id of the customer as in the overall nodes (This must be used for getting the distances).        
    int demand;
    int assignedPark; // This is the id of the carpark object that is assigned to this customer.
    boolean hasCar;
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
    public int getTotalCost() {
        int cost = 0;
        for (Route route : this.routes) {
            cost += route.getCost();
        }
        return cost;
    }
}

class RouteCarpark extends Carpark {
    int cpindex; // The index of the actual carpark
    Route route;
}

class Solution {
    Vector<Carpark> firstLevel;
    Vector<Route> routes;
    Solution() {
        firstLevel = new Vector<Carpark>();
        routes = new Vector<Route>();
    }
    public String toString() {
        String sol = "First Level: " + this.routes + "\n";
        for (Carpark cp : firstLevel) {
            sol += cp;
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
        for (Carpark cp : firstLevel) {
            cost += cp.getTotalCost();
        }
        // Add the infeasibility costs here.
        return cost;
    }
}