package main.java.com.ubo.tp.message.ihm.channel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
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

import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;

/**
 * Composant graphique affichant la liste des canaux.
 * Cliquer sur un canal le sélectionne, cliquer sur "Tous" désélectionne.
 */
public class ChannelListView extends BorderPane {

	private VBox mListBox;
	private List<ChannelPanel> mChannelPanels = new ArrayList<>();
	private Channel mSelectedChannel = null;
	private String mSearchText = "";

	private ChannelSelectionListener mSelectionListener;
	private Runnable mCreateChannelListener;
	private Runnable mDeleteChannelListener;
	private Runnable mEditChannelListener;
	private Runnable mLeaveChannelListener;

	public ChannelListView() {
		this.setStyle("-fx-background-color: #2C2F33;");

		// Header : titre + boutons
		Label titleLabel = new Label("Canaux");
		titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
		titleLabel.setStyle("-fx-text-fill: white; -fx-text-transform: uppercase; -fx-font-size: 11;");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		String btnStyle = "-fx-background-color: #40444B; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand;";

		Button addButton = new Button("+");
		addButton.setFont(Font.font("Arial", FontWeight.BOLD, 12));
		addButton.setCursor(Cursor.HAND);
		addButton.setStyle(btnStyle);
		addButton.setOnAction(e -> { if (mCreateChannelListener != null) mCreateChannelListener.run(); });

		Button deleteButton = new Button("−");
		deleteButton.setFont(Font.font("Arial", FontWeight.BOLD, 12));
		deleteButton.setCursor(Cursor.HAND);
		deleteButton.setStyle(btnStyle);
		deleteButton.setOnAction(e -> { if (mDeleteChannelListener != null) mDeleteChannelListener.run(); });

		Button editButton = new Button("✎");
		editButton.setFont(Font.font("Arial", 12));
		editButton.setCursor(Cursor.HAND);
		editButton.setStyle(btnStyle);
		editButton.setOnAction(e -> { if (mEditChannelListener != null) mEditChannelListener.run(); });

		Button leaveButton = new Button("⬅");
		leaveButton.setFont(Font.font("Arial", 12));
		leaveButton.setCursor(Cursor.HAND);
		leaveButton.setStyle(btnStyle);
		leaveButton.setOnAction(e -> { if (mLeaveChannelListener != null) mLeaveChannelListener.run(); });

		HBox headerBox = new HBox(4, titleLabel, spacer, addButton, deleteButton, editButton, leaveButton);
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

	public void updateChannels(Set<Channel> channels) {
		mChannelPanels.clear();

		List<Channel> sortedChannels = new ArrayList<>(channels);
		sortedChannels.sort(Comparator.comparing(Channel::getName, String.CASE_INSENSITIVE_ORDER));

		for (Channel channel : sortedChannels) {
			ChannelPanel panel = new ChannelPanel(channel);
			panel.addClickListener(() -> selectChannel(channel));

			if (mSelectedChannel != null && mSelectedChannel.getUuid().equals(channel.getUuid())) {
				panel.setSelected(true);
			}

			mChannelPanels.add(panel);
		}
		applyFilter();
	}

	private void applyFilter() {
		mListBox.getChildren().clear();
		for (ChannelPanel panel : mChannelPanels) {
			if (mSearchText.isEmpty()
					|| panel.getChannel().getName().toLowerCase().contains(mSearchText)) {
				mListBox.getChildren().add(panel);
			}
		}
	}

	private void selectChannel(Channel channel) {
		mSelectedChannel = channel;

		for (ChannelPanel panel : mChannelPanels) {
			panel.setSelected(channel != null && panel.getChannel().getUuid().equals(channel.getUuid()));
		}

		if (mSelectionListener != null) {
			mSelectionListener.onChannelSelected(channel);
		}
	}

	public Channel getSelectedChannel() {
		return mSelectedChannel;
	}

	public void clearSelection() {
		mSelectedChannel = null;
		for (ChannelPanel panel : mChannelPanels) {
			panel.setSelected(false);
		}
	}

	public void setSelectionListener(ChannelSelectionListener listener) {
		mSelectionListener = listener;
	}

	public void setCreateChannelListener(Runnable listener) {
		mCreateChannelListener = listener;
	}

	public void setDeleteChannelListener(Runnable listener) {
		mDeleteChannelListener = listener;
	}

	public void setEditChannelListener(Runnable listener) {
		mEditChannelListener = listener;
	}

	public void setLeaveChannelListener(Runnable listener) {
		mLeaveChannelListener = listener;
	}

	/**
	 * Met à jour les badges de messages non lus sur chaque canal.
	 */
	public void updateBadges(Set<Message> allMessages, Map<UUID, Integer> seenCounts, UUID connectedUserUuid) {
		for (ChannelPanel panel : mChannelPanels) {
			UUID channelUuid = panel.getChannel().getUuid();
			// Ne compter que les messages des AUTRES utilisateurs (pas ses propres messages)
			int totalMessages = (int) allMessages.stream()
					.filter(m -> m.getRecipient().equals(channelUuid)
							&& !m.getSender().getUuid().equals(connectedUserUuid))
					.count();
			int seenMessages = seenCounts.getOrDefault(channelUuid, 0);
			int unread = totalMessages - seenMessages;
			panel.setUnreadCount(Math.max(0, unread));
		}
	}

	public interface ChannelSelectionListener {
		void onChannelSelected(Channel channel);
	}
}
