"""
The generate module randomly generates a new test case.
And then writes the data points along with the carparks location in an output file.
"""
import random
import math
import nodecluster

NUM_CUSTOMERS = 500
MAX_RANGE = 1000

def main():
    """
    Main function that calls the module nodecluster and generates the test cases in generated.dat
    """
    file = open("./input/custom/generated.dat", "w")

    coord = []
    file.write("NAME : Generated\n")
    file.write("COMMENT : Generated Test-Case Using Python\n")
    file.write("TYPE : 2ECVRP\n")

    for node in range(NUM_CUSTOMERS+1):
        coord.append([random.randint(0, MAX_RANGE), random.randint(0, MAX_RANGE)])

    density = NUM_CUSTOMERS/MAX_RANGE
    bandwidth_ = 0.2 * math.pow(1.414, -(density*density)) * MAX_RANGE
    print(bandwidth_)
    cluster_centers = nodecluster.cluster(coord, bandwidth_)
    n_clusters_ = len(cluster_centers)
    num_carparks = n_clusters_
    num_nodes = NUM_CUSTOMERS + num_carparks + 1
    file.write("DIMENSION : %d\n" % num_nodes)
    file.write("CARPARKS : %d\n" % num_carparks)
    file.write("CUSTOMERS : %d\n" % NUM_CUSTOMERS)
    file.write("EDGE_WEIGHT_TYPE : EUC2D\n")
    file.write("FLEET SECTION\n")
    file.write("L1CAPACITY : 20000\n")
    file.write("L2CAPACITY : 8000\n")
    file.write("L1FLEET: 15\n")
    file.write("L2FLEET: 20\n")

    file.write("NODE_COORD_SECTION\n")
    for node in range(NUM_CUSTOMERS+1):
        file.write("%d %d %d\n" % (node, int(coord[node][0]), int(coord[node][1])))

    file.write("CARPARK_SECTION\n")
    i = 1
    for center in cluster_centers:
        # print(center.item(0), center.item(1))
        file.write("%d %d %d\n" % (i, int(center.item(0)), int(center.item(1))))
        i = i+1

    file.write("DEMAND_SECTION\n")
    file.write("%d %d\n" % (0, 0))
    i = 1
    for cust in range(NUM_CUSTOMERS):
        file.write("%d %d\n" % (cust, random.randint(2, 40)*10))
        i = i+1

    file.write("EOF")

if __name__ == '__main__':
    main()
