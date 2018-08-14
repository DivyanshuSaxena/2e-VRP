package vrp;

import java.util.*;
import java.io.*;

public class Cluster {
    public static void cluster(Vector<Integer> customers) throws IOException {
        PrintWriter pWriter = new PrintWriter("./files/interface/input.txt", "UTF-8");
        // pWriter.println(bandwidth);
        for (int cust : customers) {
            pWriter.println(Main.x_coord.elementAt(cust-1) + " " + Main.y_coord.elementAt(cust-1));
        }
        pWriter.close();

        // Run Node Cluster
		String command = "cmd /c py ./files/nodecluster.py 1000 --eclipse";
	    Process p = Runtime.getRuntime().exec(command);
	    try {
			p.waitFor();
			BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line;
			while ((line = bri.readLine()) != null) {
				System.out.println(line);
			}
			bri.close();
			while ((line = bre.readLine()) != null) {
				System.out.println(line);
			}
			bre.close();
			p.waitFor();
			System.out.println("Done.");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    p.destroy();

        File file = new File("files/interface/output.txt");
        Scanner intermediate = new Scanner(file);
        intermediate.nextLine();
        Main.numCarpark = intermediate.nextInt();
        Main.numNodes = Main.numCarpark + Main.numCustomers + 1;
        Main.carparks = new Carpark[Main.numCarpark];
        Vector<Integer> cpx_coord = new Vector<Integer>();
        Vector<Integer> cpy_coord = new Vector<Integer>();
        for (int i = 0; i < Main.numCarpark; i++) {
            cpx_coord.add(intermediate.nextInt());
            cpy_coord.add(intermediate.nextInt());
            Main.carparks[i] = new Carpark();
            Main.carparks[i].setId(i+1);
        }
        intermediate.close();

        // Add temporary vectors
        Vector<Integer> xcoord_temp = new Vector<Integer>();
        Vector<Integer> ycoord_temp = new Vector<Integer>();
        xcoord_temp.addAll(Main.x_coord);
        ycoord_temp.addAll(Main.y_coord);
        
        // Re-initialize global vectors
        Main.x_coord = new Vector<Integer>();
        Main.y_coord = new Vector<Integer>();
        Main.nodesDistance = new double[Main.numNodes][Main.numNodes];
        
        Main.x_coord.add(xcoord_temp.elementAt(0));
        Main.y_coord.add(ycoord_temp.elementAt(0));
        Main.x_coord.addAll(cpx_coord);
        Main.y_coord.addAll(cpy_coord);
        for (int i = 1; i < xcoord_temp.size(); i++) {
            Main.x_coord.add(xcoord_temp.elementAt(i));
            Main.y_coord.add(ycoord_temp.elementAt(i));
        }
        for (int i = 0; i < Main.numNodes; i++) {
        	if (i > Main.numCarpark) {
        		Main.customers[i-Main.numCarpark-1].setId(i);
        	}
            for (int j = i+1; j < Main.numNodes; j++) {
                int xcoordi = Main.x_coord.elementAt(i), xcoordj = Main.x_coord.elementAt(j);
                int ycoordi = Main.y_coord.elementAt(i), ycoordj = Main.y_coord.elementAt(j);
                Main.nodesDistance[i][j] = Math.sqrt((xcoordi-xcoordj)*(xcoordi-xcoordj) + (ycoordi-ycoordj)*(ycoordi-ycoordj));
                Main.nodesDistance[j][i] = Main.nodesDistance[i][j];
            }
        }

        Main.solve();
    }
}