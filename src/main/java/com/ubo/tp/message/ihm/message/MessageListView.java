package main.java.com.ubo.tp.message.ihm.message;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import main.java.com.ubo.tp.message.datamodel.Message;

/**
 * Composant graphique affichant la liste des messages.
 * Supporte le filtrage par canal (recipient UUID).
 */
public class MessageListView extends BorderPane {

	private VBox mListBox;
	private Label mTitleLabel;
	private Consumer<Message> mDeleteCallback;
	private BiConsumer<Message, String> mReactionCallback;

	public MessageListView() {
		this.setStyle("-fx-background-color: white;");

		mTitleLabel = new Label("Messages");
		mTitleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
		mTitleLabel.setAlignment(Pos.CENTER);
		mTitleLabel.setMaxWidth(Double.MAX_VALUE);
		mTitleLabel.setPadding(new Insets(10, 0, 10, 0));
		this.setTop(mTitleLabel);

		mListBox = new VBox();
		mListBox.setStyle("-fx-background-color: white;");

		ScrollPane scrollPane = new ScrollPane(mListBox);
		scrollPane.setFitToWidth(true);
		scrollPane.setStyle("-fx-background: white; -fx-border-width: 0;");
		this.setCenter(scrollPane);
	}

	public void updateMessages(Set<Message> messages) {
		updateMessages(messages, null, null, null);
	}

	public void updateMessages(Set<Message> messages, UUID filterRecipientUuid, String title) {
		updateMessages(messages, filterRecipientUuid, title, null);
	}

	/**
	 * Affiche les messages filtrés. Pour une conversation privée (user-to-user),
	 * passer connectedUserUuid pour afficher les messages dans les deux sens.
	 */
	public void updateMessages(Set<Message> messages, UUID filterRecipientUuid, String title, String searchText) {
		updateMessages(messages, filterRecipientUuid, title, searchText, null);
	}

	public void updateMessages(Set<Message> messages, UUID filterRecipientUuid, String title, String searchText, UUID connectedUserUuid) {
		updateMessages(messages, filterRecipientUuid, title, searchText, connectedUserUuid, false);
	}

	public void updateMessages(Set<Message> messages, UUID filterRecipientUuid, String title, String searchText, UUID connectedUserUuid, boolean isDM) {
		mListBox.getChildren().clear();

		if (title != null) {
			mTitleLabel.setText(title);
		} else {
			mTitleLabel.setText("Messages");
		}

		List<Message> sortedMessages = new ArrayList<>(messages);
		if (filterRecipientUuid != null) {
			if (isDM && connectedUserUuid != null) {
				// Conversation privée : montrer messages A→B et B→A
				sortedMessages.removeIf(m -> {
					UUID sender = m.getSender().getUuid();
					UUID recipient = m.getRecipient();
					boolean aToB = sender.equals(connectedUserUuid) && recipient.equals(filterRecipientUuid);
					boolean bToA = sender.equals(filterRecipientUuid) && recipient.equals(connectedUserUuid);
					return !(aToB || bToA);
				});
			} else {
				// Filtre canal : tous les messages dont le destinataire est ce canal
				sortedMessages.removeIf(m -> !m.getRecipient().equals(filterRecipientUuid));
			}
		}

		// Filtre de recherche textuelle
		if (searchText != null && !searchText.trim().isEmpty()) {
			String lowerSearch = searchText.trim().toLowerCase();
			sortedMessages.removeIf(m ->
				!m.getText().toLowerCase().contains(lowerSearch)
				&& !m.getSender().getName().toLowerCase().contains(lowerSearch)
				&& !m.getSender().getUserTag().toLowerCase().contains(lowerSearch)
			);
		}

		sortedMessages.sort(Comparator.comparingLong(Message::getEmissionDate));

		for (Message message : sortedMessages) {
			final Message msg = message;
			Runnable deleteCallback = (mDeleteCallback != null && connectedUserUuid != null)
				? () -> mDeleteCallback.accept(msg)
				: null;
			BiConsumer<Message, String> reactCallback = mReactionCallback;
			mListBox.getChildren().add(new MessagePanel(message, connectedUserUuid, deleteCallback, reactCallback));
		}
	}

	public void setDeleteCallback(Consumer<Message> callback) {
		this.mDeleteCallback = callback;
	}

	public void setReactionCallback(BiConsumer<Message, String> callback) {
		this.mReactionCallback = callback;
	}
}
