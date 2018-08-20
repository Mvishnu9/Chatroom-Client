
package Client;
import java.net.*;
import java.io.*;
import java.util.*;


public class Client {

    private Socket socket = null;
    private DatagramSocket DsocketS = null;
    private DatagramSocket DsocketR = null;
    private DataInputStream input = null;
    private DataOutputStream out = null;
    private FileInputStream fis = null;
    private FileOutputStream fos = null;
    private boolean status = true;
    private byte[] bufS = null; 
    private byte[] bufR = null; 
    private InetAddress ip = null;
    
    public Client(String address, int port, int dport, String name)
    {
        Scanner scn = new Scanner(System.in);
        try
        {
            ip = InetAddress.getByName("localhost");
            socket = new Socket(address, port);
            DsocketS = new DatagramSocket();
            DsocketR = new DatagramSocket(5003);
            out    = new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(socket.getInputStream());
            bufR = new byte[4096];
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
 
        Thread sendMessage = new Thread(new Runnable() 
        {
            @Override
            public void run() {
                while (true) {
 
                    String msg = scn.nextLine();                     
                    try 
                    {
                        StringTokenizer st = new StringTokenizer(msg);
                        String tok = st.nextToken();
                        if(tok.equalsIgnoreCase("logout"))
                        {
                            out.writeUTF(msg);                            
                            status = false;
                            return;
                        }
                        else if(tok.equalsIgnoreCase("file"))
                        {
                            out.writeUTF("Sending File");
                            bufS = msg.getBytes();
                            DatagramPacket DpSend = new DatagramPacket(bufS, bufS.length, ip, 5001);
                            DsocketS.send(DpSend);
                            out.writeUTF("File Sent");
                            continue;
                        }
                        out.writeUTF(msg);
                        
                    } 
                    catch (IOException e) 
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
         
        Thread readMessage = new Thread(new Runnable() 
        {
            @Override
            public void run() {
 
                while (true) {
                    try 
                    {
                        if(!status)
                        {
                            String Emsg = input.readUTF();
                            if(Emsg.equalsIgnoreCase("Successfully logged out"))
                            {
                                System.out.println(Emsg);
                                socket.close();
                                input.close();
                                out.close();
                                return;
                            }
                            
                        }
                        String msg = input.readUTF();
                        StringTokenizer st = new StringTokenizer(msg, ":");
                        if(st.countTokens() > 1)
                        {
                            System.out.println("LOC A");
                            String check = st.nextToken();
                            check = st.nextToken();
                            if(check.equalsIgnoreCase(" Sending File"))
                            {
                                System.out.println("LOC B");
                                DatagramPacket dp = new DatagramPacket(bufR, bufR.length);
                                DsocketR.receive(dp);
                                String str = new String(dp.getData(), 0, dp.getLength());
                                System.out.println("PACKET HAD - "+str);
                                continue;
                            }
                        }
                                
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

    }
    
    public static void main(String[] args) {
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
        Client client =  new Client("127.0.0.1", 5000, 5001, name);
    }
    
}
