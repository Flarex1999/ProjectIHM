package main.java.com.ubo.tp.message.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;

/**
 * Classe de gestion des conversion des objets entre le datamodel et les
 * fichiers de propriété.
 *
 * @author S.Lucas
 */
public class DataFilesManager {

	/**
	 * Clé du fichier de propriété pour l'attribut uuid
	 */
	protected static final String PROPERTY_KEY_UUID = "UUID";

	/**
	 * Clé du fichier de propriété pour l'attribut tag
	 */
	protected static final String PROPERTY_KEY_USER_TAG = "Tag";

	/**
	 * Clé du fichier de propriété pour l'attribut password
	 */
	protected static final String PROPERTY_KEY_USER_PASSWORD = "This_is_not_the_password";

	/**
	 * Clé du fichier de propriété pour l'attribut name
	 */
	protected static final String PROPERTY_KEY_NAME = "Name";

	/**
	 * Clé du fichier de propriété pour l'attribut Sender
	 */
	protected static final String PROPERTY_KEY_MESSAGE_SENDER = "Sender";

	/**
	 * Clé du fichier de propriété pour l'attribut Recipient
	 */
	protected static final String PROPERTY_KEY_MESSAGE_RECIPIENT = "Recipient";

	/**
	 * Clé du fichier de propriété pour l'attribut Date
	 */
	protected static final String PROPERTY_KEY_MESSAGE_DATE = "Date";

	/**
	 * Clé du fichier de propriété pour l'attribut Text
	 */
	protected static final String PROPERTY_KEY_MESSAGE_TEXT = "Text";

	/**
	 * Clé du fichier de propriété pour l'image jointe (base64).
	 */
	protected static final String PROPERTY_KEY_MESSAGE_IMAGE = "Image";
	protected static final String PROPERTY_KEY_MESSAGE_REACTIONS = "Reactions";

	/**
	 * Clé du fichier de propriété pour le statut online d'un utilisateur.
	 */
	protected static final String PROPERTY_KEY_USER_ONLINE = "Online";
	protected static final String PROPERTY_KEY_USER_DELETED = "Deleted";

	/**
	 * Clé du fichier de propriété pour l'attribut Creator
	 */
	protected static final String PROPERTY_KEY_CHANNEL_CREATOR = "Creator";

	/**
	 * Clé du fichier de propriété pour l'attribut Users
	 */
	protected static final String PROPERTY_KEY_CHANNEL_USERS = "Users";

	/**
	 * Séparateur pour les utilisateurs.
	 */
	protected static final String USER_SEPARATOR = ";";

	/**
	 * Chemin d'accès au répertoire d'échange.
	 */
	protected String mDirectoryPath;

	/**
	 * Lecture du fichier de propriété pour un {@link User}
	 *
	 * @param userFileName
	 */
	public User readUser(File userFile) {
		User user = null;

		if (userFile != null && userFile.getName().endsWith(Constants.USER_FILE_EXTENSION) && userFile.exists()) {
			Properties properties = PropertiesManager.loadProperties(userFile.getAbsolutePath());

			String uuid = properties.getProperty(PROPERTY_KEY_UUID, UUID.randomUUID().toString());
			String tag = properties.getProperty(PROPERTY_KEY_USER_TAG, "NoTag");
			String password = decrypt(properties.getProperty(PROPERTY_KEY_USER_PASSWORD, "NoPassword"));
			String name = properties.getProperty(PROPERTY_KEY_NAME, "NoName");
			boolean online = Boolean.parseBoolean(properties.getProperty(PROPERTY_KEY_USER_ONLINE, "false"));
			boolean deleted = Boolean.parseBoolean(properties.getProperty(PROPERTY_KEY_USER_DELETED, "false"));

			user = new User(UUID.fromString(uuid), tag, password, name);
			user.setOnline(online);
			user.setDeleted(deleted);
		}

		return user;
	}

	/**
	 * Génération d'un fichier pour un utilisateur ({@link User}).
	 *
	 * @param user Utilisateur à générer.
	 */
	public void writeUserFile(User user) {
		Properties properties = new Properties();

		// Récupération du chemin pour le fichier à générer
		String destFileName = this.getFileName(user.getUuid(), Constants.USER_FILE_EXTENSION);

		properties.setProperty(PROPERTY_KEY_UUID, user.getUuid().toString());
		properties.setProperty(PROPERTY_KEY_USER_TAG, user.getUserTag());
		properties.setProperty(PROPERTY_KEY_USER_PASSWORD, encrypt(user.getUserPassword()));
		properties.setProperty(PROPERTY_KEY_NAME, user.getName());
		properties.setProperty(PROPERTY_KEY_USER_ONLINE, String.valueOf(user.isOnline()));
		properties.setProperty(PROPERTY_KEY_USER_DELETED, String.valueOf(user.isDeleted()));

		PropertiesManager.writeProperties(properties, destFileName);
	}

