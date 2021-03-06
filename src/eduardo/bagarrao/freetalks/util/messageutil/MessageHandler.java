package eduardo.bagarrao.freetalks.util.messageutil;

import java.awt.image.BufferedImage;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.Vector;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import eduardo.bagarrao.freetalks.message.ImageMessage;
import eduardo.bagarrao.freetalks.message.Message;
import eduardo.bagarrao.freetalks.message.TextMessage;
import eduardo.bagarrao.freetalks.util.Encrypter;

/**
 * @author Eduardo Bagarrao 
 * 
 * Class that handles all received and sent messages
 */
public class MessageHandler implements MqttCallback {

	/**
	 * Broker of the MQTT Server.
	 */
	private static final String BROKER = "tcp://iot.eclipse.org:1883";

	/**
	 * vector that stores all the received textMessages.
	 */
	private Vector<TextMessage> textMessageVector;
	
	/**
	 * vector that stores all the received imageMessages.
	 */
	private Vector<ImageMessage> imageMessageVector;
	
	/**
	 * Mqqtclient.
	 */
	private MqttClient client;
	
	/**
	 * options of the MQTT client.
	 */
	private MqttConnectOptions options;
	
	/**
	 * persistence of the MQTT client.
	 */
	private MemoryPersistence persistence;
	
	/**
	 * Client Id to Sign in.
	 */
	private String clientId;
	
	/**
	 * Value that checks that the user is already connected or not.
	 */
	private boolean isConnected;

	/**
	 * MessageHandler constructor.
	 * @param clientId id to sign in.
	 * @throws MqttException
	 */
	public MessageHandler(String clientId) throws MqttException {
		this.textMessageVector = new Vector<TextMessage>();
		this.imageMessageVector = new Vector<ImageMessage>();
		this.clientId = clientId;
		this.persistence = new MemoryPersistence();
		this.options = new MqttConnectOptions();
		this.client = new MqttClient(BROKER, ManagementFactory.getRuntimeMXBean().getName() + "_" + clientId,persistence);
		this.isConnected = false;
	}

	/**
	 * Connects to the Eclipse Paho Server
	 * 
	 * @throws MqttSecurityException
	 * @throws MqttException
	 */
	public void connect() {
		if (!isConnected()) {
			options.setCleanSession(true);
			try {
				client.connect();
				client.subscribe(Message.TOPIC);
			} catch (MqttException e) {
				e.printStackTrace();
			}
			client.setCallback(this);
			setConnected(true);
		}
	}

	/**
	 * Disconnects from the Eclipse Paho server
	 * 
	 * @throws MqttException
	 */
	public void disconnect() {
		if (isConnected()) {
			try {
				
				client.unsubscribe(Message.TOPIC);
				client.disconnect();
			} catch (MqttException e) {
				e.printStackTrace();
			}
			setConnected(false);
		}
	}

	/**
	 * Changes the {@link #isConnected()} attribute
	 * 
	 * @param isConnected
	 *            attribute to change
	 */
	private void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	/**
	 * Checks whether this client is connected or not to the Eclipse Paho Server
	 * 
	 * @return {@link #isConnected} value
	 */
	public boolean isConnected() {
		return isConnected;
	}

	/**
	 * returns the oldest message from {@link #textMessageVector}
	 * 
	 * @return null value if the {@link #textMessageVector} size is zero, else returns the
	 *         message at the index 0 of the {@link #textMessageVector}
	 */
	public TextMessage getNextTextMessage() {
		return (hasNextTextMessage()) ? textMessageVector.remove(0) : null;
	}

	/**
	 * checks if the {@link #textMessageVector} has messages by handle
	 * 
	 * @return checks if the size of the {@link #textMessageVector} is not zero
	 */
	private boolean hasNextTextMessage() {
		return textMessageVector.size() != 0;
	}
	
	/**
	 * returns the oldest message from {@link #imageMessageVector}
	 * 
	 * @return null value if the {@link #imageMessageVector} size is zero, else returns the
	 *         message at the index 0 of the {@link #imageMessageVector}
	 */
	public ImageMessage getNextImageMessage() {
		return (hasNextImageMessage()) ? imageMessageVector.remove(0) : null;
	}

	/**
	 * checks if the {@link #imageMessageVector} has messages by handle
	 * 
	 * @return checks if the size of the {@link #imageMessageVector} is not zero
	 */
	private boolean hasNextImageMessage() {
		return imageMessageVector.size() != 0;
	}

	/**
	 * Sends a text message to the Eclipse Paho server, who will be shown on all online
	 * FreeTalker chats.
	 * 
	 * @param text text to send.
	 */
	public void writeMessage(String text) {
		try {
			MqttMessage message = new TextMessage(clientId, text, new Date());
			if (isConnected()) {
				try {
					client.publish(Message.TOPIC, message);
				} catch (MqttException e) {
					e.printStackTrace();
				}
				System.out.println("[Message Sent] --> " + text);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends a image message to the Eclipse Paho server, who will be shown on all online
	 * FreeTalker chats
	 * 
	 * @param text text to send.
	 * @param image image to send.
	 */
	public void writeMessage(String text, BufferedImage image) {
		try {
			ImageMessage message = new ImageMessage(clientId, text, image, new Date());
			if (isConnected()) {
				try {
					client.publish(Message.TOPIC, message);
				} catch (MqttException e) {
					e.printStackTrace();
				}
				System.out.println("[ImageMessage Sent] --> " + text);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void connectionLost(Throwable throwable) {
		connect();
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		JSONObject obj = new JSONObject(Encrypter.decrypt(message.toString(), "ssshhhhhhhhhhh!!!!"));
		if(obj.has(ImageMessage.KEY_TYPE)) {
			switch(MessageType.valueOf(obj.getString(ImageMessage.KEY_TYPE))) {
			case TEXT_MESSAGE:
				System.out.println("Received Text Message! --> " + message.toString());
				textMessageVector.add(new TextMessage(new JSONObject(Encrypter.decrypt(message.toString(), "ssshhhhhhhhhhh!!!!"))));
				break;
			case IMAGE_MESSAGE:
				System.out.println("Received Image Message! --> " + message.toString());
				imageMessageVector.add(new ImageMessage(new JSONObject(Encrypter.decrypt(message.toString(), "ssshhhhhhhhhhh!!!!"))));
				break;
			default:
				break;
			};
		}		
	}
}