import math
from itertools import cycle
from sklearn.cluster import MeanShift
import numpy as np
import matplotlib.pyplot as plt

def cluster(coord, num_customers, max_range):
    """
    cluster function clusters the elements in the array coord using the other two as parameters
    """
    density = num_customers/max_range
    bandwidth_ = 0.2 * math.pow(1.414, -(density*density)) * max_range
    print(bandwidth_)
    ms = MeanShift(bandwidth=bandwidth_)
    ms.fit(coord)
    labels = ms.labels_
    cluster_centers = ms.cluster_centers_
    # print (cluster_centers) # Debug

    n_clusters_ = len(np.unique(labels))
    print("number of estimated clusters : %d, %d" % (n_clusters_, len(labels)))

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
    return cluster_centers
