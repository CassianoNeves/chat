package chat;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import server.CodesClientReceive;
import server.CodesServerReceive;
import server.Logger;

public class Client extends JFrame {

	private static final long serialVersionUID = -2517616123689799182L;

	private static final int HEIGHT_MESSAGE = 40; 
	
	private JButton buttonSend;
	
	private DefaultListModel<String> listModel;
	
	private JList<String> usersOline;
	
	private JTextArea textChat;
	
	private JTextArea textMessage;
	
	private Socket socket;
	
	private PrintWriter out;
	
	private BufferedReader in;
	
	public Client() {
		initialize();
	}

	public static void main(String[] args) {
		new Client();
	}
	
	private void initializeSocket() {
		try {
			socket = new Socket("localhost", 8088);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream());
			
			createListnerReceivedMessager();
		} catch (UnknownHostException e) {
			textChat.setText("Server: N�o foi possivel conectar ao servidor, tente mais tarde.");
		} catch (IOException e) {
			textChat.setText("Server: N�o foi possivel conectar ao servidor, tente mais tarde.");
		}
	}

	private void initialize() {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		
		textMessage = new JTextArea();
		textMessage.setBounds(170, 450, 520, HEIGHT_MESSAGE);
		textMessage.setLineWrap(true);
		
		JScrollPane scrollText = new JScrollPane();
		scrollText.setBounds(170, 420, 400, HEIGHT_MESSAGE);
		scrollText.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollText.setViewportView(textMessage);
		scrollText.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		scrollText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(scrollText);
		
		buttonSend = new JButton("ENVIAR");
		buttonSend.setBackground(Color.WHITE);
		buttonSend.setBorder(null);
		buttonSend.setForeground(new Color(59, 89, 182));
		buttonSend.setBounds(580, 420, 90, HEIGHT_MESSAGE);
		panel.add(buttonSend);
		
		listModel = new DefaultListModel<>();
        
        usersOline = new JList<>(listModel);
        usersOline.setBounds(10, 10, 150, 450);
        panel.add(usersOline);
		
        textChat = new JTextArea();
        textChat.setBounds(170, 450, 520, HEIGHT_MESSAGE);
        textChat.setLineWrap(true);
        textChat.setEditable(false);
		
		JScrollPane scrollTextChat = new JScrollPane();
		scrollTextChat.setBounds(170, 10, 500, 400);
		scrollTextChat.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollTextChat.setViewportView(textChat);
		scrollTextChat.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(scrollTextChat);
		panel.setBackground(new Color(59, 89, 182));
        
		getContentPane().add(panel);
		setTitle("Chat Client");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(700, 510);
		setLocationRelativeTo(null);
		setVisible(true);
		
		buttonSend.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Boolean close = false;
				
				String message = textMessage.getText();
				
				if (message != null && !message.isEmpty()) {
					
					switch (resolveMessageToSend(message)) {
					case CodesServerReceive.CODE_SAIR:
						cleanClientsLis();
						close = true;
						break;

					default:
						break;
					}
					
					textChat.append("Eu: {0}\n".replace("{0}", message));
					textMessage.setText("");
					out.println(message);
					out.flush();
					
					if (close) {
						System.exit(0);
					}
				}
			}

			private void cleanClientsLis() {
				listModel.clear();
			}

			private String resolveMessageToSend(String message) {
				if (message.contains(CodesServerReceive.CODE_SAIR)) {
					return CodesServerReceive.CODE_SAIR;
				}
				
				return "";
			}
		});
		
		initializeSocket();
	}
	
	private void createListnerReceivedMessager() {
		new Runnable() {
			
			@Override
			public void run() {
				try {
					String serverResponse = null;
					
					while ((serverResponse = in.readLine()) != null) {
						resolveMessage(serverResponse);
					}
					
					textChat.append("----------A conex�o foi fechada------------");
				} catch (IOException e) {
					Logger.error("createListnerReceivedMessager");
				}
			}
		}.run();
	}
	
	private void resolveMessage(String message) {
		if (message != null) {
			
			switch (resolveCodeMessage(message)) {
			
			case CodesClientReceive.CODE_USUARIO:
				message = message.replace(CodesClientReceive.CODE_USUARIO, "");
				addElementInListClients(message);
				break;
				
			case CodesClientReceive.CODE_MESSAGE:
				message = message.replace(CodesClientReceive.CODE_MESSAGE, "");
				addTextInChat(message);
				break;
				
			case CodesClientReceive.CODE_ENTROU:
				message = message.replace(CodesClientReceive.CODE_ENTROU, "");
				addElementInListClients(message);
				addTextInChat("----------" + message + " acabou de entrar ----------" );
				break;
				
			case CodesClientReceive.CODE_CAIU:
				message = message.replace(CodesClientReceive.CODE_CAIU, "");
				removeElementInListClients(message);
				addTextInChat("---------- A conex�o de " + message + " caiu inesperadamente ----------" );
				break;
				
			case CodesClientReceive.CODE_SAIR:
				message = message.replace(CodesClientReceive.CODE_SAIR, "");
				removeElementInListClients(message);
				addTextInChat("---------- " + message + " saiu do chat ----------" );
				break;

			default:
				addTextInChat(message);
				break;
			}
		}
	}
	
	private void addTextInChat(String message) {
		textChat.append(message + "\n");
	}
	
	private void addElementInListClients(String nickname) {
		if (!listModel.contains(nickname)) {
			listModel.addElement(nickname);
		}
	}
	
	private void removeElementInListClients(String nickname) {
		int index = listModel.indexOf(nickname);
		
		if (index >= 0) {
			listModel.remove(index);
		}
	}
	
	private String resolveCodeMessage(String message) {
		if (message.contains(CodesClientReceive.CODE_USUARIO)) {
			return CodesClientReceive.CODE_USUARIO;
		}
		
		if (message.contains(CodesClientReceive.CODE_MESSAGE)) {
			return CodesClientReceive.CODE_MESSAGE;
		}
		
		if (message.contains(CodesClientReceive.CODE_ENTROU)) {
			return CodesClientReceive.CODE_ENTROU;
		}
		
		if (message.contains(CodesClientReceive.CODE_CAIU)) {
			return CodesClientReceive.CODE_CAIU;
		}
		
		if (message.contains(CodesClientReceive.CODE_SAIR)) {
			return CodesClientReceive.CODE_SAIR;
		}
		
		return "";
	}
}
