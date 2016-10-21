package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gabriel
 */
public class ClienteConectado implements Runnable{
    Socket s;
    PrintWriter out;
    BufferedReader in;
    Thread t;
    String apelido;

    public ClienteConectado(Socket s) {
        this.s = s;
        setup();
        start();
    }
    
    public void enviarMensagem(String msg){
        out.println(msg);
        out.flush();
    }
    
    public String receberMensagem(){
        String msg;
        try {
            msg = in.readLine();            
            for(ClienteConectado c:Servidor.clientes){
                c.enviarMensagem(apelido + ": " + msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }        
        return null;
    }
    
    private void setup(){
        try {
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void start(){
        t = new Thread(this);
        t.start();
    }
    
    private void recebeApelido(){
        try {
            apelido = in.readLine();            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        recebeApelido();
        while(true){
            receberMensagem();
        }
    }    
}
