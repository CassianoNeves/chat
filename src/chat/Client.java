package chat;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
		
		ImageIcon icon = new ImageIcon("images/user.png");
		JLabel label = new JLabel(icon);
		label.setBounds(50, 10, 72, 90);
		panel.add(label);
		
		ImageIcon iconChat = new ImageIcon("images/icon-chat.png");
		JLabel labelChat = new JLabel(iconChat);
		labelChat.setBounds(560, 10, 110, 88);
		panel.add(labelChat);
		
		JLabel userName = new JLabel("Diego Knorst");
		userName.setBounds(170, 45, 200, 30);
		userName.setFont(new Font("Serif", Font.PLAIN, 22));
		userName.setForeground(Color.WHITE);
		panel.add(userName);
		
		textMessage = new JTextArea();
		textMessage.setBounds(170, 450, 520, HEIGHT_MESSAGE);
		textMessage.setLineWrap(true);
		
		JScrollPane scrollText = new JScrollPane();
		scrollText.setBounds(170, 420, 400, HEIGHT_MESSAGE);
		scrollText.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollText.setViewportView(textMessage);
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
        usersOline.setBounds(10, 160, 150, 300);
        usersOline.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        usersOline.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(usersOline);
		
        textChat = new JTextArea();
        textChat.setBounds(170, 450, 520, HEIGHT_MESSAGE);
        textChat.setLineWrap(true);
        textChat.setEditable(false);
		
		JScrollPane scrollTextChat = new JScrollPane();
		scrollTextChat.setBounds(170, 110, 500, 300);
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
		
		Image imgRefresh = Toolkit.getDefaultToolkit().getImage("images/refresh.png");

		JButton buttonRefreshList = new JButton(new ImageIcon(imgRefresh));
		buttonRefreshList.setBounds(10, 110, 70, HEIGHT_MESSAGE);
		buttonRefreshList.setBackground(Color.WHITE);
		buttonRefreshList.setBorder(null);
		panel.add(buttonRefreshList);
		
		Image imgClose = Toolkit.getDefaultToolkit().getImage("images/close.png");
		
		JButton buttonCloseChat = new JButton(new ImageIcon(imgClose));
		buttonCloseChat.setBounds(90, 110, 70, HEIGHT_MESSAGE);
		buttonCloseChat.setBackground(Color.WHITE);
		buttonCloseChat.setBorder(null);
		panel.add(buttonCloseChat);
		
		buttonSend.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String message = textMessage.getText();
				
				if (message != null && !message.isEmpty()) {
					
					textChat.append("Eu: {0}\n".replace("{0}", message));
					textMessage.setText("");
					
					if (!message.contains(CodesServerReceive.CODE_PRIVADO)) {
						message = CodesServerReceive.CODE_MENSAGEM + message;
					}
					
					out.println(message);
					out.flush();
				}
			}
		});
		
		buttonRefreshList.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				out.println(CodesServerReceive.CODE_LIST);
				out.flush();
			}
			
		});
		
		buttonCloseChat.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				out.println(CodesServerReceive.CODE_SAIR);
				out.flush();
				listModel.clear();
				System.exit(0);
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
