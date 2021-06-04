import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JTextField;
import java.util.Scanner;
import java.util.Timer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import java.awt.*;

public class Server extends JFrame implements ActionListener {
	
    /**
	 * 
	 */
	public static SimpleDateFormat formatter = new SimpleDateFormat("[dd/MM/yy hh:mm a]");
	private static HashMap<String, PrintWriter> connectedClients = new HashMap<>();
	private static final int MAX_CONNECTED = 50;
	private static int PORT = 6666;
	private static ServerSocket server;
	private static volatile boolean exit = false;
	private static JSONObject data;
	public static enum Status{
		SUCCESS,
		ERROR
	}
	
	private JPanel contentPane;
	private JTextArea txtAreaLogs;
	private JLabel lblChatServer;
	private JButton btnStart;
	public JLabel lblIP;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Server frame = new Server();
					System.setOut(new PrintStream(new textOutput(frame.txtAreaLogs)));
					//Logs
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Server() {
		setTitle("20424024 - Tr\u1EA7n \u0110\u1ED7 Thanh H\u1EA3i");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 570, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		lblChatServer = new JLabel("CHAT SERVER");
		lblChatServer.setBounds(5, 5, 262, 38);
		lblChatServer.setHorizontalAlignment(SwingConstants.CENTER);
		lblChatServer.setFont(new Font("Tahoma", Font.PLAIN, 40));
		contentPane.add(lblChatServer);
		
		
		btnStart = new JButton("START");
		btnStart.setBounds(5, 311, 544, 45);
		btnStart.addActionListener(this);
		//btnStart.setPreferredSize(new Dimension(10,10));
		btnStart.setFont(new Font("Tahoma", Font.PLAIN, 30));
		contentPane.add(btnStart);
		add(btnStart);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(5, 54, 544, 257);
		contentPane.add(scrollPane);

		txtAreaLogs = new JTextArea();
		txtAreaLogs.setBackground(Color.WHITE);
		txtAreaLogs.setForeground(Color.BLACK);
		txtAreaLogs.setLineWrap(true);
		scrollPane.setViewportView(txtAreaLogs);
		
		lblIP = new JLabel("IP : Port");
		lblIP.setForeground(Color.BLUE);
		lblIP.setHorizontalAlignment(SwingConstants.CENTER);
		lblIP.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblIP.setBounds(289, 11, 243, 32);
		contentPane.add(lblIP);
		add(lblIP);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() == btnStart) {
			if(btnStart.getText().equals("START")) {
				exit = false;
				start();
				btnStart.setText("STOP");
				  try {
			            lblIP.setText(InetAddress.getLocalHost().getHostAddress() + " : " + PORT);

			        } catch (Exception ex) {
			            System.out.println(ex);
			        }
			}else {
				addToLogs("Chat server is stopped...");
				try {
					broadcastMessage("Chat server is shutting downs..",Status.ERROR);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				exit = true;
				btnStart.setText("START");
				connectedClients.clear();
				try {
					server.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				lblIP.setText("IP : Port");
			}
		}
		
	}
	private static void broadcastMessage(String message) throws JSONException {
		broadcastMessage("SERVER: ",message,Status.SUCCESS);
	}
	private static void broadcastMessage(String message, Status status) throws JSONException {
		broadcastMessage("SERVER: ",message,status);
	}
	private static void broadcastMessage(String sender,String message,Status status) throws JSONException {
		// TODO Auto-generated method stub
		for (PrintWriter p: connectedClients.values()) {
			sendToClient(p,sender,"message",message,status);
		}
	}
	public static void sendToClient(PrintWriter writer, String key, String value) throws JSONException {
		sendToClient(writer,"SERVER: ",key,value,Status.SUCCESS);
	}
	public static void sendToClient(PrintWriter writer, String key, String value,Status status) throws JSONException {
		sendToClient(writer,"SERVER",key,value,status);
	}
	public static void sendToClient(PrintWriter writer, String sender,String key ,String value,Status status) throws JSONException {
		data = new JSONObject();
		data.put(key, value);
		data.put("sender",sender);
		data.put("status",status);
		writer.println(data.toString());
	}
	public static void addToLogs(String message) {
		System.out.printf("%s %s\n", formatter.format(new Date()), message);
	}
	public static void start() {
		new Thread(new ServerHandler()).start();
	}
	public static void stop() throws IOException {
		if (!server.isClosed()) server.close();
	}
	// Start of Server Handler
	private static class ServerHandler implements Runnable{
		public void run() {
			try {
				server = new ServerSocket(PORT);
				addToLogs("Server is listening connection...");
				while(!exit) {
					if (connectedClients.size() <= MAX_CONNECTED){
						new Thread(new ClientHandler(server.accept())).start();
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	// Start of Client Handler
	private static class ClientHandler implements Runnable {
		private Socket socket;
		private PrintWriter out;
		private BufferedReader in;
		private String name;
		
		public ClientHandler(Socket socket) {
			this.socket = socket;
		}
		
		@Override
		public void run(){
			addToLogs("Client connected: " + socket.getInetAddress());
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
				if(exit) sendToClient(out,"message","Chat server is offline.",Status.ERROR);
				for(;;) {
					name = in.readLine();
					if (new JSONObject(name).has("nickname")) {
						name = new JSONObject(name).getString("nickname");
					}
					synchronized (connectedClients) {
						if (!name.isEmpty() && !connectedClients.keySet().contains(name)) break;
						else
							sendToClient(out,"login","Please input UserName",Status.ERROR);
					}
				}
				addToLogs(name.toUpperCase() + " has joined.");
				broadcastMessage(String.format("%s  has joined.", name));
				connectedClients.put(name, out);
				sendToClient(out,"login",String.format("Well come to chat group, %s",name.toUpperCase()));
				sendToClient(out,"message","You can the chat now");
				String message;
				while ((message = in.readLine()) != null && !exit) {
					data = new JSONObject(message);
					if (data.has("message")) {
						String msg = data.getString ("message");
						if(msg.toLowerCase().equals("/quit")) {
							break;
						}
						else {
							broadcastMessage(name,msg,Status.SUCCESS);
						}
					}
				}
			} catch (Exception e) {
				addToLogs(e.getMessage());
			} finally {
				if (name != null) {
					addToLogs(name + " is leaving");
					try {
						sendToClient(out,"message","You have been logged out!...",Status.ERROR);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					connectedClients.remove(name);
					try {
						broadcastMessage(name + " has left");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}


	}
}