	/**
	 * Génération d'un fichier pour un utilisateur ({@link User}).
	 *
	 * @param user Utilisateur à générer.
	 */
	public void writeChannelFile(Channel channel) {
		Properties properties = new Properties();

		// Récupération du chemin pour le fichier à générer
		String destFileName = this.getFileName(channel.getUuid(), Constants.CHANNEL_FILE_EXTENSION);

		properties.setProperty(PROPERTY_KEY_UUID, channel.getUuid().toString());
		properties.setProperty(PROPERTY_KEY_NAME, channel.getName());
		properties.setProperty(PROPERTY_KEY_CHANNEL_CREATOR, channel.getCreator().getUuid().toString());
		properties.setProperty(PROPERTY_KEY_CHANNEL_USERS, this.getUsersAsString(channel.getUsers()));

		PropertiesManager.writeProperties(properties, destFileName);
	}

	/**
	 * Lecture du fichier de propriété pour un {@link Channel}
	 *
	 * @param channelFile
	 * @param userMap
	 */
	public Channel readChannel(File channelFile, Map<UUID, User> userMap) {
		Channel channel = null;

		if (channelFile != null && channelFile.getName().endsWith(Constants.CHANNEL_FILE_EXTENSION)
				&& channelFile.exists()) {
			Properties properties = PropertiesManager.loadProperties(channelFile.getAbsolutePath());

			String uuid = properties.getProperty(PROPERTY_KEY_UUID, UUID.randomUUID().toString());
			String channelName = properties.getProperty(PROPERTY_KEY_NAME, "NoName");
			String channelCreator = properties.getProperty(PROPERTY_KEY_CHANNEL_CREATOR,
					Constants.UNKNONWN_USER_UUID.toString());
			String channelUsers = properties.getProperty(PROPERTY_KEY_CHANNEL_USERS, "");

			User creator = getUserFromUuid(channelCreator, userMap);
			List<User> allUsers = this.getUsersFromString(channelUsers, userMap);

			channel = new Channel(UUID.fromString(uuid), creator, channelName, allUsers);
		}

		return channel;
	}

	/**
	 * Lecture du fichier de propriété pour un {@link Message}
	 *
	 * @param messageFile
	 * @param userMap
	 */
	public Message readMessage(File messageFile, Map<UUID, User> userMap) {
		Message message = null;

		if (messageFile != null && messageFile.getName().endsWith(Constants.MESSAGE_FILE_EXTENSION)
				&& messageFile.exists()) {
			Properties properties = PropertiesManager.loadProperties(messageFile.getAbsolutePath());

			String uuid = properties.getProperty(PROPERTY_KEY_UUID, UUID.randomUUID().toString());
			String senderUuid = properties.getProperty(PROPERTY_KEY_MESSAGE_SENDER,
					Constants.UNKNONWN_USER_UUID.toString());
			String recipientUuid = properties.getProperty(PROPERTY_KEY_MESSAGE_RECIPIENT,
					Constants.UNKNONWN_USER_UUID.toString());
			String emissionDateStr = properties.getProperty(PROPERTY_KEY_MESSAGE_DATE, "0");
			String text = properties.getProperty(PROPERTY_KEY_MESSAGE_TEXT, "NoText");

			User sender = getUserFromUuid(senderUuid, userMap);
			long emissionDate = Long.valueOf(emissionDateStr);

			message = new Message(UUID.fromString(uuid), sender, UUID.fromString(recipientUuid), emissionDate, text);

			// Lire l'image si présente
			String imageBase64 = properties.getProperty(PROPERTY_KEY_MESSAGE_IMAGE, "");
			if (!imageBase64.isEmpty()) {
				message.setImageBase64(imageBase64);
			}

			// Lire les réactions
			String reactionsStr = properties.getProperty(PROPERTY_KEY_MESSAGE_REACTIONS, "");
			if (!reactionsStr.isEmpty()) {
				message.setReactions(deserializeReactions(reactionsStr));
			}
		}

		return message;
	}

	/**
	 * Génération d'un fichier pour un Message ({@link Message}).
	 *
	 * @param message Message à générer.
	 */
	public void writeMessageFile(Message message) {
		Properties properties = new Properties();

		// Récupération du chemin pour le fichier à générer
		String destFileName = this.getFileName(message.getUuid(), Constants.MESSAGE_FILE_EXTENSION);

		properties.setProperty(PROPERTY_KEY_UUID, message.getUuid().toString());
		properties.setProperty(PROPERTY_KEY_MESSAGE_SENDER, message.getSender().getUuid().toString());
		properties.setProperty(PROPERTY_KEY_MESSAGE_RECIPIENT, message.getRecipient().toString());
		properties.setProperty(PROPERTY_KEY_MESSAGE_DATE, String.valueOf(message.getEmissionDate()));
		properties.setProperty(PROPERTY_KEY_MESSAGE_TEXT, message.getText());

		// Écrire l'image si présente
		if (message.hasImage()) {
			properties.setProperty(PROPERTY_KEY_MESSAGE_IMAGE, message.getImageBase64());
		}

		// Écrire les réactions
		String reactionsStr = serializeReactions(message.getReactions());
		if (!reactionsStr.isEmpty()) {
			properties.setProperty(PROPERTY_KEY_MESSAGE_REACTIONS, reactionsStr);
		}

		PropertiesManager.writeProperties(properties, destFileName);
	}

