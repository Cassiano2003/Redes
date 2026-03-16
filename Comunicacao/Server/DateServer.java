package Server;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.ArrayList;


public class DateServer {

    private static List<Usuarios> usuarios = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        try {

            discoverServer();

            ServerSocket sock = new ServerSocket(6013);

            System.out.println("Server IP: " +
            InetAddress.getLocalHost().getHostAddress());

            List<Socket> clients = new ArrayList<>();

            long start = System.currentTimeMillis();
            long maxTime = 600000; // 10 min

            sock.setSoTimeout(10000); // 10 seconds
            
            while (System.currentTimeMillis() - start < maxTime) {

                try {
                    Socket client = sock.accept();
                    clients.add(client);
                    usuarios.add(new Usuarios(client.getInetAddress().getHostName(), client.getInetAddress().getHostAddress()));
                    System.out.println("New client connected: " + client.getInetAddress().getHostName());
                    new Thread(() -> responceClient(client, clients, usuarios)).start();
                } catch (SocketTimeoutException ignored) {
                }
                
                if (clients.size() >= 10) {
                    System.out.println("Maximum clients reached. No longer accepting new connections.");
                    break;
                }
            }
            
            System.out.println("Server timeout. Shutting down.");
            sock.close();
            System.out.println("Connected users: " + usuarios);
            System.exit(0);
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }

    private static void responceClient(Socket client, List<Socket> clients, List<Usuarios> usuarios) {
        try {
            BufferedReader bin = new BufferedReader(
                new InputStreamReader(client.getInputStream())
            );
            String line;
            while ((line = bin.readLine()) != null) {
                Usuarios usuario = usuarios.stream()
                    .filter(u -> u.getIp().equals(client.getInetAddress().getHostAddress()))
                    .findFirst()
                    .orElse(null);
                if (usuario != null) {
                    usuario.addMensagem(line);
                }
                // Vai enviar a mensagem para todos os outros clientes, exceto o remetene
                for (Socket other : clients) {
                    if (other != client) {
                        PrintWriter otherPout = new PrintWriter(
                            other.getOutputStream(), true
                        );
                        otherPout.println("\n" + client.getInetAddress().getHostName() + ": " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Connection closed.");
        } finally {
            usuarioDesconectado(client, clients);
            try {
                client.close();
            } catch (IOException ignored) {}

        }
    }

    private static void usuarioDesconectado(Socket client, List<Socket> clients) {
        for (Socket other : clients) {
            if (other != client) {
                try {
                    PrintWriter otherPout = new PrintWriter(
                        other.getOutputStream(), true
                    );

                    otherPout.println("\n" + client.getInetAddress().getHostName() + " has disconnected.");

                } catch (IOException e) {
                    System.out.println("Error sending disconnect message.");
                }
            }
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