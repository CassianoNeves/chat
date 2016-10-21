package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author gabriel
 */
public class Servidor {
    public static List<ClienteConectado> clientes;
    private int porta;
    ServerSocket ss;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Servidor server = new Servidor(8088);
        server.aguardarClientes();
    }

    public Servidor(int porta) {
        this.porta = porta;
        setup();
    }

    private void setup() {
        try {
            ss = new ServerSocket(porta);
            clientes = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void aguardarClientes() {
        Socket s;
        ClienteConectado cli;
        try {
            while (true) {
                s = ss.accept();
                cli = new ClienteConectado(s);
                clientes.add(cli);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
