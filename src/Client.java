import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.json.JSONException;
import org.json.JSONObject;

import ClientUI.Listener;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.JTextField;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class Client extends JFrame implements ActionListener {

	    private JPanel contentPane;
	// Socket Related
		private static Socket clientSocket;
		private static int PORT ;
		private static String IP ;
		private PrintWriter out;
		private static volatile boolean isLoggedIn = false;
		private static JSONObject data;
		private static Client THIS;
		
		// JFrame related
		private JTextArea txtAreaLogs;
		private JButton btnStart;
		private JPanel panelNorth;
		private JLabel lblChatClient;
		private JPanel panelNorthSouth;
		private JLabel lblPort;
		private JLabel lblName;
		private JPanel panelSouth;
		private JButton btnSend;
		private JTextField txtMessage;
		private JTextField txtUserName;
		private JTextField txtPort;
		private String clientName;
		private JTextField txtIP;
		private JLabel lblIp;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Client frame = new Client();
					THIS = frame;
					//UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
					//SwingUtilities.updateComponentTreeUI(frame);
					//Logs
					System.setOut(new PrintStream(new textOutput(frame.txtAreaLogs)));
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
	public Client() {
		setTitle("20424024 - Tr\u1EA7n \u0110\u1ED7 Thanh H\u1EA3i");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 629, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		panelNorth = new JPanel();
		panelNorth.setBounds(5, 5, 603, 82);
		contentPane.add(panelNorth);
		panelNorth.setLayout(null);

		lblChatClient = new JLabel("CHAT CLIENT");
		lblChatClient.setBounds(0, 0, 265, 49);
		lblChatClient.setHorizontalAlignment(SwingConstants.CENTER);
		lblChatClient.setFont(new Font("Tahoma", Font.PLAIN, 40));
		panelNorth.add(lblChatClient);

		panelNorthSouth = new JPanel();
		panelNorthSouth.setBounds(0, 49, 603, 33);
		panelNorth.add(panelNorthSouth);
		panelNorthSouth.setLayout(null);

		lblName = new JLabel("UserName");
		lblName.setBounds(10, 9, 67, 14);
		panelNorthSouth.add(lblName);

		txtUserName = new JTextField();
		txtUserName.setBounds(87, 6, 96, 20);
		txtUserName.setColumns(10);
		panelNorthSouth.add(txtUserName);

		lblPort = new JLabel("Port");
		lblPort.setHorizontalAlignment(SwingConstants.CENTER);
		lblPort.setBounds(377, 9, 32, 14);
		panelNorthSouth.add(lblPort);

		txtPort = new JTextField();
		txtPort.setText("6666");
		txtPort.setBounds(417, 6, 96, 20);
		panelNorthSouth.add(txtPort);
		txtPort.setColumns(10);

		btnStart = new JButton("START");
		btnStart.setBounds(523, 5, 75, 23);
		panelNorthSouth.add(btnStart);
		btnStart.addActionListener(this);
		btnStart.setFont(new Font("Tahoma", Font.PLAIN, 12));
		
		txtIP = new JTextField();
		try {
	            txtIP.setText(InetAddress.getLocalHost().getHostAddress());

	        } catch (Exception ex) {
	            System.out.println(ex);
	     }
		txtIP.setColumns(10);
		txtIP.setBounds(239, 6, 128, 20);
		panelNorthSouth.add(txtIP);
		
		lblIp = new JLabel("IP");
		lblIp.setHorizontalAlignment(SwingConstants.CENTER);
		lblIp.setBounds(193, 9, 49, 14);
		panelNorthSouth.add(lblIp);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(5, 87, 603, 236);
		contentPane.add(scrollPane);

		txtAreaLogs = new JTextArea();
		txtAreaLogs.setBackground(Color.WHITE);
		txtAreaLogs.setForeground(Color.BLACK);
		txtAreaLogs.setLineWrap(true);
		scrollPane.setViewportView(txtAreaLogs);

		panelSouth = new JPanel();
		panelSouth.setBounds(5, 323, 603, 33);
		FlowLayout fl_panelSouth = (FlowLayout) panelSouth.getLayout();
		fl_panelSouth.setAlignment(FlowLayout.RIGHT);
		contentPane.add(panelSouth);

		txtMessage = new JTextField();
		txtMessage.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				getRootPane().setDefaultButton(btnSend);
			}
		});
		panelSouth.add(txtMessage);
		txtMessage.setColumns(50);

		btnSend = new JButton("SEND");
		btnSend.addActionListener(this);
		btnSend.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panelSouth.add(btnSend);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnStart) {
			if(btnStart.getText().equals("START")) {
				btnStart.setText("STOP");
				start();
			}else {
				btnStart.setText("START");
				try {
					stop();
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}else if(e.getSource() == btnSend) {
			String message = txtMessage.getText().trim();
			if(!message.isEmpty()) {
				try {
					Server.sendToClient(out, "message", message);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				txtMessage.setText("");
			}
		}
		//Refresh UI
		refreshUIComponents();
	}
	public void refreshUIComponents() {

	}
	public void start() {
		try {
			PORT = Integer.parseInt(txtPort.getText().trim());
			IP = txtIP.getText();
			clientName = txtUserName.getText().trim();
			clientSocket = new Socket(IP, PORT);
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			new Thread(new Listener()).start();
			//send name
			Server.sendToClient(out,"UserName", clientName);
		} catch (Exception err) {
			addToLogs("[ERROR] "+err.getLocalizedMessage());
		}
	}

	public void stop() throws JSONException{
		Server.sendToClient(out,"message","/quit");
	}

	public static void addToLogs(String message) {
		System.out.printf("%s %s\n", Server.formatter.format(new Date()), message);
	}

	private static class Listener implements Runnable {
		private BufferedReader in;
		@Override
		public void run() {
			try {
				
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				for(;;) {
					try {
						data = new JSONObject(in.readLine());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(data.has("status") && data.has("sender")) {
							@SuppressWarnings("unlikely-arg-type")
							boolean success = data.getString("status").equals(Server.Status.SUCCESS);
							String sender = data.getString("sender");
						if(data.has("login")) {
							isLoggedIn = success;
							THIS.refreshUIComponents();
							addToLogs(String.format("{[%s] %s",sender,data.get("login")));
						}
						else if(data.has("message")) {
							if(! success) {
								isLoggedIn = false;
								THIS.refreshUIComponents();
							}
							addToLogs(String.format("{[%s] %s",sender,data.get("message")));
						}
					}
				}
			} catch (IOException | JSONException e) {
				return;
			}
		}

	}

}
