import matplotlib.pyplot as plt
import re

file = open("./output/solution.txt","r")

numNodes, lastCarpark, routeEnd = 0, 0, 0
x_coord, y_coord = [], []
route_x, route_y = [], []
cproute_x, cproute_y = [], []
x_coord_filled, y_coord_filled, num_nodes = False, False, False
for line in file :
    listLine = re.findall(r'[0-9]+', line)
    # print (listLine)
    if x_coord_filled and y_coord_filled and num_nodes:
        if len(listLine) > 0:
            start_node = int(listLine[0])
            if start_node == 0 and lastCarpark == 0:
                if len(cproute_x) > 0 and len(cproute_y) > 0:
                    # Add the cproute to the plot.
                    plt.plot(cproute_x,cproute_y)
                    print ("First Level Route : ") # Debug
                    print (cproute_x, cproute_y)
                cproute_x = [int(x_coord[0])]
                cproute_y = [int(y_coord[0])]
            else:
                cproute_x.append(int(x_coord[start_node]))
                cproute_y.append(int(y_coord[start_node]))
            lastCarpark = start_node
            if len(listLine) > 1:
                print (listLine)
                for node in listLine[1:-1]:
                    if int(node) < numNodes:
                        print (node, end = ' ') # Debug
                        route_x.append(int(x_coord[int(node)]))
                        route_y.append(int(y_coord[int(node)]))
                    if int(node) == lastCarpark and routeEnd == 0:
                        routeEnd = 1
                    elif int(node) == lastCarpark and routeEnd == 1:
                        # Add the route to the plot
                        plt.plot(route_x, route_y, 'o')
                        print ("Second Level Route : ") # Debug
                        print (route_x, route_y)
                        route_x, route_y = [], []
                        routeEnd = 0
    elif x_coord_filled and y_coord_filled:
        numNodes = int(listLine[0])
        num_nodes = True
    elif x_coord_filled:
        y_coord = listLine
        y_coord_filled = True
    else:
        x_coord = listLine
        x_coord_filled = True
print ("First Level Route : ") # Debug
print (cproute_x, cproute_y)
plt.plot(cproute_x, cproute_y, 'bo', cproute_x, cproute_y, 'k--')
plt.show()