package mock;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import main.java.com.ubo.tp.message.common.Constants;
import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.database.DbConnector;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;

public class MessageAppMock {

	/**
	 * Fenetre du bouchon
	 */
	protected Stage mStage;

	/**
	 * Connecteur spécifique pour la BDD de l'application.
	 */
	protected DbConnector mDbConnector;

	/**
	 * Gestionnaire de données.
	 */
	protected DataManager mDataManager;

	/**
	 * Constructeur.
	 *
	 * @param dbConnector , lien vers la BDD de l'application.
	 */
	public MessageAppMock(DbConnector dbConnector, DataManager dataManager) {
		this.mDbConnector = dbConnector;
		this.mDataManager = dataManager;
	}

	/**
	 * Lance l'affichage de l'IHM.
	 */
	public void showGUI() {
		// Init auto de l'IHM au cas ou ;)
		if (mStage == null) {
			this.initGUI();
		}

		// Positionnement de la fenêtre
		mStage.setX(100);
		mStage.setY(100);

		// Affichage
		mStage.show();
	}

	/**
	 * Initialisation de l'IHM
	 */
	protected void initGUI() {
		// Création de la fenetre principale
		mStage = new Stage();
		mStage.setTitle("MOCK");

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(5);
		grid.setVgap(5);
		grid.setPadding(new Insets(10));

		//
		// Gestion de la base de données

		Label dbLabel = new Label("Database");
		dbLabel.setStyle("-fx-font-weight: bold;");

		Button addUserButton = new Button("Add User");
		addUserButton.setPrefSize(120, 50);
		addUserButton.setOnAction(e -> addUserInDatabase());

		Button addMessageButton = new Button("Add Message");
		addMessageButton.setPrefSize(120, 50);
		addMessageButton.setOnAction(e -> addMessageInDatabase());

		Button addChannelButton = new Button("Add Channel");
		addChannelButton.setPrefSize(120, 50);
		addChannelButton.setOnAction(e -> addChannelInDatabase());

		//
		// Gestion des fichiers

		Label fileLabel = new Label("Files");
		fileLabel.setStyle("-fx-font-weight: bold;");

		Button sendUserButton = new Button("Write User");
		sendUserButton.setPrefSize(120, 50);
		sendUserButton.setOnAction(e -> writeUser());

		Button sendMessageButton = new Button("Write Message");
		sendMessageButton.setPrefSize(120, 50);
		sendMessageButton.setOnAction(e -> writeMessage());

		Button sendChannelButton = new Button("Write Channel");
		sendChannelButton.setPrefSize(120, 50);
		sendChannelButton.setOnAction(e -> writeChannel());

		//
		// Ajout des composants à la grille
		grid.add(dbLabel, 0, 0, 3, 1);
		GridPane.setHalignment(dbLabel, javafx.geometry.HPos.CENTER);

		grid.add(addUserButton, 0, 1);
		grid.add(addMessageButton, 1, 1);
		grid.add(addChannelButton, 2, 1);

		grid.add(fileLabel, 0, 3, 3, 1);
		GridPane.setHalignment(fileLabel, javafx.geometry.HPos.CENTER);

		grid.add(sendUserButton, 0, 4);
		grid.add(sendMessageButton, 1, 4);
		grid.add(sendChannelButton, 2, 4);

		Scene scene = new Scene(grid);
		mStage.setScene(scene);
		mStage.sizeToScene();
	}

	/**
	 * Ajoute un utilisateur fictif à la base de donnée.
	 */
	protected void addUserInDatabase() {
		// Création d'un utilisateur fictif
		User newUser = this.generateUser();

		// Ajout de l'utilisateur à la base
		this.mDbConnector.addUser(newUser);
	}

	/**
	 * Génération et envoi d'un fichier utilisateur
	 */
	protected void writeUser() {
		// Création d'un utilisateur fictif
		User newUser = this.generateUser();

		// Génération du fichier utilisateur
		this.mDataManager.sendUser(newUser);
	}

	/**
	 * Génération d'un utilisateur fictif.
	 */
	protected User generateUser() {
		int randomInt = new Random().nextInt(99999);
		String userName = "MockUser" + randomInt;
		User newUser = new User(UUID.randomUUID(), userName, "This_Is_Not_A_Password", userName);

		return newUser;
	}

	/**
	 * Ajoute un message fictif à la base de données.
	 */
	protected void addMessageInDatabase() {
		// Création d'un message fictif
		Message newMessage = this.generateMessage();

		// Ajout du message
		this.mDbConnector.addMessage(newMessage);
	}

	/**
	 * Génération et envoi d'un fichier message
	 */
	protected void writeMessage() {
		// Création d'un message fictif
		Message newMessage = this.generateMessage();

		// Génération du fichier message
		this.mDataManager.sendMessage(newMessage);
	}

	/**
	 * Ajoute un canal fictif à la base de données.
	 */
	protected void addChannelInDatabase() {
		// Création d'un canal fictif
		Channel newChannel = this.generateChannel();

		// Ajout du message
		this.mDbConnector.addChannel(newChannel);
	}

	/**
	 * Génération et envoi d'un fichier canal
	 */
	protected void writeChannel() {
		Channel newChannel = this.generateChannel();

		// Génération du fichier message
		this.mDataManager.sendChannel(newChannel);
	}

	/**
	 * Génération d'un message fictif.
	 */
	protected Message generateMessage() {
		// Si la base n'a pas d'utilisateur
		if (this.mDataManager.getUsers().size() == 0) {
			// Création d'un utilisateur
			this.addUserInDatabase();
		}

		// Récupération d'un utilisateur au hazard
		int userIndex = new Random().nextInt(this.mDataManager.getUsers().size());
		User randomUser = new ArrayList<>(this.mDataManager.getUsers()).get(Math.max(0, userIndex - 1));

		// Création d'un message fictif
		Message newMessage = new Message(randomUser, Constants.UNKNONWN_USER_UUID, "Message fictif!! #Mock #test ;)");

		return newMessage;
	}

	/**
	 * Génération d'un canal fictif.
	 */
	protected Channel generateChannel() {
		// Si la base n'a pas d'utilisateur
		if (this.mDataManager.getUsers().size() == 0) {
			// Création d'un utilisateur
			this.addUserInDatabase();
		}

		// Récupération d'un utilisateur au hazard
		int userIndex = new Random().nextInt(this.mDataManager.getUsers().size());
		User randomUser = new ArrayList<>(this.mDataManager.getUsers()).get(Math.max(0, userIndex - 1));

		// Création d'un canal fictif
		Channel newChannel = new Channel(randomUser, "Canal fictif");

		return newChannel;
	}
}
