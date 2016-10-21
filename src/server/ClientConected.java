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
			Logger.error("[ERROR] ClientConeted Contruct");
		}
		
		start();
	}
	
	@Override
	public void run() {
		receiveNickname();
		sendUserJoined();
		sendListOfClients();
		
		receiveMessages();
	}

	private void receiveMessages() {
		while (socket.isConnected()) {
			String messageReceived = null;
			
			try {
				messageReceived = in.readLine();
				
				if (messageReceived == null) {
					Logger.info(this.nickname + " se desconectou.");
					sendUserLeft();
					return;
				}
				
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
	
	private void sendUserLeft() {
		for (ClientConected client : clients) {
			if (this.nickname == client.getNickname() || client.getNickname() == null) {
				continue;
			}
			
			String message = generateMessageProtocol(CodesClientReceive.CODE_SAIU, this.nickname);
			
			client.getOutStream().println(message);
			client.getOutStream().flush();
		}
	}
	
	private void sendUserJoined() {
		for (ClientConected client : clients) {
			if (this.nickname == client.getNickname() || client.getNickname() == null) {
				continue;
			}
			
			String message = generateMessageProtocol(CodesClientReceive.CODE_ENTROU, this.nickname);
			
			client.getOutStream().println(message);
			client.getOutStream().flush();
		}
	}
	
	private void sendListOfClients() {
		for (ClientConected client : clients) {
			if (this.nickname == client.getNickname() || client.getNickname() == null) {
				continue;
			}
			
			String message = generateMessageProtocol(CodesClientReceive.CODE_USUARIO, client.getNickname());
			
			out.println(message);
			out.flush();
		}
	}

	private String generateMessageProtocol(String code, String message) {
		String messageSender = "{0} {1}";
		
		messageSender = messageSender.replace("{0}", code);
		messageSender = messageSender.replace("{1}", message);
		
		return messageSender;
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
					out.println("Este Nickname já esta em uso, por favor digite outro.");
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
	
	public PrintWriter getOutStream() {
		return this.out;
	}

}
