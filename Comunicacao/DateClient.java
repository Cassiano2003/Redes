// Java: Data Client
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class DateClient{

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        
        try {

            String serverIP;
            String serverPort;

            System.out.println("Ir manualmente ou altomaticamente? (digite 'manual' ou 'auto')");
            String escolha = scanner.nextLine();
            if (escolha.equalsIgnoreCase("auto")) {
                String msg = discoverServer();
                if (msg == null) {
                    System.out.print("Digite o IP do servidor: ");
                    serverIP = scanner.nextLine();
                    System.out.print("Digite a porta do servidor: ");
                    serverPort = scanner.nextLine();
                }else{
                    System.out.println("Conectando ao servidor: " + msg);   
                    serverIP = msg.split(":")[0];
                    serverPort = msg.split(":")[1];
                }
            }else{
                System.out.print("Digite o IP do servidor: ");
                serverIP = scanner.nextLine();
                System.out.print("Digite a porta do servidor: ");
                serverPort = scanner.nextLine();
            }



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
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    System.err.println("Connection closed.");
                }
            }).start();

            try {
                System.out.print("Escrevas suas mensagens (digite 'exit' para sair): ");
                while(true){
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
            }catch (SocketException e) {
                System.out.println("\nConnection lost. Exiting...");
                sock.close();
                System.exit(0);
            }
        }
        catch (IOException ioe) {
            System.err.println(ioe);
        }
    }

    private static String discoverServer() {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.setSoTimeout(5000); // 5 segundos costumam ser suficientes para resposta local

            byte[] data = "DISCOVER_SERVER".getBytes();
            boolean sentAtLeastOne = false;

            // Itera sobre as interfaces de rede
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();

                // Pula interfaces inativas ou de loopback
                if (ni.isLoopback() || !ni.isUp()) continue;

                for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                    InetAddress broadcast = ia.getBroadcast();
                    
                    if (broadcast != null) {
                        DatagramPacket packet = new DatagramPacket(data, data.length, broadcast, 8888);
                        
                        socket.send(packet); // Envia o pacote
                        sentAtLeastOne = true;
                        
                        System.out.println("Enviando broadcast para: " + broadcast.getHostAddress());

                        // --- O SEGREDO ESTÁ AQUI ---
                        // Pausa de 200ms entre cada interface para não inundar a rede
                        try {
                            Thread.sleep(200); 
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }

            // Se não encontrou interfaces específicas, tenta o broadcast global como último recurso
            if (!sentAtLeastOne) {
                DatagramPacket globalPacket = new DatagramPacket(
                    data, data.length, InetAddress.getByName("255.255.255.255"), 8888
                );
                socket.send(globalPacket);
            }

            // Tenta receber a resposta
            byte[] buffer = new byte[256];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);

            try {
                socket.receive(response);
                String msg = new String(response.getData(), 0, response.getLength());
                System.out.println("Servidor encontrado: " + msg);
                return msg;
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout: Nenhum servidor respondeu.");
            } finally {
                socket.close(); // Importante fechar o socket
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}