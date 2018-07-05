from sklearn.cluster import MeanShift
import numpy as np
import random
import math

file = open("./input/custom/generated.dat","w")

num_customers = 500
max_range = 1000
coord = []
file.write("NAME : Generated\n")
file.write("COMMENT : Generated Test-Case Using Python\n")
file.write("TYPE : 2ECVRP\n")

for node in range(num_customers+1):
    coord.append([random.randint(0,max_range), random.randint(0,max_range)]) 

density = num_customers/max_range
bandwidth_ = 0.1 * math.pow(1.414, -(density*density)) * max_range
print (bandwidth_)
ms = MeanShift(bandwidth=bandwidth_)
ms.fit(coord)
labels = ms.labels_
cluster_centers = ms.cluster_centers_
# print (cluster_centers) # Debug

labels_unique = np.unique(labels)
n_clusters_ = len(labels_unique)

print("number of estimated clusters : %d, %d" % (n_clusters_, len(labels)))

## ###   #############################################################   ### ##
# Plot result
import matplotlib.pyplot as plt
from itertools import cycle

plt.figure(1)
plt.clf()
X = np.array(coord)

colors = cycle('bgrcmykbgrcmykbgrcmykbgrcmyk')
for k, col in zip(range(n_clusters_), colors):
    my_members = labels == k
    cluster_center = cluster_centers[k]
    plt.plot(X[my_members, 0], X[my_members, 1], col + '.')
    plt.plot(cluster_center[0], cluster_center[1], 'o', markerfacecolor=col,
             markeredgecolor='k', markersize=14)
plt.title('Estimated number of clusters: %d' % n_clusters_)
plt.show()

## ###   #############################################################   ### ##
num_carparks = n_clusters_
num_nodes = num_customers + num_carparks + 1
file.write("DIMENSION : %d\n" % num_nodes)
file.write("CARPARKS : %d\n" % num_carparks)
file.write("CUSTOMERS : %d\n" % num_customers)
file.write("EDGE_WEIGHT_TYPE : EUC2D\n")
file.write("FLEET SECTION\n")
file.write("L1CAPACITY : 20000\n")
file.write("L2CAPACITY : 8000\n")
file.write("L1FLEET: 15\n")
file.write("L2FLEET: 20\n")

file.write("NODE_COORD_SECTION\n")
for node in range(num_customers+1):
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
for cust in range(num_customers):
    file.write("%d %d\n" % (cust, random.randint(2,40)*10))
    i = i+1

file.write("EOF")