package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientConected implements Runnable {

	private Socket socket;
	
	private PrintWriter out;
	
	private BufferedReader in;
	
	private String nickname;
	
	private List<ClientConected> clients;
	
	private Thread thread;
			
	public ClientConected(Socket socket, List<ClientConected> clients) {
		this.socket = socket;
		this.clients = clients;
		
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream());
		} catch (IOException e) {
			Logger.error("ClientConeted Contruct");
		}
		
		start();
	}
	
	@Override
	public void run() {
		receiveNickname();
//		sendListOfClients();
		
		receiveMessages();
	}

	private void receiveMessages() {
		while (true) {
			String messageReceived = null;
			
			try {
				messageReceived = in.readLine();
			} catch (IOException e) {
				Logger.error("[ERROR] ClientConected.receiveMessages()");
			}
			
			switch (resolveCodeMessage(messageReceived)) {
			case CodesServerReceive.CODE_LIST:
				sendListOfClients();
				break;

			default:
				break;
			}
			
		}
	}
	
	private String resolveCodeMessage(String message) {
		if (message.contains(CodesServerReceive.CODE_LIST)) {
			return CodesServerReceive.CODE_LIST;
		}
		
		
		
		return "";
	}
	
	private void sendListOfClients() {
		for (ClientConected client : clients) {
			if (this.nickname == client.getNickname()) {
				continue;
			}
			
			String message = "{0} {1}";
			message = message.replace("{0}", CodesClientReceive.CODE_USUARIO);
			message = message.replace("{1}", client.getNickname());
			
			out.println(message);
			out.flush();
		}
	}

	private void receiveNickname() {
		out.println("Olá seja bem-vindo.");
		out.println("Digite o seu Nickname para se conectar ao chat.");
		out.flush();
		
		Boolean isValidNickname = false;
		
		try {
			while (!isValidNickname) {
				String nicknameReceived = in.readLine();
				isValidNickname = isValidNickname(nicknameReceived);
				
				if (!isValidNickname) {
					out.println("Este Nickname já esta em uso, por favor digite outro.\n");
					out.flush();
					nicknameReceived = null;
				} else {
					this.nickname = nicknameReceived;
					out.println("Agora você está no chat, aproveite!");
					out.flush();
				}
			}
		} catch (IOException e) {
			Logger.error("ClientConeted.receiveNickname()");
		}
	}
	
	private boolean isValidNickname(String nickname) {
		Boolean isValid = true;
		
		for (ClientConected client : clients) {
			if (nickname.equals(client.getNickname())) {
				return false;
			}
		}
		
		return isValid;
	}
	
	private void start(){
		thread = new Thread(this);
		thread.start();
    }
	
	public String getNickname() {
		return nickname;
	}

}
