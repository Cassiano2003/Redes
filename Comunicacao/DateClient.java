// Java: Data Client
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;

public class DateClient{

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        
        try {

            String serverIP = "localhost";
            String serverPort = "6013";

            System.out.println("Ir manualmente ou altomaticamente? (digite 'manual' ou 'auto')");
            String escolha = scanner.nextLine();
            if (escolha.equalsIgnoreCase("auto")) {
                String msg = discoverServer();
                serverIP = msg.split(":")[0];
                serverPort = msg.split(":")[1];
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
        }
        catch (IOException ioe) {
            System.err.println(ioe);
        }
    }

    private static String discoverServer() {
        try {
            DatagramSocket pacotes = new DatagramSocket();
    
            pacotes.setBroadcast(true);
            pacotes.setSoTimeout(20000); // 2 segundos de timeout para esperar a resposta do servidor
    
            byte[] data = "DISCOVER_SERVER".getBytes();

            // Tenta enviar o pacote para o endereço de broadcast da rede
            DatagramPacket packet = null; // Inicializa o pacote como null para verificar se foi configurado corretamente

            try {
                // Itera sobre as interfaces de rede para encontrar o endereço de broadcast
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

                // Para cada interface, verifica se é ativa e não é de loopback, e tenta obter o endereço de broadcast
                while (interfaces.hasMoreElements()) {
                    // Obtém a próxima interface de rede
                    NetworkInterface ni = interfaces.nextElement();

                    // Verifica se a interface é ativa e não é de loopback
                    if (!ni.isLoopback() && ni.isUp()) {

                        // Para cada endereço associado à interface, tenta obter o endereço de broadcast
                        for (InterfaceAddress ia : ni.getInterfaceAddresses()) {

                            // Obtém o endereço de broadcast da interface
                            InetAddress broadcast = ia.getBroadcast();

                            // Se o endereço de broadcast for válido, configura o pacote para ser enviado para esse endereço
                            if (broadcast != null) {

                                packet = new DatagramPacket(
                                    data,
                                    data.length,
                                    broadcast,
                                    8888
                                );

                                break;
                            }
                        }
                    }

                    if (packet != null) break;
                }

                // Se não foi possível configurar o pacote para um endereço de broadcast válido, configura o pacote para ser enviado para o endereço de broadcast global
                if (packet == null) {
                    packet = new DatagramPacket(
                        data,
                        data.length,
                        InetAddress.getByName("255.255.255.255"),
                        8888
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
    
            pacotes.send(packet);
    
            byte[] buffer = new byte[256];
    
            DatagramPacket response =
                    new DatagramPacket(buffer, buffer.length);

            try{
                pacotes.receive(response);
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