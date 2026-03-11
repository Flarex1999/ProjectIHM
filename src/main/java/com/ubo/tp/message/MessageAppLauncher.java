package main.java.com.ubo.tp.message;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.database.Database;
import main.java.com.ubo.tp.message.core.database.DbConnector;
import main.java.com.ubo.tp.message.core.database.EntityManager;
import main.java.com.ubo.tp.message.core.database.IDatabaseObserver;
import main.java.com.ubo.tp.message.core.session.Session;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;
import main.java.com.ubo.tp.message.ihm.MessageApp;
import mock.MessageAppMock;

/**
 * Classe de lancement de l'application.
 *
 * C'est le point d'entrée du programme (méthode main).
 * Elle crée et assemble tous les composants de l'application :
 * - La base de données et ses observateurs
 * - Le gestionnaire de données
 * - La session (Séance 2)
 * - L'application principale avec son IHM
 *
 * @author S.Lucas
 */
public class MessageAppLauncher extends Application {

	/**
	 * Indique si le mode bouchoné est activé.
	 */
	protected static boolean IS_MOCK_ENABLED = true;

	@Override
	public void start(Stage primaryStage) {

		// Fermer l'application complètement quand on ferme la fenêtre principale
		Platform.setImplicitExit(true);
		primaryStage.setOnCloseRequest(e -> {
			Platform.exit();
			System.exit(0);
		});

		Database database = new Database();

		// Etape 1 : On s'inscrit comme observateur de la base de données.
		database.addObserver(new IDatabaseObserver() {
			@Override
			public void notifyUserAdded(User addedUser) {
				System.out.println("[BDD] Utilisateur ajouté : " + addedUser);
			}

			@Override
			public void notifyUserDeleted(User deletedUser) {
				System.out.println("[BDD] Utilisateur supprimé : " + deletedUser);
			}

			@Override
			public void notifyUserModified(User modifiedUser) {
				System.out.println("[BDD] Utilisateur modifié : " + modifiedUser);
			}

			@Override
			public void notifyMessageAdded(Message addedMessage) {
				System.out.println("[BDD] Message ajouté : " + addedMessage);
			}

			@Override
			public void notifyMessageDeleted(Message deletedMessage) {
				System.out.println("[BDD] Message supprimé : " + deletedMessage);
			}

			@Override
			public void notifyMessageModified(Message modifiedMessage) {
				System.out.println("[BDD] Message modifié : " + modifiedMessage);
			}

			@Override
			public void notifyChannelAdded(Channel addedChannel) {
				System.out.println("[BDD] Canal ajouté : " + addedChannel);
			}

			@Override
			public void notifyChannelDeleted(Channel deletedChannel) {
				System.out.println("[BDD] Canal supprimé : " + deletedChannel);
			}

			@Override
			public void notifyChannelModified(Channel modifiedChannel) {
				System.out.println("[BDD] Canal modifié : " + modifiedChannel);
			}
		});

		EntityManager entityManager = new EntityManager(database);

		DataManager dataManager = new DataManager(database, entityManager);

		DbConnector dbConnector = new DbConnector(database);

		// Séance 2 : Création de la Session.
		Session session = new Session();

		if (IS_MOCK_ENABLED) {
			MessageAppMock mock = new MessageAppMock(dbConnector, dataManager);
			mock.showGUI();
		}

		// Séance 2 : On passe maintenant la Session et le Stage au constructeur de MessageApp
		MessageApp messageApp = new MessageApp(dataManager, dbConnector, session, primaryStage);
		messageApp.init();
		messageApp.show();
	}

	/**
	 * Launcher.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
