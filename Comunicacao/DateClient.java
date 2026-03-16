// Java: Data Client
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class DateClient{

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        
        try {
    
            String msg = discoverServer();

            String serverIP = msg.split(":")[0];
            String serverPort = msg.split(":")[1];

            Socket sock = new Socket(serverIP, Integer.parseInt(serverPort));

            BufferedReader bin = new BufferedReader(
                    new InputStreamReader(sock.getInputStream())
            );

            PrintWriter pout = new PrintWriter(
                    sock.getOutputStream(), true
            );

            System.out.println("Connected to server: " + serverIP);

            new Thread(() -> {
                try {
                    String line;
                    while ((line = bin.readLine()) != null) {
                        System.out.println(line+"\n"+InetAddress.getLocalHost().getHostName() + ": ");
                    }
                } catch (IOException e) {
                    System.err.println("Connection closed.");
                }
            }).start();

            while(true){
                System.out.print(InetAddress.getLocalHost().getHostName() + ": ");
                String mensagem = scanner.nextLine();
                // Encerra a conexão se o usuário digitar "exit"
                if(mensagem.trim().equalsIgnoreCase("exit")){
                    sock.close();
                    break;
                }
                // Sem mensagens vazias
                if(!mensagem.isEmpty() && !mensagem.trim().isEmpty()){
                    pout.println(mensagem);
                }
            }
        }
        catch (IOException ioe) {
            System.err.println(ioe);
        }
    }

    private static String discoverServer() {
        try {
            DatagramSocket teste = new DatagramSocket();
    
            teste.setBroadcast(true);
    
            byte[] data = "DISCOVER_SERVER".getBytes();
    
            DatagramPacket packet = new DatagramPacket(
                    data,
                    data.length,
                    InetAddress.getByName("255.255.255.255"),
                    8888
            );
    
            teste.send(packet);
    
            byte[] buffer = new byte[256];
    
            DatagramPacket response =
                    new DatagramPacket(buffer, buffer.length);
    
            teste.setSoTimeout(2000);

            try{
                teste.receive(response);
            }catch(SocketTimeoutException e){
                System.out.println("Timeout waiting for server");
            }
    
            String msg = new String(response.getData(),0,response.getLength());
    
            System.out.println("Found server: " + msg);
    
            return msg;
        } catch (IOException ioe) {
            System.err.println(ioe);
            return null;
        }
    }
}