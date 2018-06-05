package vrp;

import java.util.Vector;
import java.util.Collections;

class Route {
    Vector<Integer> route;
    int routeCost;
    int demand;
    public Route() {
        route = new Vector<Integer>();
        routeCost = 0;
        demand = 0;
    }
    public String toString() {
        // Give the string representation for the Route object
        String route = "";
        for (int cust : this.route) {
            route = route + cust + ", ";
        }
        return route;
    }
    public void addCustomer(int id) {
        route.add(id);
        // Add the demand if the added node is a customer 
        if (id > Main.numCarpark) demand += Main.customers[id-Main.numCarpark-1].demand;
        if (route.size() > 1) routeCost += Main.nodesDistance[route.elementAt(route.size()-2)][route.elementAt(route.size()-1)];
    }
    public void addAllCustomers(Vector<Integer> v, int index) {
        // Add the vector v at index 'index' in the route vector
        int insertIndex = index;
        for (int cust : v) {
            this.route.add(insertIndex, cust);;
            if (cust > Main.numCarpark) demand += Main.customers[cust-Main.numCarpark-1].demand;
            // Conditions on whether there is a node after insert index or not, and what is the route size
            if (insertIndex >= route.size() -1 && insertIndex != 0) routeCost = routeCost + Main.nodesDistance[route.elementAt(insertIndex-1)][route.elementAt(insertIndex)];
            else if (insertIndex == 0 && insertIndex < route.size() - 1) routeCost = routeCost + Main.nodesDistance[route.elementAt(insertIndex)][route.elementAt(insertIndex+1)];
            else if (insertIndex > 0 && insertIndex < route.size() - 1) routeCost = routeCost - Main.nodesDistance[route.elementAt(insertIndex-1)][route.elementAt(insertIndex+1)] 
                        + Main.nodesDistance[route.elementAt(insertIndex)][route.elementAt(insertIndex+1)]
                        + Main.nodesDistance[route.elementAt(insertIndex-1)][route.elementAt(insertIndex)];         
            insertIndex++;
        }
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
            merged.addAllCustomers(r.route,this.route.size()); // Merge them
        } else if (startCust == r.route.elementAt(r.route.size()-2)) {
            r.route.remove(r.route.size()-1);   
            merged.addAllCustomers(r.route, 0);
            this.route.remove(0);   
            this.route.remove(0);   
            merged.addAllCustomers(this.route, r.route.size());
        } else if (startCust == r.route.elementAt(1)) {
            Collections.reverse(r.route);
            return (this.mergeRoute(r));
        } else if (endCust == r.route.elementAt(r.route.size()-2)) {
            Collections.reverse(r.route);
            return (this.mergeRoute(r));            
        }
        return merged;
    }
    public int getCost() {
        // Function to evaluate the total costs of the solution.
        // Note :- This function should include the infeasibility costs
        // Presently, no infeasibility costs are taken into account
           
        return 0;
    }
}
