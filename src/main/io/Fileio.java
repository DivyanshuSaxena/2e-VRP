package io;
import java.util.*;
import vrp.*;

public class Fileio {
    Scanner sc;
    public Fileio(Scanner scanner) {
        sc = scanner;
    }
    public void setOneInput() {
        // Input parameters form the input file
        sc.nextLine(); // Name
        sc.nextLine(); // Comment
        sc.nextLine(); // Type
        Main.numNodes = Integer.parseInt(sc.nextLine().split(" ")[2]); //Dimension 
        Main.numCarpark = Integer.parseInt(sc.nextLine().split(" ")[2]); // Satellites
        Main.numCustomers = Integer.parseInt(sc.nextLine().split(" ")[2]); // Customers

        sc.nextLine(); // Edge Weight Type
        sc.nextLine(); // Fleet Section
        Main.l1cap = Integer.parseInt(sc.nextLine().split(" ")[2]); // L1 Cap
        Main.l2cap = Integer.parseInt(sc.nextLine().split(" ")[2]); // L2 Cap

        Main.numVehicles1 = Integer.parseInt(sc.nextLine().split(" ")[1]); // L1 Fleet
        Main.numVehicles2 = Integer.parseInt(sc.nextLine().split(" ")[1]); // L2 Fleet
        
        sc.nextLine(); // Edge Weight Section
        Main.nodesDistance = new double[Main.numNodes][Main.numNodes];
        Main.customers = new Customer[Main.numCustomers];
        Main.carparks = new Carpark[Main.numCarpark];

        // Creation of distances matrix from the input file
        for (int i = 0; i < Main.numNodes; i++) {
            for (int j = 0; j < Main.numNodes; j++) {
                if (i != j) Main.nodesDistance[i][j] = sc.nextInt();
                else {
                    sc.nextInt();
                    Main.nodesDistance[i][j] = 0;
                }
            }
        }
        // Empty Lines
        sc.nextLine();
        sc.nextLine();
        // Initialization of the customer array 
        sc.nextLine(); // Demand Section
        sc.nextLine(); // Demand of Main Depot	
        for(int i = 1; i < Main.numNodes; i++) {
            // i = 0 is reserved for the main depot
            if (i <= Main.numCarpark) {
            	sc.nextLine(); // Demand of ith carpark 
                Main.carparks[i-1] = new Carpark();
                Main.carparks[i-1].setId(i);
            } else {
                int offset = Main.numCarpark + 1;
                Main.customers[i-offset] = new Customer();
                Main.customers[i-offset].setId(i);
                int demand = Integer.parseInt(sc.nextLine().split(" ")[1]);
                Main.customers[i-offset].setDemand(demand);            
            }
        }
    }
    public void setTwoInput() {
        // Input parameters form the input file
        sc.nextLine(); // Name
        sc.nextLine(); // Comment
        sc.nextLine(); // Type
        Main.numNodes = Integer.parseInt(sc.nextLine().split(" ")[2]); //Dimension 
        Main.numCarpark = Integer.parseInt(sc.nextLine().split(" ")[2]); // Satellites
        Main.numCustomers = Integer.parseInt(sc.nextLine().split(" ")[2]); // Customers

        sc.nextLine(); // Edge Weight Type
        sc.nextLine(); // Fleet Section
        Main.l1cap = Integer.parseInt(sc.nextLine().split(" ")[2]); // L1 Cap
        Main.l2cap = Integer.parseInt(sc.nextLine().split(" ")[2]); // L2 Cap

        Main.numVehicles1 = Integer.parseInt(sc.nextLine().split(" ")[1]); // L1 Fleet
        Main.numVehicles2 = Integer.parseInt(sc.nextLine().split(" ")[1]); // L2 Fleet
        
        sc.nextLine(); // Node Coord Section
        Main.nodesDistance = new double[Main.numNodes][Main.numNodes];
        Main.customers = new Customer[Main.numCustomers];
        Main.carparks = new Carpark[Main.numCarpark];

        // Creation of distances matrix from the input file
        Main.x_coord = new Vector<Integer>(Main.numNodes);
        Main.y_coord = new Vector<Integer>(Main.numNodes);
        Main.x_coord.setSize(Main.numNodes);
        Main.y_coord.setSize(Main.numNodes);
        for (int i = 0; i <= Main.numCustomers; i++) {
            sc.nextInt(); // Node Number
            if (i == 0) {
                Main.x_coord.set(0, sc.nextInt());
                Main.y_coord.set(0, sc.nextInt());
            } else {
                Main.x_coord.set(i+Main.numCarpark, sc.nextInt());
                Main.y_coord.set(i+Main.numCarpark, sc.nextInt());                
            }
        }
        sc.nextLine(); // Empty Line
        sc.nextLine(); // Satellite Section
        for (int i = 0; i < Main.numCarpark; i++) {
            sc.nextInt(); // Node Number
            Main.x_coord.set(i+1, sc.nextInt());
            Main.y_coord.set(i+1, sc.nextInt());
            Main.carparks[i] = new Carpark();
            Main.carparks[i].setId(i+1);
        }
        sc.nextLine(); // Empty Line
        for (int i = 0; i < Main.numNodes; i++) {
            for (int j = i+1; j < Main.numNodes; j++) {
                int xcoordi = Main.x_coord.elementAt(i);
                int xcoordj = Main.x_coord.elementAt(j);
                int ycoordi = Main.y_coord.elementAt(i);
                int ycoordj = Main.y_coord.elementAt(j);
                Main.nodesDistance[i][j] = Math.sqrt((xcoordi-xcoordj)*(xcoordi-xcoordj) + (ycoordi-ycoordj)*(ycoordi-ycoordj));
                Main.nodesDistance[j][i] = Main.nodesDistance[i][j];
            }
        }

        // Initialization of the customer array 
        sc.nextLine(); // Demand Section
        sc.nextLine(); // Demand of Main Depot	
        for(int i = 1; i <= Main.numCustomers; i++) {
            // i = 0 is reserved for the main depot
            Main.customers[i-1] = new Customer();
            Main.customers[i-1].setId(i+Main.numCarpark);
            int demand = Integer.parseInt(sc.nextLine().split(" ")[1]);
            Main.customers[i-1].setDemand(demand);            
        }
    }
}