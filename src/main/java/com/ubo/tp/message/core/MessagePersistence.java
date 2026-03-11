package main.java.com.ubo.tp.message.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Set;
import java.util.UUID;

import main.java.com.ubo.tp.message.core.database.DbConnector;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;

/**
 * Persistance simple des messages dans un fichier texte.
 * Format par ligne : msgUuid|senderUuid|senderTag|senderName|recipientUuid|emissionDate|text
 */
public class MessagePersistence {

	private static final String SEP = "||";
	private static final String FILE_NAME = "messages.dat";

	private File mFile;

	public MessagePersistence() {
		this.mFile = new File(FILE_NAME);
	}

	/**
	 * Sauvegarde tous les messages actuels dans le fichier.
	 */
	public void saveMessages(Set<Message> messages) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(mFile))) {
			for (Message msg : messages) {
				StringBuilder sb = new StringBuilder();
				sb.append(msg.getUuid().toString()).append(SEP);
				sb.append(msg.getSender().getUuid().toString()).append(SEP);
				sb.append(msg.getSender().getUserTag()).append(SEP);
				sb.append(msg.getSender().getName()).append(SEP);
				sb.append(msg.getRecipient().toString()).append(SEP);
				sb.append(msg.getEmissionDate()).append(SEP);
				sb.append(msg.getText());
				writer.write(sb.toString());
				writer.newLine();
			}
		} catch (Exception e) {
			System.err.println("[Persistance] Erreur sauvegarde messages : " + e.getMessage());
		}
	}

	/**
	 * Charge les messages depuis le fichier et les ajoute dans la BDD.
	 */
	public void loadMessages(DbConnector dbConnector) {
		if (!mFile.exists()) {
			return;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(mFile))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split("\\|\\|");
				if (parts.length < 7) {
					continue;
				}

				UUID msgUuid = UUID.fromString(parts[0]);
				UUID senderUuid = UUID.fromString(parts[1]);
				String senderTag = parts[2];
				String senderName = parts[3];
				UUID recipientUuid = UUID.fromString(parts[4]);
				long emissionDate = Long.parseLong(parts[5]);
				String text = parts[6];

				User sender = new User(senderUuid, senderTag, "", senderName);
				Message message = new Message(msgUuid, sender, recipientUuid, emissionDate, text);
				dbConnector.addMessage(message);
			}
			System.out.println("[Persistance] " + dbConnector.getDatabase().getMessages().size() + " messages charges.");
		} catch (Exception e) {
			System.err.println("[Persistance] Erreur chargement messages : " + e.getMessage());
		}
	}
}
