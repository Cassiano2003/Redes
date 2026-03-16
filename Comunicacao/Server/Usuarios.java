package Comunicacao.Server;



public class Usuarios {
    private String nome;
    private String ip;

    pubic Usuarios(String nome, String ip) {
        this.nome = nome;
        this.ip = ip;
    }

    public String getNome() {
        return nome;
    }

    public String getIp() {
        return ip;
    }


    public String toString() {
        return nome + " (" + ip + ")";
    }

}