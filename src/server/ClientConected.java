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
		
		if (this.nickname != null) {
			sendUserJoined();
			sendListOfClients();
			
			receiveMessages();
		}
		
	}

	private void receiveMessages() {
		while (socket.isConnected() && this.nickname !=null) {
			String messageReceived = null;
			
			try {
				messageReceived = in.readLine();
				
				if (messageReceived == null && this.nickname != null) {
					Logger.info(this.nickname + " caiu a conexão.");
					removeYourSelf();
					sendUserLeft();
					return;
				}
				
			} catch (IOException e) {
				Logger.error("ClientConected.receiveMessages()");
			}
			
			if (messageReceived != null) {
				switch (resolveCodeMessage(messageReceived)) {
				case CodesServerReceive.CODE_LIST:
					sendListOfClients();
					break;
					
				case CodesServerReceive.CODE_SAIR:
					sendUserExit();
					break;
					
				case CodesServerReceive.CODE_MENSAGEM:
					sendMessageToAllUsers(messageReceived);
					break;
					
				case CodesServerReceive.CODE_PRIVADO:
					sendMessagePrivado(messageReceived);
					break;

				default:
					break;
				}
			}
		}
	}

	private String resolveCodeMessage(String message) {
		if (message.contains(CodesServerReceive.CODE_LIST)) {
			return CodesServerReceive.CODE_LIST;
		}
		
		if (message.contains(CodesServerReceive.CODE_SAIR)) {
			return CodesServerReceive.CODE_SAIR;
		}
		
		if (message.contains(CodesServerReceive.CODE_MENSAGEM)) {
			return CodesServerReceive.CODE_MENSAGEM;
		}
		
		if (message.contains(CodesServerReceive.CODE_PRIVADO)) {
			return CodesServerReceive.CODE_PRIVADO;
		}
		
		return "";
	}
	
	private void sendMessagePrivado(String messageToSend) {
		String to = messageToSend
				.replace(CodesServerReceive.CODE_PRIVADO, "")
				.substring(1)
				.split(" ")[0];
		
		for (ClientConected client : clients) {
			if (to.equals(client.getNickname())) {
				
				messageToSend = messageToSend
						.replace(CodesServerReceive.CODE_PRIVADO, "")
						.replace(" " + to, "");
				
				messageToSend = "Privado -> " + this.nickname + ":" + messageToSend;
				String message = generateMessageProtocol(CodesClientReceive.CODE_MESSAGE, messageToSend);
				
				client.getOutStream().println(message);
				client.getOutStream().flush();
			}
		}
		
	}
	
	private void sendMessageToAllUsers(String messageToSend) {
		messageToSend = messageToSend.replace(CodesServerReceive.CODE_MENSAGEM, "");
		messageToSend = this.nickname + ":" + messageToSend;
		String message = generateMessageProtocol(CodesClientReceive.CODE_MESSAGE, messageToSend);
		
		for (ClientConected client : clients) {
			if (this.nickname.equals(client.getNickname()) || client.getNickname() == null) {
				continue;
			}
			
			client.getOutStream().println(message);
			client.getOutStream().flush();
		}
	}
	
	private void removeYourSelf() {
		clients.remove(this);
	}
	
	private void sendUserExit() {
		removeYourSelf();
		
		for (ClientConected client : clients) {
			String message = generateMessageProtocol(CodesClientReceive.CODE_SAIR, this.nickname);
			
			client.getOutStream().println(message);
			client.getOutStream().flush();
		}
		
		this.nickname = null;
	}
	
	private void sendUserLeft() {
		for (ClientConected client : clients) {
			if (this.nickname.equals(client.getNickname()) || client.getNickname() == null) {
				continue;
			}
			
			String message = generateMessageProtocol(CodesClientReceive.CODE_CAIU, this.nickname);
			
			client.getOutStream().println(message);
			client.getOutStream().flush();
		}
	}
	
	private void sendUserJoined() {
		for (ClientConected client : clients) {
			if (this.nickname.equals(client.getNickname()) || client.getNickname() == null) {
				continue;
			}
			
			String message = generateMessageProtocol(CodesClientReceive.CODE_ENTROU, this.nickname);
			
			client.getOutStream().println(message);
			client.getOutStream().flush();
		}
	}
	
	private void sendListOfClients() {
		for (ClientConected client : clients) {
			if (this.nickname.equals(client.getNickname()) || client.getNickname() == null) {
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
		out.println("Olá, seja bem-vindo.");
		out.println("Digite o seu Nickname para se conectar ao chat.");
		out.flush();
		
		Boolean isValidNickname = false;
		
		try {
			while (!isValidNickname) {
				String nicknameReceived = in.readLine();
				
				if (nicknameReceived == null) {
					return;
				}
				
				nicknameReceived = nicknameReceived.replace(CodesServerReceive.CODE_MENSAGEM, "");
				
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
