import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;


public class DateServer {

    public static void main(String[] args) throws Exception {
        try {

            discoverServer();

            ServerSocket sock = new ServerSocket(6013);
            //sock.setSoTimeout(10000); // 10 segundos

            System.out.println("Server IP: " +
            InetAddress.getLocalHost().getHostAddress());

            List<Socket> clients = new ArrayList<>();

            new Thread(() -> {
                for (Socket c : clients) {
                    try {
                        BufferedReader bin = new BufferedReader(
                                new InputStreamReader(c.getInputStream())
                        );
                        String line;
                            while ((line = bin.readLine()) != null) {
                                // Vai enviar a mensagem para todos os outros clientes, exceto o remetene
                                for (Socket other : clients) {
                                    if (other != c) {
                                        PrintWriter otherPout = new PrintWriter(
                                            other.getOutputStream(), true
                                        );
                                        otherPout.println(line);
                                    }
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Connection closed.");
                        }
                    }
                }
            ).start();

            // Adiciona o cliente à lista de clientes conectados
            while (true) {
                Socket client = sock.accept();
                clients.add(client);

                System.out.println("Client connected: " +
                client.getInetAddress().getHostAddress());
            }
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }

    private static void discoverServer() {
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket(8888);

                byte[] buffer = new byte[256];

                System.out.println("Waiting for discovery...");

                while(true){

                    DatagramPacket packet =
                            new DatagramPacket(buffer, buffer.length);

                    socket.receive(packet);

                    String msg = new String(packet.getData(),0,packet.getLength());

                    if(msg.equals("DISCOVER_SERVER")){

                        String response = InetAddress.getLocalHost().getHostAddress() + ":6013";

                        DatagramPacket reply = new DatagramPacket(
                                response.getBytes(),
                                response.length(),
                                packet.getAddress(),
                                packet.getPort()
                        );

                        socket.send(reply);
                    }
                }
            }catch (IOException e) {
                System.err.println("Error in discovery: " + e.getMessage());
            }
        }).start();
    }
}