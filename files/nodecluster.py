import math
from itertools import cycle
from sklearn.cluster import MeanShift
import numpy as np
import matplotlib.pyplot as plt
import re

def cluster(coord, bandwidth):
    """
    cluster function clusters the elements in the array coord using the other two as parameters
    """
    ms = MeanShift(bandwidth=bandwidth)
    ms.fit(coord)
    labels = ms.labels_
    cluster_centers = ms.cluster_centers_
    # print (cluster_centers) # Debug

    n_clusters_ = len(np.unique(labels))
    print("number of estimated clusters : %d, % d" % (n_clusters_, len(labels)))

    ## ###   #############################################################   ### ##
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

    # Write to a file
    file = open("./files/interface/output.txt", "w")
    file.write("CARPARK_SECTION\n")
    file.write("%d\n" % n_clusters_)
    i = 0
    for center in cluster_centers:
        # print(center.item(0), center.item(1))
        file.write("%d %d %d\n" % (i, int(center.item(0)), int(center.item(1))))
        i = i+1

    return cluster_centers

def main():
    """
    The main function that reads from the input file and calls cluster over it.
    """
    file = open("./files/interface/input.txt","r")
    get_bandwidth = False
    bandwidth_ = 0
    coord = []
    for line in file:
        listLine = re.findall(r'[0-9]+', line)
        if get_bandwidth:
            coord.append([int(listLine[0]), int(listLine[1])])
        else:
            bandwidth_ = float(listLine[0])
            get_bandwidth = True
    cluster(coord, bandwidth_)

if __name__ == '__main__':
    main()
