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
    Vector<Route> routes; // Can be removed
    Vector<Integer> routeCarparks;
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
    // Can be removed
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

class CustomerIndex {
    int route;
    int routecp;
    int index;
}