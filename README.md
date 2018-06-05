# Network-Design
Repository for the implementation of a solution to the modified Two Echelon Vehicle Routing Problem with Transfer Optimizations.

## Problem Statement
The classical Vehicle Routing Problem (VRP) is a combinatorial optimization problem that is NP-hard. It consists in designing the optimal set of routes for fleet of vehicles in order to serve a given set of customers.  
The Two Echelon Vehicle Routing Problem (2E-VRP), is an advanced version of the classical Vehicle Routing Problem. It involves the delivery of goods to customers in a two tier fashion - first, from the main depot to certain satellite locations (warehouses) and second, from these warehouses to the customers. Separate fleets are used for the two tiers. Hence, it at least as hard as the classical VRP.   
The problem, being targeted in the repository, is a modified version of the two echelon VRP. In the current statement, instead of warehouses, we have car-parks, where the goods can only be transferred from the first level vehicle to the second level vehicle. Hence, no storage is possible and transfer optimization is needed to be done.  

## Structure
The directory provides a Java package for running the code.  
The package is placed inside the src/main folder. The directory structure is as follows:  
Network-Design:  
├───bin  
│   ├───test  
│   └───vrp  
├───files  
└───src  
    ├───main  
    │   └───vrp  
    └───test  

The source code is placed in main/vrp/ and the compiled .CLASS files are placed in bin/vrp/.  
