package main.java.com.ubo.tp.message.ihm.user;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;

/**
 * Composant graphique affichant la liste des utilisateurs.
 * Cliquer sur un utilisateur le sélectionne pour envoyer un message privé.
 */
public class UserListView extends BorderPane {

	private VBox mListBox;
	private List<UserPanel> mUserPanels = new ArrayList<>();
	private User mSelectedUser = null;
	private String mSearchText = "";

	private UserSelectionListener mSelectionListener;

	public UserListView() {
		this.setStyle("-fx-background-color: #2C2F33;");

		Label titleLabel = new Label("Utilisateurs");
		titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
		titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 11;");

		HBox headerBox = new HBox(5, titleLabel);
		headerBox.setAlignment(Pos.CENTER_LEFT);
		headerBox.setPadding(new Insets(10, 10, 5, 10));

		TextField searchField = new TextField();
		searchField.setPromptText("Rechercher...");
		searchField.setStyle("-fx-background-color: #40444B; -fx-text-fill: white; -fx-prompt-text-fill: #72767D; -fx-border-width: 0; -fx-background-radius: 4;");
		searchField.setPadding(new Insets(4, 8, 4, 8));
		HBox searchBox = new HBox(searchField);
		searchBox.setPadding(new Insets(0, 8, 6, 8));
		HBox.setHgrow(searchField, Priority.ALWAYS);
		searchField.textProperty().addListener((obs, oldVal, newVal) -> {
			mSearchText = newVal == null ? "" : newVal.trim().toLowerCase();
			applyFilter();
		});

		VBox topBox = new VBox(headerBox, searchBox);
		this.setTop(topBox);

		// Liste scrollable
		mListBox = new VBox();
		mListBox.setStyle("-fx-background-color: #2C2F33;");

		ScrollPane scrollPane = new ScrollPane(mListBox);
		scrollPane.setFitToWidth(true);
		scrollPane.setStyle("-fx-background: #2C2F33; -fx-border-width: 0;");
		this.setCenter(scrollPane);
	}

	public void updateUsers(Set<User> users) {
		mUserPanels.clear();

		List<User> sortedUsers = new ArrayList<>(users);
		sortedUsers.sort(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER));

		for (User user : sortedUsers) {
			UserPanel panel = new UserPanel(user);
			panel.addClickListener(() -> selectUser(user));

			if (mSelectedUser != null && mSelectedUser.getUuid().equals(user.getUuid())) {
				panel.setSelected(true);
			}

			mUserPanels.add(panel);
		}
		applyFilter();
	}

	private void applyFilter() {
		mListBox.getChildren().clear();
		for (UserPanel panel : mUserPanels) {
			User u = panel.getUser();
			if (mSearchText.isEmpty()
					|| u.getName().toLowerCase().contains(mSearchText)
					|| u.getUserTag().toLowerCase().contains(mSearchText)) {
				mListBox.getChildren().add(panel);
			}
		}
	}

	private void selectUser(User user) {
		mSelectedUser = user;

		for (UserPanel panel : mUserPanels) {
			panel.setSelected(user != null && panel.getUser().getUuid().equals(user.getUuid()));
		}

		if (mSelectionListener != null) {
			mSelectionListener.onUserSelected(user);
		}
	}

	public User getSelectedUser() {
		return mSelectedUser;
	}

	public void clearSelection() {
		mSelectedUser = null;
		for (UserPanel panel : mUserPanels) {
			panel.setSelected(false);
		}
	}

	public void setSelectionListener(UserSelectionListener listener) {
		mSelectionListener = listener;
	}

	/**
	 * Met à jour les badges de messages non lus pour chaque utilisateur.
	 */
	public void updateBadges(Set<Message> allMessages, Map<UUID, Integer> seenCounts, UUID connectedUserUuid) {
		for (UserPanel panel : mUserPanels) {
			UUID userUuid = panel.getUser().getUuid();
			// Compter les messages envoyés PAR cet utilisateur À MOI (messages privés reçus)
			int totalMessages = (int) allMessages.stream()
					.filter(m -> m.getSender().getUuid().equals(userUuid)
							&& m.getRecipient().equals(connectedUserUuid))
					.count();
			int seenMessages = seenCounts.getOrDefault(userUuid, 0);
			int unread = totalMessages - seenMessages;
			panel.setUnreadCount(Math.max(0, unread));
		}
	}

	public interface UserSelectionListener {
		void onUserSelected(User user);
	}
}
