package vrp;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.*;

class Server {
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
                // Call the parseJSON method in main method
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
}