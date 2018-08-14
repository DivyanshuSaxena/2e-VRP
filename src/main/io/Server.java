package io;
import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;

import vrp.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.*;

public class Server {
    ServerSocket ss;
    Socket socket;
    DataInputStream datain;
    public Server(int port) {
        JSONParser parser = new JSONParser();
        try
        {
            ss = new ServerSocket(port);
            System.out.println("Server started");
            System.out.println("Waiting for a client ...");
 
            socket = ss.accept();
            System.out.println("Client accepted");
 
            // takes input from the client socket
            try {
                datain = new DataInputStream(socket.getInputStream());

                BufferedReader in = new BufferedReader(new InputStreamReader(datain));
                String inputString = in.readLine();
                JSONObject json = (JSONObject) parser.parse(inputString);
                // Call the parseJSON method 
                parseJSON(json);
                datain.close();
                    
            } catch (IOException e) {
                e.printStackTrace();			
            } catch (ParseException e) {
                System.out.println("Error in parsing JSON object.");
                e.printStackTrace();
            }
            System.out.println("Closing connection");
 
            // close connection
            socket.close();
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }
    @SuppressWarnings("unchecked")
	public void parseJSON(JSONObject json) throws IOException {
        JSONParser parser = new JSONParser();
        try {
            // Constants
            Main.l1cap = Integer.parseInt(json.get("l1cap").toString());
            Main.l2cap = Integer.parseInt(json.get("l2cap").toString());
            Main.numVehicles1 = Integer.parseInt(json.get("numVehicles1").toString());
            Main.numVehicles2 = Integer.parseInt(json.get("numVehicles2").toString());

            // Get coordinates
            JSONArray coordinates = (JSONArray) parser.parse(json.get("coordinates").toString());
            Iterator<JSONObject> coordIterator = coordinates.iterator();
            while (coordIterator.hasNext()) {
                JSONObject coord = (JSONObject) coordIterator.next();
                int index = Integer.parseInt(coord.get("id").toString());
                int x = Integer.parseInt(coord.get("x").toString());
                int y = Integer.parseInt(coord.get("y").toString());
                if (index <= Main.x_coord.size()) {
                    Main.x_coord.add(index, x);
                    Main.y_coord.add(index, y);
                } else {
                    Main.x_coord.add(x);
                    Main.y_coord.add(y);
                }
            }

            // Get Customers
            Vector<Integer> customerPool = new Vector<Integer>();
            JSONArray customers = (JSONArray) parser.parse(json.get("customers").toString());
            Iterator<JSONObject> custIterator = customers.iterator();
            while (custIterator.hasNext()) {
                JSONObject customer = (JSONObject) custIterator.next();
                Customer cust = new Customer();
                int id = Integer.parseInt(customer.get("id").toString());
                cust.setId(id);
                customerPool.add(id);
                cust.setDemand(Integer.parseInt(customer.get("demand").toString()));
                Main.customers[id] = cust;
            }

            // Miscellaneous Constants and call Python file
            Main.numCustomers = Main.customers.length;
            // Cluster and then solve the results
            Cluster.cluster(customerPool);
            Main.constructJSON(Main.solve());
        } catch (ParseException e) {
            // Send response of trying again
        }
    }
    public void sendJSON(JSONObject json) {
        try {
            OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
            out.write(json.toString());
        } catch (IOException e) {
            // Try again to send the data
        }
    }
}