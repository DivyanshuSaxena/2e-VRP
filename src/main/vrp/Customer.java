package vrp;

// Class for the representation of the customers
public class Customer {
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
