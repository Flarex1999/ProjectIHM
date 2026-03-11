package main.java.com.ubo.tp.message.ihm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import java.util.Random;
import java.util.function.BiConsumer;

import main.java.com.ubo.tp.message.common.EmojiImageUtil;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.database.DbConnector;
import main.java.com.ubo.tp.message.core.database.IDatabaseObserver;
import main.java.com.ubo.tp.message.core.session.ISession;
import main.java.com.ubo.tp.message.core.session.ISessionObserver;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;
import main.java.com.ubo.tp.message.ihm.login.LoginController;
import main.java.com.ubo.tp.message.ihm.login.LoginView;
import main.java.com.ubo.tp.message.ihm.channel.ChannelController;
import main.java.com.ubo.tp.message.ihm.message.SendMessageController;
import main.java.com.ubo.tp.message.ihm.signup.SignupController;
import main.java.com.ubo.tp.message.ihm.signup.SignupView;

/**
 * Classe principale de l'application.
 *
 * Orchestre la navigation entre les vues :
 * - SignupView (inscription) <-> LoginView (connexion) via les liens
 * - Après connexion : écran d'accueil
 * - Après déconnexion : retour à la SignupView
 *
 * Implémente ISessionObserver pour réagir aux connexions/déconnexions.
 *
 * @author S.Lucas
 */
public class MessageApp implements ISessionObserver {

	/** Gestionnaire de données de l'application */
	protected DataManager mDataManager;

	/** Connecteur direct à la base de données */
	protected DbConnector mDbConnector;

	/** Session de l'application (utilisateur connecté) */
	protected ISession mSession;

	/** Vue principale (fenêtre Stage + menu) */
	protected MessageAppMainView mMainView;

	/** Stage JavaFX */
	protected Stage mStage;

	/** Vue du formulaire d'inscription */
	protected SignupView mSignupView;

	/** Contrôleur de l'inscription */
	protected SignupController mSignupController;

	/** Vue du formulaire de connexion */
	protected LoginView mLoginView;

	/** Contrôleur de la connexion */
	protected LoginController mLoginController;

	/** Séance 3 : Vue principale après connexion (messages, utilisateurs, canaux) */
	protected MainContentView mMainContentView;

	/** Séance 3 : Contrôleur d'envoi de message */
	protected SendMessageController mSendMessageController;

	/** Séance 4 : Contrôleur des canaux */
	protected ChannelController mChannelController;

	/** Timestamp de connexion — les easter eggs ne s'activent que sur les messages postérieurs */
	protected long mLoginTime = 0;

	/** Référence à l'utilisateur connecté localement (pour le logout) */
	protected User mLocalConnectedUser;

	/**
	 * Constructeur.
	 *
	 * @param dataManager le gestionnaire de données
	 * @param dbConnector le connecteur à la base de données
	 * @param session     la session de l'application
	 * @param stage       le Stage JavaFX
	 */
	public MessageApp(DataManager dataManager, DbConnector dbConnector, ISession session, Stage stage) {
		this.mDataManager = dataManager;
		this.mDbConnector = dbConnector;
		this.mSession = session;
		this.mStage = stage;

		// S'inscrire comme observateur de la session
		this.mSession.addObserver(this);
	}

	/**
	 * Initialisation de l'application.
	 */
	public void init() {
		this.initGui();
		this.initDirectory();
	}

	/**
	 * Initialisation de l'interface graphique.
	 *
	 * Crée les deux vues (inscription et connexion) avec leurs contrôleurs,
	 * branche la navigation entre les vues, et affiche l'inscription par défaut.
	 */
	protected void initGui() {
		// 1. Vue principale (fenêtre + menu)
		this.mMainView = new MessageAppMainView(mStage);

		// 2. Vue et contrôleur d'inscription
		this.mSignupView = new SignupView();
		this.mSignupController = new SignupController(mDataManager, mSession, mSignupView);

		// 3. Vue et contrôleur de connexion
		this.mLoginView = new LoginView();
		this.mLoginController = new LoginController(mDataManager, mSession, mLoginView);

		// 4. Navigation entre les vues :
		//    Clic sur "Déjà un compte ? Se connecter" -> affiche LoginView
		this.mSignupView.addSwitchToLoginListener(e -> showLoginView());

		//    Clic sur "Pas de compte ? S'inscrire" -> affiche SignupView
		this.mLoginView.addSwitchToSignupListener(e -> showSignupView());

		// 5. Afficher l'inscription par défaut
		this.mMainView.setContentPanel(mSignupView);

		// 8. Gérer la fermeture de la fenêtre → mettre le statut offline
		mStage.setOnCloseRequest(e -> {
			if (mLocalConnectedUser != null) {
				mLocalConnectedUser.setOnline(false);
				try { mDataManager.sendUser(mLocalConnectedUser); } catch (Exception ex) { /* ignoré */ }
			}
			Platform.exit();
		});

		// 6. Brancher le menu "Se déconnecter" sur la session
		this.mMainView.addDeconnexionListener(e -> mSession.disconnect());

		// 7. Brancher le menu "Mon profil"
		this.mMainView.addProfilListener(e -> handleProfil());
	}

