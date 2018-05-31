package vrp;

import java.util.Vector;

import javax.sound.midi.VoiceStatus;

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
    int id; // This id is the id of the car park as in the overall nodes (This must be used for getting the distances)
    Vector<Customer> customers;
    Vector<Route> routes; 
    int totalDemand;
    public Carpark() {
        this.id = 0;
        this.totalDemand = 0;
        customers = new Vector<Customer>();
        routes = new Vector<Route>();
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
    public void getInitialRoutes() {
        // Function to get the initial routes for the current carpark object based on the customers vector
        // Using Clarke and Wright's Savings Algorithm

    }
}

class Route {
    Vector<Integer> route;
    int routeCost;
    int demand;
    public Route() {
        route = new Vector<Integer>();
        routeCost = 0;
        demand = 0;
    }
    public void addCustomer(int id) {
        route.add(id);
        demand += Main.customers[id-Main.numCarpark-1].demand;
        routeCost += Main.nodesDistance[route.elementAt(route.size()-2)][route.elementAt(route.size()-1)];
    }
    public void addAllCustomers(Vector<Integer> v, int index) {

    }
    public int positionOf(int customer) {
        // This function gives the position of the customer in the route.
        // It returns -2 if the customer is at the begin or end of the route
        // And returns simple index otherwise
        int index = this.route.indexOf(customer);
        if (index == 1) {
            index = -2;
        } else if (index == route.size()-2) {
            index = -2;
        }
        return index;
    }
    public Route mergeRoute(Route r) {
        // Function to return the merged rotue with the current route
        Route merged = new Route();
        int startCust = this.route.elementAt(1);
        int endCust = this.route.elementAt(this.route.size()-2);
        if (endCust == r.route.elementAt(1)) {
            this.route.remove(this.route.size()-1);
            merged.addAllCustomers(this.route, 0); // Add the current route
            r.route.remove(0);
            r.route.remove(0); // Trim the new route
            merged.addAllCustomers(r.route,this.route.size()-1); // Merge them
        } else if (startCust == r.route.elementAt(r.route.size()-2)) {
            r.route.remove(r.route.size()-1);   
            merged.addAllCustomers(r.route, 0);
            this.route.remove(0);   
            this.route.remove(0);   
            merged.addAllCustomers(this.route, r.route.size()-1);
        }
        return merged;
    }
}

class Solution {
    Vector<Carpark> firstLevel;
    Vector<Route> routes;
    public void updateRoutes() {
        // Function to update the routes for the first level routes
    }
    public int getCost() {
        // Function to evaluate the total costs of the solution.
        // Note :- This includes the infeasibility costs
        return 0;
    }
}