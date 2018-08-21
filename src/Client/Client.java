
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
//           DsocketR = new DatagramSocket(5003);
//            Fsocket = new Socket(address, 5004);
            out = new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(socket.getInputStream());
            bufR = new byte[4096];
            bufS = new byte[4096];
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
                        else if(tok.equalsIgnoreCase("udp"))
                        {
                            System.out.println("Enter the path of the file to be sent");
                            String path = scn.nextLine();
                            File FileToSend = new File(path);
                            if(!FileToSend.exists())
                            {
                                System.out.println("Specified File does not exist, please give absolute path");
                                continue;
                            }
                            out.writeUTF("Sending File UDP");
                            fis = new FileInputStream(path);
                            int count;
                            while((count = fis.read(bufS))!= -1)
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
                            if(!FileToSend.exists())
                            {
                                System.out.println("Specified File does not exist, please give absolute path");
                                continue;
                            }
                            out.writeUTF("Sending File TCP :"+fnam);
                            fis = new FileInputStream(path);
//                            OutputStream os = Fsocket.getOutputStream();
                            int count;
                            bufS = new byte[4096];
                            while((count = fis.read(bufS)) != -1)
                            {
                                out.write(bufS);
                                bufS = new byte[4096];
                            }
                            out.flush();
                            fis.close();
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
                            String check = st.nextToken();
                            check = st.nextToken();
                            if(check.equalsIgnoreCase(" Sending File UDP"))
                            {
                                DatagramPacket dp = new DatagramPacket(bufR, bufR.length);
                                DsocketR.receive(dp);
                                String str = new String(dp.getData(), 0, dp.getLength());
                                System.out.println("PACKET HAD - "+str);
                                continue;
                            }
                            else if(check.equalsIgnoreCase(" Sending File TCP"))
                            {
                                String fnam = st.nextToken();
                                String Dir = CreateDir();
                                Path Fpath = Paths.get(Dir, fnam);
                                File fil = new File(Fpath.toString());                                
                                fos = new FileOutputStream(fil);
                                int count;
                                bufR = new byte[4096];
                                while((count = input.read(bufR)) != -1)
                                {
                                    fos.write(bufR);
                                    bufR = new byte[4096];
                                }
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
        File dir = new File(apath);
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
        Client client =  new Client("127.0.0.1", 5000, 5001, name);
    }
    
}
