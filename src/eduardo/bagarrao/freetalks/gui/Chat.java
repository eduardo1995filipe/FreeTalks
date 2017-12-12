package eduardo.bagarrao.freetalks.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.ParseException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import eduardo.bagarrao.freetalks.engine.ConnectionManager;
import eduardo.bagarrao.freetalks.message.Message;
import eduardo.bagarrao.freetalks.util.DateParser;

public class Chat extends JFrame {

	private static final long serialVersionUID = 1L;

	private ConnectionManager cm = ConnectionManager.getInstance();

	private JTextArea area;
	private JTextArea writeTextArea;
	private JButton sendButton;
	private JPanel sendPanel;
	private JPanel mainPanel;
	
	public Chat() {
		setTitle("[" + cm.getClientId() + "]" + Login.APP_NAME + " " + Login.PHASE + " v" + Login.VERSION);
		this.area = new JTextArea();
		this.writeTextArea = new JTextArea();
		this.sendPanel = new JPanel(new BorderLayout());
		this.sendButton = new JButton("Send!");
		this.mainPanel  = new JPanel(new BorderLayout());
		setSize(new Dimension(600, 600));
	
		sendPanel.add(writeTextArea);
		sendPanel.add(sendButton, BorderLayout.EAST);
		mainPanel.add(area);
		mainPanel.add(sendPanel,BorderLayout.SOUTH);
		
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		sendButton.addActionListener(e ->
		{cm.publishMessage(writeTextArea.getText());
		writeTextArea.setText("");});		 
		
		addWindowListener(new WindowAdapter()
	        {
	            @Override
	            public void windowClosing(WindowEvent e)
	            {
	                cm.disconnect();
	            }
	        });
		 
		new Thread(() -> {while(true) {
			try {
				while(true) {
					Vector<Message> vector = cm.getAllMessages();
					for(Message msg : vector){
						area.setText(area.getText() + 
								"[" + ((msg.getSender().equals(cm.getClientId())? "You" : msg.getSender())) + "] " + 
								" [" + DateParser.parseString(msg.getDate()) + "] --> " +
								msg.getMessage().toString() + "\n");
					}
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}}).start();
		
		Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);

		mainPanel.setBorder(padding);
		sendPanel.setBorder(padding);
		
		setContentPane(mainPanel);
	}

	public void init() {
		setVisible(true);
	}
}
	