package main.java.com.ubo.tp.message.ihm;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * Classe de la vue principale de l'application.
 * Gère la fenêtre principale (Stage), la barre de menu, et le contenu central.
 *
 * Séance 2 : Ajout de la gestion du panneau de contenu central (pour afficher
 * le formulaire de login ou le contenu principal) et du menu "Se déconnecter"
 * (SRS-MAP-USR-005).
 */
public class MessageAppMainView {

	/**
	 * Fenêtre principale de l'application.
	 */
	protected Stage mStage;

	/**
	 * Layout racine de la scène.
	 */
	protected BorderPane mRootPane;

	/**
	 * Menu item "Se déconnecter" dans le menu Fichier.
	 * Désactivé par défaut (personne n'est connecté au lancement).
	 * Activé quand un utilisateur se connecte (SRS-MAP-USR-005).
	 */
	protected MenuItem mDeconnexionItem;
	protected MenuItem mProfilItem;

	/**
	 * Constructeur.
	 */
	public MessageAppMainView(Stage stage) {
		this.mStage = stage;
		this.mRootPane = new BorderPane();
		this.initFrame();
		this.initMenuBar();
	}

	/**
	 * Initialisation de la fenêtre principale.
	 */
	protected void initFrame() {
		mStage.setTitle("MessageApp");
		mStage.setWidth(800);
		mStage.setHeight(600);

		// Logo dans la barre de titre de la fenêtre
		try {
			Image logoIcon = new Image("file:src/main/resources/images/logo_20.png");
			mStage.getIcons().add(logoIcon);
		} catch (Exception e) {
			// Logo non trouvé
		}

		Scene scene = new Scene(mRootPane);
		mStage.setScene(scene);
	}

	/**
	 * Initialisation de la barre de menu.
	 */
	protected void initMenuBar() {
		MenuBar menuBar = new MenuBar();

		// --- Menu "Fichier" ---
		Menu fichierMenu = new Menu("Fichier");

		// Séance 2 : Menu item "Se déconnecter" (SRS-MAP-USR-005)
		mProfilItem = new MenuItem("Mon profil");
		mProfilItem.setDisable(true);
		fichierMenu.getItems().add(mProfilItem);

		mDeconnexionItem = new MenuItem("Se déconnecter");
		mDeconnexionItem.setDisable(true);
		fichierMenu.getItems().add(mDeconnexionItem);

		// Séparateur visuel
		fichierMenu.getItems().add(new SeparatorMenuItem());

		// Menu item "Quitter"
		MenuItem quitterItem = new MenuItem("Quitter");
		try {
			Image exitIcon = new Image("file:src/main/resources/images/exitIcon_20.png");
			quitterItem.setGraphic(new ImageView(exitIcon));
		} catch (Exception e) {
			// Icône non trouvée
		}
		quitterItem.setOnAction(e -> System.exit(0));
		fichierMenu.getItems().add(quitterItem);

		// --- Menu "?" ---
		Menu helpMenu = new Menu("?");

		MenuItem aProposItem = new MenuItem("A propos");
		try {
			Image logoIcon = new Image("file:src/main/resources/images/logo_20.png");
			aProposItem.setGraphic(new ImageView(logoIcon));
		} catch (Exception e) {
			// Icône non trouvée
		}
		aProposItem.setOnAction(e -> showAboutDialog());
		helpMenu.getItems().add(aProposItem);

		menuBar.getMenus().addAll(fichierMenu, helpMenu);
		mRootPane.setTop(menuBar);
	}

	/**
	 * Séance 2 : Remplace le panneau de contenu central de la fenêtre.
	 *
	 * @param panel le nouveau panneau à afficher dans la zone centrale
	 */
	public void setContentPanel(Pane panel) {
		mRootPane.setCenter(panel);
	}

	/**
	 * Séance 2 : Ajoute un écouteur sur le menu item "Se déconnecter".
	 *
	 * @param listener l'écouteur à ajouter
	 */
	public void addProfilListener(EventHandler<ActionEvent> listener) {
		mProfilItem.setOnAction(listener);
	}

	public void setProfilEnabled(boolean enabled) {
		mProfilItem.setDisable(!enabled);
	}

	public void addDeconnexionListener(EventHandler<ActionEvent> listener) {
		mDeconnexionItem.setOnAction(listener);
	}

	/**
	 * Séance 2 : Active ou désactive le menu item "Se déconnecter".
	 *
	 * @param enabled true pour activer, false pour désactiver
	 */
	public void setDeconnexionEnabled(boolean enabled) {
		mDeconnexionItem.setDisable(!enabled);
	}

	/**
	 * Etape 5 : Affiche la boîte de dialogue "A propos".
	 */
	protected void showAboutDialog() {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("A propos");
		alert.setHeaderText(null);
		alert.setContentText("UBO M2-TIIL\nDépartement Informatique");

		try {
			Image logoIcon = new Image("file:src/main/resources/images/logo_50.png");
			alert.setGraphic(new ImageView(logoIcon));
		} catch (Exception e) {
			// Logo non trouvé
		}

		alert.showAndWait();
	}

	/**
	 * Etape 6 : Ouvre un sélecteur de répertoire pour choisir le répertoire d'échange.
	 *
	 * @return le chemin du répertoire choisi, ou null si annulé.
	 */
	public String showDirectoryChooser() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Choisir le répertoire d'échange");

		File selectedDirectory = directoryChooser.showDialog(mStage);
		if (selectedDirectory != null) {
			return selectedDirectory.getAbsolutePath();
		}
		return null;
	}

	/**
	 * Affiche la fenêtre.
	 */
	public void show() {
		mStage.show();
	}

	/**
	 * Retourne le Stage.
	 */
	public Stage getStage() {
		return mStage;
	}
}
