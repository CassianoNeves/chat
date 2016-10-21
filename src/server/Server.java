package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

	private ServerSocket serverSocket;
	
	List<ClientConected> clients = new ArrayList<>();
	
	public Server() {
		initialize();
	}
	
	public static void main(String[] args) {
		new Server();
	}
	
	public void initialize() {
		try {
			serverSocket = new ServerSocket(8088);
			System.out.println("Servidor iniciado com Sucesso.");
			
			Socket socket = null;
			
			while (true) {
				socket = serverSocket.accept();
				
				ClientConected clientConected = new ClientConected(socket, clients);
				
				clients.add(clientConected);
			}
			
		} catch (IOException e) {
			System.out.println("Não foi possivel iniciar o servidor");
			return;
		}
		
		
	}
}
