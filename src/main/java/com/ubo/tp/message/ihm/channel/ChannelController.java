package main.java.com.ubo.tp.message.ihm.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;

import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.database.DbConnector;
import main.java.com.ubo.tp.message.core.session.ISession;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.User;

/**
 * Contrôleur pour la gestion des canaux :
 * - Création d'un canal
 * - Suppression d'un canal
 * - Modification d'un canal (ajout/suppression d'utilisateurs)
 */
public class ChannelController {

	private DbConnector mDbConnector;
	private DataManager mDataManager;
	private ISession mSession;
	private ChannelListView mChannelListView;

	public ChannelController(DbConnector dbConnector, DataManager dataManager, ISession session,
			ChannelListView channelListView) {
		this.mDbConnector = dbConnector;
		this.mDataManager = dataManager;
		this.mSession = session;
		this.mChannelListView = channelListView;

		// Brancher les listeners de la vue
		this.mChannelListView.setCreateChannelListener(() -> handleCreateChannel());
		this.mChannelListView.setDeleteChannelListener(() -> handleDeleteChannel());
		this.mChannelListView.setEditChannelListener(() -> handleEditChannel());
		this.mChannelListView.setLeaveChannelListener(() -> handleLeaveChannel());
	}

	/**
	 * Crée un nouveau canal via une boîte de dialogue.
	 */
	private void handleCreateChannel() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Nouveau canal");
		dialog.setHeaderText("Créer un canal");
		dialog.setContentText("Nom du canal :");

		Optional<String> result = dialog.showAndWait();
		if (result.isPresent() && !result.get().trim().isEmpty()) {
			String name = result.get().trim();
			Channel channel = new Channel(mSession.getConnectedUser(), name);

			// Ajouter en BDD
			mDbConnector.addChannel(channel);

			// Persister dans le répertoire d'échange
			mDataManager.sendChannel(channel);
		}
	}

	/**
	 * Supprime le canal sélectionné (seul le créateur peut supprimer).
	 */
	private void handleDeleteChannel() {
		Channel selected = mChannelListView.getSelectedChannel();
		if (selected == null) {
			showAlert("Aucun canal sélectionné", "Veuillez sélectionner un canal à supprimer.");
			return;
		}

		// Vérifier que l'utilisateur connecté est le créateur
		if (!selected.getCreator().getUuid().equals(mSession.getConnectedUser().getUuid())) {
			showAlert("Action interdite", "Seul le créateur du canal peut le supprimer.");
			return;
		}

		// Confirmation
		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
		confirm.setTitle("Supprimer le canal");
		confirm.setHeaderText("Supprimer « " + selected.getName() + " » ?");
		confirm.setContentText("Cette action est irréversible.");

		Optional<ButtonType> result = confirm.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			// Supprimer de la BDD locale
			mDbConnector.deleteChannel(selected);
			// Supprimer le fichier → l'autre instance le détectera via WatchableDirectory
			mDataManager.deleteChannelFile(selected);
		}
	}

	/**
	 * Modifie le canal sélectionné : ajout/suppression d'utilisateurs.
	 */
	private void handleEditChannel() {
		Channel selected = mChannelListView.getSelectedChannel();
		if (selected == null) {
			showAlert("Aucun canal sélectionné", "Veuillez sélectionner un canal à modifier.");
			return;
		}

		// Vérifier que l'utilisateur connecté est le créateur
		if (!selected.getCreator().getUuid().equals(mSession.getConnectedUser().getUuid())) {
			showAlert("Action interdite", "Seul le créateur du canal peut le modifier.");
			return;
		}

		// Récupérer tous les utilisateurs
		Set<User> allUsers = mDataManager.getUsers();
		List<User> channelMembers = selected.getUsers();

		// Créer un dialogue avec des checkboxes
		Dialog<List<User>> dialog = new Dialog<>();
		dialog.setTitle("Modifier le canal");
		dialog.setHeaderText("Membres de « " + selected.getName() + " »");

		ButtonType okButtonType = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

		VBox content = new VBox(5);
		content.setStyle("-fx-padding: 10;");
		List<CheckBox> checkBoxes = new ArrayList<>();

		for (User user : allUsers) {
			// Ne pas afficher le créateur (il est toujours membre)
			if (user.getUuid().equals(selected.getCreator().getUuid())) {
				continue;
			}

			CheckBox cb = new CheckBox(user.getName() + " (@" + user.getUserTag() + ")");
			cb.setUserData(user);

			// Cocher si déjà membre
			boolean isMember = channelMembers.stream()
					.anyMatch(m -> m.getUuid().equals(user.getUuid()));
			cb.setSelected(isMember);

			checkBoxes.add(cb);
			content.getChildren().add(cb);
		}

		if (checkBoxes.isEmpty()) {
			content.getChildren().add(new Label("Aucun autre utilisateur disponible."));
		}

		ScrollPane scrollPane = new ScrollPane(content);
		scrollPane.setFitToWidth(true);
		scrollPane.setPrefHeight(300);
		dialog.getDialogPane().setContent(scrollPane);

		dialog.setResultConverter(buttonType -> {
			if (buttonType == okButtonType) {
				List<User> selectedUsers = new ArrayList<>();
				// Canal public si personne n'est coché, privé sinon
				for (CheckBox cb : checkBoxes) {
					if (cb.isSelected()) {
						selectedUsers.add((User) cb.getUserData());
					}
				}
				if (!selectedUsers.isEmpty()) {
					selectedUsers.add(0, selected.getCreator());
				}
				return selectedUsers;
			}
			return null;
		});

		Optional<List<User>> result = dialog.showAndWait();
		if (result.isPresent()) {
			// Recréer le canal avec les nouveaux membres
			Channel updatedChannel = new Channel(selected.getUuid(), selected.getCreator(),
					selected.getName(), result.get());
			mDbConnector.modifiyChannel(updatedChannel);
			mDataManager.sendChannel(updatedChannel);
		}
	}

	/**
	 * Quitter un canal privé dont on n'est pas le propriétaire (CHN-005).
	 */
	private void handleLeaveChannel() {
		Channel selected = mChannelListView.getSelectedChannel();
		if (selected == null) {
			showAlert("Aucun canal sélectionné", "Veuillez sélectionner un canal à quitter.");
			return;
		}

		User currentUser = mSession.getConnectedUser();

		if (selected.getCreator().getUuid().equals(currentUser.getUuid())) {
			showAlert("Action interdite", "Vous êtes le créateur. Supprimez le canal plutôt.");
			return;
		}

		List<User> members = selected.getUsers();
		if (members.isEmpty()) {
			showAlert("Canal public", "Impossible de quitter un canal public.");
			return;
		}

		boolean isMember = members.stream().anyMatch(m -> m.getUuid().equals(currentUser.getUuid()));
		if (!isMember) {
			showAlert("Non membre", "Vous n'êtes pas membre de ce canal.");
			return;
		}

		List<User> newMembers = new ArrayList<>(members);
		newMembers.removeIf(m -> m.getUuid().equals(currentUser.getUuid()));

		Channel updatedChannel = new Channel(selected.getUuid(), selected.getCreator(), selected.getName(), newMembers);
		mDbConnector.modifiyChannel(updatedChannel);
		mDataManager.sendChannel(updatedChannel);
	}

	private void showAlert(String title, String content) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}
}
