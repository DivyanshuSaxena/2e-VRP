# Network-Design
Repository for the implementation of a solution to the modified Two Echelon Vehicle Routing Problem with Transfer Optimizations.

## Problem Statement
The classical Vehicle Routing Problem (VRP) is a combinatorial optimization problem that is NP-hard. It consists in designing the optimal set of routes for fleet of vehicles in order to serve a given set of customers.  
The Two Echelon Vehicle Routing Problem (2E-VRP), is an advanced version of the classical Vehicle Routing Problem. It involves the delivery of goods to customers in a two tier fashion - first, from the main depot to certain satellite locations (warehouses) and second, from these warehouses to the customers. Separate fleets are used for the two tiers. Hence, it at least as hard as the classical VRP.   
The problem, being targeted in the repository, is a modified version of the two echelon VRP. In the current statement, instead of warehouses, we have car-parks, where the goods can only be transferred from the first level vehicle to the second level vehicle. Hence, no storage is possible and transfer optimization is needed to be done.

## Solution Method
The solution method that is followed for the current problem, makes use of a local search heuristic on a solution based improvement method.  
The initial solution is obtained by applying the Clarke and Wright's Savings Algorithm on the two levels (or tiers) separately. This initial solution is then, repeatedly improved using the local search heuristic, involving the move, iterated swap and segment exchange operators.  
Once, the local optimum has been obtained, the solution space, is then, explored by a perturb and shake operator on the obtained best Solution.  
This process of improvement and perturbation is iterated several times, so as to yield the global best solution.

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

## Assumptions
1. The goods are to be delivered in a to tier hierarchy. First, the level 1 vehicles shall take the goods to certain carparks, where the goods shall be transferred into the smaller vehicles. These smaller vehicles shall deliver the goods to the customers.
2. The capacity of level 1 vehicles is more than the capacity of level 2 vehicles.
3. The number of vehicles available at level 1 and level 2 are known beforehand. 

## To-do
- [X] Check Iterated Swap Procedure, without the dynamically changing solution with every iteration.
- [X] Perturbation Algorithm :- Satellite Swap and Worst Removal Operations to be implemented first. Regret Insertion also done.
- [X] Modify the current problem such that the constraints match the classical Two Echelon VRP, and check the results. 
- [ ] Implement restrictions on the number of carparks.
- [X] Check for larger test cases - Appropriate input format for the test cases of set 2, set 3, etc.
- [X] Change nodesDistances[][] to double[][].
- [ ] Separate out the test case files in a separate src/test/ folder 
- [ ] Python Code to generate random test cases and plot them for visualization.
- [ ] Code clean-up for private and public access modifiers of functions.