	/**
	 * Affiche la vue d'inscription dans la fenêtre.
	 */
	private void showSignupView() {
		mSignupView.resetFields();
		mMainView.setContentPanel(mSignupView);
	}

	/**
	 * Affiche la vue de connexion dans la fenêtre.
	 */
	private void showLoginView() {
		mLoginView.resetFields();
		mMainView.setContentPanel(mLoginView);
	}

	/**
	 * Initialisation du répertoire d'échange.
	 */
	protected void initDirectory() {
		String directoryPath = this.mMainView.showDirectoryChooser();

		if (directoryPath != null && this.isValidExchangeDirectory(new File(directoryPath))) {
			this.initDirectory(directoryPath);
		}
	}

	protected boolean isValidExchangeDirectory(File directory) {
		return directory != null && directory.exists() && directory.isDirectory() && directory.canRead()
				&& directory.canWrite();
	}

	protected void initDirectory(String directoryPath) {
		mDataManager.setExchangeDirectory(directoryPath);
	}

	/**
	 * Affiche la fenêtre principale.
	 */
	public void show() {
		this.mMainView.show();
	}

	// ===================================================================
	// IMPLÉMENTATION DE ISessionObserver
	// ===================================================================

	/**
	 * Notification de connexion (SRS-MAP-USR-004).
	 * Appelée après inscription ou connexion réussie.
	 *
	 * @param connectedUser l'utilisateur connecté
	 */
	@Override
	public void notifyLogin(User connectedUser) {
		// Horodater la connexion (les easter eggs ne s'activent que sur les messages reçus après ce timestamp)
		mLoginTime = System.currentTimeMillis();

		// Sauvegarder la référence pour le logout
		mLocalConnectedUser = connectedUser;

		// Marquer l'utilisateur comme en ligne et sauvegarder le fichier
		connectedUser.setOnline(true);
		mDataManager.sendUser(connectedUser);

		// 1. Activer les menus
		mMainView.setDeconnexionEnabled(true);
		mMainView.setProfilEnabled(true);

		// 2. Mettre à jour le titre
		mMainView.getStage().setTitle("MessageApp - " + connectedUser.getName());

		// 3. Séance 3 : Créer la vue principale avec messages, utilisateurs, canaux
		mMainContentView = new MainContentView(connectedUser.getUuid());

		// 4. Créer le contrôleur d'envoi de message
		mSendMessageController = new SendMessageController(mDbConnector, mDataManager, mSession, mMainContentView.getSendMessageView(), mMainContentView);

		// 4b. Séance 4 : Créer le contrôleur des canaux
		mChannelController = new ChannelController(mDbConnector, mDataManager, mSession, mMainContentView.getChannelListView());

		// 4c. Brancher la suppression de message (MSG-006)
		mMainContentView.setDeleteMessageCallback(message -> {
			mDbConnector.deleteMessage(message);
			mDataManager.deleteMessageFile(message);
		});

		// 4d. Brancher les réactions (Séance 6)
		mMainContentView.setReactionCallback((message, emojiCode) -> {
			message.toggleReaction(emojiCode, connectedUser.getUuid());
			mDbConnector.modifiyMessage(message);
			mDataManager.sendMessage(message);
		});

		// 5. Observer la base de données pour rafraîchir les listes automatiquement
		mDataManager.addObserver(new IDatabaseObserver() {
			@Override
			public void notifyUserAdded(User addedUser) { Platform.runLater(() -> MessageApp.this.refreshMainContent()); }
			@Override
			public void notifyUserDeleted(User deletedUser) { Platform.runLater(() -> MessageApp.this.refreshMainContent()); }
			@Override
			public void notifyUserModified(User modifiedUser) { Platform.runLater(() -> MessageApp.this.refreshMainContent()); }
			@Override
			public void notifyMessageAdded(Message addedMessage) {
				Platform.runLater(() -> {
					MessageApp.this.refreshMainContent();
					// MSG-010 : notifier si DM ou mention
					if (mLocalConnectedUser != null
							&& !addedMessage.getSender().getUuid().equals(mLocalConnectedUser.getUuid())) {
						boolean isDirect = addedMessage.getRecipient().equals(mLocalConnectedUser.getUuid());
						boolean isMentioned = addedMessage.getText().contains("@" + mLocalConnectedUser.getUserTag());
						if ((isDirect || isMentioned) && addedMessage.getEmissionDate() > mLoginTime) {
							showNotification(addedMessage.getSender().getName(), addedMessage.getText());
						}
					}
					// Easter eggs (Séance 6) — uniquement pour les messages reçus après la connexion
					if (addedMessage.getEmissionDate() > mLoginTime) {
						String txt = addedMessage.getText().trim();
						if ("/party".equals(txt)) triggerParty();
						else if ("/flip".equals(txt)) triggerFlip();
						else if ("/earthquake".equals(txt)) triggerEarthquake();
					}
				});
			}
			@Override
			public void notifyMessageDeleted(Message deletedMessage) { Platform.runLater(() -> MessageApp.this.refreshMainContent()); }
			@Override
			public void notifyMessageModified(Message modifiedMessage) { Platform.runLater(() -> MessageApp.this.refreshMainContent()); }
			@Override
			public void notifyChannelAdded(Channel addedChannel) { Platform.runLater(() -> MessageApp.this.refreshMainContent()); }
			@Override
			public void notifyChannelDeleted(Channel deletedChannel) { Platform.runLater(() -> MessageApp.this.refreshMainContent()); }
			@Override
			public void notifyChannelModified(Channel modifiedChannel) { Platform.runLater(() -> MessageApp.this.refreshMainContent()); }
		});

		// 6. Charger les données initiales
		refreshMainContent();

		// 7. Afficher la vue principale
		mMainView.setContentPanel(mMainContentView);
	}

