
package Client;
import java.net.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class Client {

    private Socket socket = null;
    private Socket Fsocket = null;
    private DatagramSocket DsocketS = null;
    private DatagramSocket DsocketR = null;
    private DataInputStream input = null;
    private DataOutputStream out = null;
    private DataInputStream fin = null;
    private DataOutputStream fout = null;
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
            socket = new Socket();
            socket.connect(new InetSocketAddress(address, port), 1000);
//            socket = new Socket(address, port);
            DsocketS = new DatagramSocket();
//           DsocketR = new DatagramSocket(5003);
            Fsocket = new Socket(address, 5004);
            out = new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(socket.getInputStream());
            fout = new DataOutputStream(Fsocket.getOutputStream());
            fin = new DataInputStream(Fsocket.getInputStream());
            bufR = new byte[4096];
            bufS = new byte[4096];
            status = true;
            out.writeUTF(name);
        }
        catch(SocketTimeoutException ste)
        {
            System.out.println("Connection timed out, please verify if the Server is running at the given ip and port");
            return;
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
                        else if(tok.equalsIgnoreCase("udp"))
                        {
                            System.out.println("Enter the path of the file to be sent");
                            String path = scn.nextLine();
                            File FileToSend = new File(path);
                            String fnam = FileToSend.getName();
                            long Flen = FileToSend.length();
                            if(!FileToSend.exists())
                            {
                                System.out.println("Specified File does not exist, please give absolute path");
                                continue;
                            }                            
                            out.writeUTF("Sending File UDP :"+fnam+":"+Long.toString(Flen));
                            fis = new FileInputStream(path);
                            int count;
                            bufS = new byte[4096];
                            while((count = fis.read(bufS))>0)
                            {
                                DatagramPacket dp = new DatagramPacket(bufS, count, ip, 5001);
                                DsocketS.send(dp);                                
                            }
                            fis.close();
                            out.writeUTF("File Sent");
                            continue;
                        }
                        else if(tok.equalsIgnoreCase("tcp"))
                        {
                            System.out.println("Enter the path of the file to be sent");
                            String path = scn.nextLine();
                            File FileToSend = new File(path);
                            String fnam = FileToSend.getName();
                            long Flen = FileToSend.length();
                            if(!FileToSend.exists())
                            {
                                System.out.println("Specified File does not exist, please give absolute path");
                                continue;
                            }
                            
                            out.writeUTF("Sending File TCP :"+fnam+":"+Long.toString(Flen));
                            fis = new FileInputStream(path);
                            int count;
                            bufS = new byte[4096];
                            while((count = fis.read(bufS)) > 0)
                            {
                                fout.write(bufS);
                                bufS = new byte[4096];
                            }                           
                            fout.flush();
                            fis.close();
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
         
        Thread readMessage; 
        readMessage = new Thread(new Runnable() 
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
                            String check = st.nextToken();
                            check = st.nextToken();
                            if(check.equalsIgnoreCase(" Sending File UDP "))
                            {
                                String fnam = st.nextToken();
                                String Dir = CreateDir();
                                Path Fpath = Paths.get(Dir, fnam);
                                String len = st.nextToken();
                                long lenl = Long.parseLong(len);
                                System.out.println("Length of file = "+lenl);
                                File fil = new File(Fpath.toString());                                
                                fos = new FileOutputStream(fil);
                                System.out.println("Saving File to "+Fpath.toString());
                                bufR = new byte[4096];
                                DatagramPacket dp = new DatagramPacket(bufR, bufR.length);
//                                while(DsocketR.receive(dp))
                                    
                                    DsocketR.receive(dp);
                                
                                continue;
                            }
                            else if(check.equalsIgnoreCase(" Sending File TCP "))
                            {                                
                                String fnam = st.nextToken();
                                String Dir = CreateDir();
                                Path Fpath = Paths.get(Dir, fnam);
                                String len = st.nextToken();
                                long lenl = Long.parseLong(len);
                                System.out.println("Length of file = "+lenl);
                                File fil = new File(Fpath.toString());                                
                                fos = new FileOutputStream(fil);
                                System.out.println("Saving File to "+Fpath.toString());
                                int count;
                                bufR = new byte[4096];
                                while((lenl > 0)&&((count = fin.read(bufR)) > 0))
                                {
                                    lenl = lenl - 4096;
                                    fos.write(bufR);
                                    bufR = new byte[4096];
                                }
                                System.out.println("File Downloaded successfully");
                                fos.close();        
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
    
    public String CreateDir() throws IOException
    {
        Path currentRelativePath = Paths.get("");
        String apath = currentRelativePath.toAbsolutePath().normalize().toString();
        Path Dir = Paths.get(apath,"ChatDownloads");
        File dir = new File(Dir.toString());
        if (dir.exists()) 
        {
            return Dir.toString();
        }
        if(dir.mkdirs()) 
        {
            return Dir.toString();
        }
        throw new IOException("Failed to create directory '" + dir.getAbsolutePath() + "' for an unknown reason.");
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
        System.out.println("Enter IP Address of server to connect to -");
        String ip = "";
        try
        {
            ip = reader.readLine();
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
        String port = "";
        int portno = 5000;
        System.out.println("Enter port of server to connect to -");
        try
        {
            port = reader.readLine();
            portno = Integer.parseInt(port);
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
        Client client =  new Client(ip, portno, 5001, name);
    }
    
}