	/**
	 * Récupération de l'utilisateur identifié.
	 * 
	 * @param uuid
	 * @param userMap
	 * @return
	 */
	protected User getUserFromUuid(String uuid, Map<UUID, User> userMap) {
		// Récupération de l'utilisateur en fonction de l'UUID
		User user = userMap.get(UUID.fromString(uuid));
		if (user == null) {
			user = userMap.get(Constants.UNKNONWN_USER_UUID);
		}

		return user;
	}

	/**
	 * Retourne un chemin d'accès au fichier pour l'uuid et l'extension donnés.
	 *
	 * @param objectUuid
	 * @param fileExtension
	 */
	protected String getFileName(UUID objectUuid, String fileExtension) {
		return mDirectoryPath + Constants.SYSTEM_FILE_SEPARATOR + objectUuid + "." + fileExtension;
	}

	/**
	 * Configure le chemin d'accès au répertoire d'échange.
	 *
	 * @param directoryPath
	 */
	public void setExchangeDirectory(String directoryPath) {
		this.mDirectoryPath = directoryPath;
	}

	/**
	 * Retourne la liste des identifiants des utilisateurs sour forme d'une chaine
	 * de caractère.
	 * 
	 * @param users
	 */
	protected String getUsersAsString(List<User> users) {
		String usersAsString = "";

		Iterator<User> iterator = users.iterator();
		while (iterator.hasNext()) {
			usersAsString += iterator.next().getUuid();

			if (iterator.hasNext()) {
				usersAsString += USER_SEPARATOR;
			}
		}

		return usersAsString;
	}

	/**
	 * Retourne la liste des utilisateurs depuis une chaine de caractère.
	 * 
	 * @param users
	 * @param userMap
	 */
	protected List<User> getUsersFromString(String users, Map<UUID, User> userMap) {
		List<User> userList = new ArrayList<User>();

		String[] splittedUsers = users.split(USER_SEPARATOR);
		for (String userId : splittedUsers) {
			if (!userId.isEmpty()) {
				userList.add(getUserFromUuid(userId, userMap));
			}
		}

		return userList;
	}

	/**
	 * Supprime le fichier .chn correspondant au canal donné.
	 */
	public void deleteChannelFile(Channel channel) {
		String fileName = this.getFileName(channel.getUuid(), Constants.CHANNEL_FILE_EXTENSION);
		new File(fileName).delete();
	}

	public void deleteMessageFile(Message message) {
		String fileName = this.getFileName(message.getUuid(), Constants.MESSAGE_FILE_EXTENSION);
		new File(fileName).delete();
	}

	public void deleteUserFile(User user) {
		String fileName = this.getFileName(user.getUuid(), Constants.USER_FILE_EXTENSION);
		new File(fileName).delete();
	}

	/** Sérialise les réactions : "heart:uuid1,uuid2|thumbsup:uuid3" */
	private String serializeReactions(Map<String, List<UUID>> reactions) {
		if (reactions == null || reactions.isEmpty()) return "";
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, List<UUID>> entry : reactions.entrySet()) {
			if (!entry.getValue().isEmpty()) {
				if (sb.length() > 0) sb.append("|");
				sb.append(entry.getKey()).append(":");
				sb.append(entry.getValue().stream().map(UUID::toString).collect(Collectors.joining(",")));
			}
		}
		return sb.toString();
	}

	/** Désérialise les réactions depuis la chaîne de propriété */
	private Map<String, List<UUID>> deserializeReactions(String str) {
		Map<String, List<UUID>> reactions = new LinkedHashMap<>();
		if (str == null || str.isEmpty()) return reactions;
		for (String part : str.split("\\|")) {
			String[] kv = part.split(":", 2);
			if (kv.length == 2) {
				List<UUID> uuids = new ArrayList<>();
				for (String u : kv[1].split(",")) {
					try { uuids.add(UUID.fromString(u.trim())); } catch (Exception e) {}
				}
				if (!uuids.isEmpty()) reactions.put(kv[0], uuids);
			}
		}
		return reactions;
	}

	public static String encrypt(String data) {
		return Base64.getEncoder().encodeToString(data.getBytes());
	}

	public static String decrypt(String encryptedData) {
		byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
		return new String(decodedBytes);
	}
}
