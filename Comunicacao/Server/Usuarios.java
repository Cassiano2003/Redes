package Server;

import java.util.List;
import java.util.ArrayList;

public class Usuarios {
    private String nome;
    private String ip;
    private List<String> mensagens;

    public Usuarios(String nome, String ip) {
        this.nome = nome;
        this.ip = ip;
        this.mensagens = new ArrayList<>();
    }

    public String getNome() {
        return nome;
    }

    public String getIp() {
        return ip;
    }

    public List<String> getMensagens() {
        return mensagens;
    }

    public void addMensagem(String mensagem) {
        this.mensagens.add(mensagem);
    }


    public String toString() {
        return nome + " (" + ip + ")" + " - Mensagens: " + mensagens;
    }

}