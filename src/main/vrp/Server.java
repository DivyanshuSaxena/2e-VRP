package vrp;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import org.json.simple.JSONObject;

class Server {
    ServerSocket ss;
    Socket socket;
    DataInputStream in;
    public Server(int port) {
        try
        {
            ss = new ServerSocket(port);
            System.out.println("Server started");
            System.out.println("Waiting for a client ...");
 
            socket = ss.accept();
            System.out.println("Client accepted");
 
            // takes input from the client socket
            in = new DataInputStream(
                new BufferedInputStream(socket.getInputStream()));
 
            String line = "";
 
            while (!line.equals("EOF"))
            {
                try
                {
                    line = in.readUTF();
                    System.out.println(line);
 
                }
                catch(IOException i)
                {
                    System.out.println(i);
                }
            }
            System.out.println("Closing connection");
 
            // close connection
            socket.close();
            in.close();
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }
}