	/**
	 * USR-009 : Modifier le nom. USR-010 : Supprimer le compte.
	 */
	private void handleProfil() {
		User user = mLocalConnectedUser;
		if (user == null) return;

		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.setTitle("Mon profil");
		dialog.setHeaderText("Profil : @" + user.getUserTag());

		TextField nameField = new TextField(user.getName());
		nameField.setPromptText("Nom d'utilisateur");

		javafx.scene.control.Button deleteBtn = new javafx.scene.control.Button("Supprimer mon compte");
		deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white;");

		VBox content = new VBox(8,
			new Label("Nom :"), nameField,
			new javafx.scene.control.Separator(),
			deleteBtn
		);
		content.setPadding(new Insets(10));

		dialog.getDialogPane().setContent(content);
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		deleteBtn.setOnAction(e -> {
			dialog.close();
			handleDeleteAccount();
		});

		Optional<ButtonType> result = dialog.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			String newName = nameField.getText().trim();
			if (!newName.isEmpty() && !newName.equals(user.getName())) {
				user.setName(newName);
				mDbConnector.modifiyUser(user);
				mDataManager.sendUser(user);
				mMainView.getStage().setTitle("MessageApp - " + newName);
			}
		}
	}

	private void handleDeleteAccount() {
		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
		confirm.setTitle("Supprimer le compte");
		confirm.setHeaderText("Supprimer votre compte ?");
		confirm.setContentText("Cette action est irréversible.");

		Optional<ButtonType> result = confirm.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			User userToDelete = mLocalConnectedUser;
			// Marquer comme supprimé et hors-ligne (sans effacer le fichier)
			userToDelete.setDeleted(true);
			userToDelete.setOnline(false);
			mLocalConnectedUser = null; // empêche notifyLogout de réécrire le fichier
			mDbConnector.modifiyUser(userToDelete);
			mDataManager.sendUser(userToDelete);
			mSession.disconnect();
		}
	}

	/**
	 * MSG-010 : Affiche un toast de notification en bas à droite de la fenêtre.
	 */
	private void showNotification(String senderName, String messageText) {
		String display = messageText.length() > 60 ? messageText.substring(0, 60) + "…" : messageText;

		Label notifLabel = new Label("💬 " + senderName + " : " + display);
		notifLabel.setStyle(
			"-fx-background-color: #2C3E50; -fx-text-fill: white;" +
			"-fx-padding: 10 16; -fx-background-radius: 8;" +
			"-fx-font-size: 12; -fx-max-width: 320; -fx-wrap-text: true;"
		);
		notifLabel.setWrapText(true);
		notifLabel.setMaxWidth(320);

		Popup popup = new Popup();
		popup.getContent().add(notifLabel);
		popup.setAutoHide(true);

		Stage stage = mMainView.getStage();
		popup.show(stage,
			stage.getX() + stage.getWidth() - 350,
			stage.getY() + stage.getHeight() - 110
		);

		PauseTransition pause = new PauseTransition(Duration.seconds(4));
		pause.setOnFinished(e -> popup.hide());
		pause.play();
	}

	// ===================================================================
	// EASTER EGGS (Séance 6)
	// ===================================================================

	private void triggerEarthquake() {
		Node root = mMainView.getStage().getScene().getRoot();
		Timeline shake = new Timeline(
			new KeyFrame(Duration.millis(0),   new KeyValue(root.translateXProperty(), 0)),
			new KeyFrame(Duration.millis(80),  new KeyValue(root.translateXProperty(), 14)),
			new KeyFrame(Duration.millis(160), new KeyValue(root.translateXProperty(), -14)),
			new KeyFrame(Duration.millis(240), new KeyValue(root.translateXProperty(), 10)),
			new KeyFrame(Duration.millis(320), new KeyValue(root.translateXProperty(), -10)),
			new KeyFrame(Duration.millis(400), new KeyValue(root.translateXProperty(), 6)),
			new KeyFrame(Duration.millis(480), new KeyValue(root.translateXProperty(), -6)),
			new KeyFrame(Duration.millis(560), new KeyValue(root.translateXProperty(), 3)),
			new KeyFrame(Duration.millis(640), new KeyValue(root.translateXProperty(), -3)),
			new KeyFrame(Duration.millis(700), new KeyValue(root.translateXProperty(), 0))
		);
		shake.play();
	}

	private void triggerFlip() {
		Node root = mMainView.getStage().getScene().getRoot();
		RotateTransition flip = new RotateTransition(Duration.seconds(0.8), root);
		flip.setFromAngle(0);
		flip.setToAngle(180);
		flip.setAutoReverse(true);
		flip.setCycleCount(2);
		flip.play();
	}

	private void triggerParty() {
		Stage stage = mMainView.getStage();
		Pane pane = new Pane();
		pane.setMouseTransparent(true);
		pane.setPrefSize(stage.getWidth(), stage.getHeight());

		String[] colors = {"#E74C3C","#3498DB","#2ECC71","#F39C12","#9B59B6","#1DA1F2","#E91E63"};
		String[] shapes = {"●","■","▲","★","♦"};
		Random rand = new Random();
		List<Label> confetti = new ArrayList<>();
		for (int i = 0; i < 60; i++) {
			Label lbl = new Label(shapes[rand.nextInt(shapes.length)]);
			lbl.setStyle("-fx-text-fill:" + colors[rand.nextInt(colors.length)]
				+ ";-fx-font-size:" + (10 + rand.nextInt(14)) + ";");
			lbl.setLayoutX(rand.nextDouble() * stage.getWidth());
			lbl.setLayoutY(-20);
			pane.getChildren().add(lbl);
			confetti.add(lbl);
		}

		Popup popup = new Popup();
		popup.getContent().add(pane);
		popup.show(stage, stage.getX(), stage.getY());

		Timeline anim = new Timeline();
		for (Label lbl : confetti) {
			double dx = (rand.nextDouble() - 0.5) * 120;
			anim.getKeyFrames().addAll(
				new KeyFrame(Duration.ZERO,
					new KeyValue(lbl.layoutYProperty(), -20),
					new KeyValue(lbl.layoutXProperty(), lbl.getLayoutX())),
				new KeyFrame(Duration.seconds(3),
					new KeyValue(lbl.layoutYProperty(), stage.getHeight() + 20),
					new KeyValue(lbl.layoutXProperty(), lbl.getLayoutX() + dx))
			);
		}
		anim.setOnFinished(e -> popup.hide());
		anim.play();
	}

	/**
	 * Séance 3 : Rafraîchit les listes de messages, utilisateurs et canaux.
	 * Appelée à chaque modification de la base de données.
	 */
	private void refreshMainContent() {
		if (mMainContentView != null) {
			mMainContentView.updateAll(
					mDataManager.getMessages(),
					mDataManager.getUsers(),
					mDataManager.getChannels()
			);
		}
	}

	/**
	 * Notification de déconnexion (SRS-MAP-USR-005).
	 * Retourne à la vue d'inscription.
	 */
	@Override
	public void notifyLogout() {
		// Marquer l'utilisateur comme hors ligne et sauvegarder le fichier
		if (mLocalConnectedUser != null) {
			mLocalConnectedUser.setOnline(false);
			mDataManager.sendUser(mLocalConnectedUser);
			mLocalConnectedUser = null;
		}

		// 1. Désactiver les menus
		mMainView.setDeconnexionEnabled(false);
		mMainView.setProfilEnabled(false);

		// 2. Remettre le titre par défaut
		mMainView.getStage().setTitle("MessageApp");

		// 3. Ré-afficher la vue d'inscription
		showSignupView();
	}
}
