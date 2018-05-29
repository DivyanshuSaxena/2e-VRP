package vrp;

import java.util.Vector;

// Class for the representation of the customers
class Customer {
    // This id is the id of the car park as in the overall nodes (This must be used for getting the distances)    
    int id;
    int demand;
    int assignedPark;
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
    Vector<Vector<Integer>> routes; 
    int totalDemand;
    public Carpark() {
        this.id = 0;
        this.totalDemand = 0;
        customers = new Vector<Customer>();
        routes = new Vector<Vector<Integer>>();
    }
    public void addCustomer(Customer c) {
        customers.add(c);
        totalDemand = totalDemand + c.demand;
    }
    public void removeCustomer(Customer c) {
        customers.remove(c);
        totalDemand = totalDemand - c.demand;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void updateRoutes() {
        // Function to update the routes of the current object based on the customers vector for the object
    }
}