
package Client;
import java.net.*;
import java.io.*;
import java.util.*;


public class Client {

    private Socket socket = null;
    private DataInputStream input = null;
    private DataOutputStream out = null;
    private boolean status = true;

    
    public Client(String address, int port, String name)
    {
        Scanner scn = new Scanner(System.in);
        // establish a connection
        try
        {

            InetAddress ip = InetAddress.getByName("localhost");
            socket = new Socket(address, port);
            out    = new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(socket.getInputStream());
            status = true;
            out.writeUTF(name);
        }
        catch(UnknownHostException u)
        {
            System.out.println(u);
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
 
        // string to read message from input
        Thread sendMessage = new Thread(new Runnable() 
        {
            @Override
            public void run() {
                while (true) {
 
                    // read the message to deliver.
                    String msg = scn.nextLine();
                     
                    try 
                    {
                        // write on the output stream
                        out.writeUTF(msg);
                        if(msg.equals("logout"))
                        {
                            status = false;
                            return;
                        }
                    } 
                    catch (IOException e) 
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
         
        // readMessage thread
        Thread readMessage = new Thread(new Runnable() 
        {
            @Override
            public void run() {
 
                while (true) {
                    try 
                    {
                        if(!status)
                        {
                            socket.close();
                            input.close();
                            out.close();
                            return;
                        }
                        // read the message sent to this client
                        String msg = input.readUTF();
                        System.out.println(msg);
                    } 
                    catch (EOFException f)
                    {
                        System.out.println("Client Logging OFF");
                    }
                    catch (IOException e) 
                    {
 
                        e.printStackTrace();
                    }
                }
            }
        });
        
        sendMessage.start();
        readMessage.start();
        // close the connection

    }
    
    public static void main(String[] args) {
        // TODO code application logic here
        String name = "";
        BufferedReader reader = 
                   new BufferedReader(new InputStreamReader(System.in));
        
        System.out.println("Enter your Name: ");
        try
        {
            name = reader.readLine();
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
        Client client =  new Client("127.0.0.1", 5000, name);
    }
    
}
