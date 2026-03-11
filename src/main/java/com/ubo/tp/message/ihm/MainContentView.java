package main.java.com.ubo.tp.message.ihm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import main.java.com.ubo.tp.message.common.Constants;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;
import main.java.com.ubo.tp.message.ihm.channel.ChannelListView;
import main.java.com.ubo.tp.message.ihm.message.MessageListView;
import main.java.com.ubo.tp.message.ihm.message.SendMessageView;
import main.java.com.ubo.tp.message.ihm.user.UserListView;

/**
 * Vue principale affichée après connexion.
 */
public class MainContentView extends BorderPane {

	private MessageListView mMessageListView;
	private SendMessageView mSendMessageView;
	private UserListView mUserListView;
	private ChannelListView mChannelListView;
	private TextField mSearchField;
	private StackPane mRightPanel;
	private VBox mPlaceholder;

	private Channel mSelectedChannel = null;
	private User mSelectedUser = null;
	private Set<Message> mAllMessages;
	private Map<UUID, Integer> mSeenMessageCounts = new HashMap<>();
	private UUID mConnectedUserUuid;
	private boolean mInitialLoadDone = false;
	private Label mConversationHeaderLabel;

	public MainContentView(UUID connectedUserUuid) {
		this.mConnectedUserUuid = connectedUserUuid;
		this.initComponents();
	}

	private void initComponents() {
		mUserListView = new UserListView();
		mChannelListView = new ChannelListView();

		mChannelListView.setSelectionListener(channel -> {
			mSelectedChannel = channel;
			mSelectedUser = null;
			mUserListView.clearSelection();
			if (channel != null) {
				markAsSeen(channel.getUuid(), false);
				updateConversationHeader("# " + channel.getName());
			}
			refreshMessages();
			updateRightPanel();
		});

		mUserListView.setSelectionListener(user -> {
			mSelectedUser = user;
			mSelectedChannel = null;
			mChannelListView.clearSelection();
			if (user != null) {
				markAsSeen(user.getUuid(), true);
				updateConversationHeader(user.getName() + "  @" + user.getUserTag());
			}
			// Désactiver l'envoi si l'utilisateur est supprimé
			mSendMessageView.setDisable(user != null && user.isDeleted());
			refreshMessages();
			updateRightPanel();
		});

		SplitPane leftSplitPane = new SplitPane(mUserListView, mChannelListView);
		leftSplitPane.setOrientation(Orientation.VERTICAL);
		leftSplitPane.setDividerPositions(0.5);
		leftSplitPane.setStyle("-fx-background-color: #2C2F33;");

		// Placeholder quand rien n'est sélectionné
		mPlaceholder = new VBox(10);
		mPlaceholder.setAlignment(Pos.CENTER);
		mPlaceholder.setStyle("-fx-background-color: #F8F9FA;");
		Label placeholderIcon = new Label("💬");
		placeholderIcon.setFont(Font.font("Arial", 48));
		Label placeholderText = new Label("Sélectionnez une conversation");
		placeholderText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
		placeholderText.setStyle("-fx-text-fill: #95A5A6;");
		Label placeholderSub = new Label("Choisissez un utilisateur ou un canal");
		placeholderSub.setFont(Font.font("Arial", 12));
		placeholderSub.setStyle("-fx-text-fill: #BDC3C7;");
		mPlaceholder.getChildren().addAll(placeholderIcon, placeholderText, placeholderSub);

		// Panneau droit : recherche + messages + saisie
		BorderPane conversationPanel = new BorderPane();
		conversationPanel.setStyle("-fx-background-color: white;");

		// Header de conversation (nom du destinataire)
		mConversationHeaderLabel = new Label("Sélectionnez une conversation");
		mConversationHeaderLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
		mConversationHeaderLabel.setStyle("-fx-text-fill: #2C3E50;");
		HBox conversationHeader = new HBox(mConversationHeaderLabel);
		conversationHeader.setAlignment(Pos.CENTER_LEFT);
		conversationHeader.setPadding(new Insets(10, 16, 10, 16));
		conversationHeader.setStyle("-fx-background-color: white; -fx-border-color: #EBEBEB; -fx-border-width: 0 0 1 0;");

		mSearchField = new TextField();
		mSearchField.setPromptText("Rechercher dans les messages...");
		mSearchField.setPadding(new Insets(6, 10, 6, 10));
		mSearchField.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #EBEBEB; -fx-border-radius: 4;");
		mSearchField.textProperty().addListener((obs, oldVal, newVal) -> refreshMessages());
		BorderPane searchBar = new BorderPane(mSearchField);
		searchBar.setPadding(new Insets(8));
		searchBar.setStyle("-fx-background-color: white; -fx-border-color: #EBEBEB; -fx-border-width: 0 0 1 0;");

		VBox topBar = new VBox(conversationHeader, searchBar);

		mMessageListView = new MessageListView();
		mSendMessageView = new SendMessageView();

		conversationPanel.setTop(topBar);
		conversationPanel.setCenter(mMessageListView);
		conversationPanel.setBottom(mSendMessageView);

		mRightPanel = new StackPane(mPlaceholder, conversationPanel);
		conversationPanel.setVisible(false);

		// Stocker la référence pour updateRightPanel
		mRightPanel.setUserData(conversationPanel);

		SplitPane mainSplitPane = new SplitPane(leftSplitPane, mRightPanel);
		mainSplitPane.setOrientation(Orientation.HORIZONTAL);
		mainSplitPane.setDividerPositions(0.28);

		this.setCenter(mainSplitPane);
	}

	private void updateRightPanel() {
		boolean hasSelection = mSelectedChannel != null || mSelectedUser != null;
		BorderPane conversation = (BorderPane) mRightPanel.getUserData();
		mPlaceholder.setVisible(!hasSelection);
		conversation.setVisible(hasSelection);
	}

	private void refreshMessages() {
		if (mAllMessages == null) return;
		String searchText = mSearchField != null ? mSearchField.getText() : null;

		if (mSelectedChannel != null) {
			mMessageListView.updateMessages(mAllMessages, mSelectedChannel.getUuid(), "# " + mSelectedChannel.getName(), searchText, mConnectedUserUuid, false);
		} else if (mSelectedUser != null) {
			mMessageListView.updateMessages(mAllMessages, mSelectedUser.getUuid(), "✉ " + mSelectedUser.getName(), searchText, mConnectedUserUuid, true);
		}
	}

	public void updateConversationHeader(String title) {
		if (mConversationHeaderLabel != null) {
			mConversationHeaderLabel.setText(title);
		}
	}

	public UUID getSelectedRecipientUuid() {
		if (mSelectedChannel != null) return mSelectedChannel.getUuid();
		if (mSelectedUser != null) return mSelectedUser.getUuid();
		return null;
	}

	@Deprecated
	public UUID getSelectedChannelUuid() {
		return getSelectedRecipientUuid();
	}

	public MessageListView getMessageListView() { return mMessageListView; }
	public SendMessageView getSendMessageView() { return mSendMessageView; }
	public UserListView getUserListView() { return mUserListView; }
	public ChannelListView getChannelListView() { return mChannelListView; }

	public void setDeleteMessageCallback(Consumer<Message> callback) {
		mMessageListView.setDeleteCallback(callback);
	}

	public void setReactionCallback(java.util.function.BiConsumer<Message, String> callback) {
		mMessageListView.setReactionCallback(callback);
	}

	private void markAsSeen(UUID targetUuid, boolean isUser) {
		if (mAllMessages == null) return;
		int count;
		if (isUser) {
			count = (int) mAllMessages.stream()
					.filter(m -> m.getSender().getUuid().equals(targetUuid) && m.getRecipient().equals(mConnectedUserUuid))
					.count();
		} else {
			count = (int) mAllMessages.stream()
					.filter(m -> m.getRecipient().equals(targetUuid) && !m.getSender().getUuid().equals(mConnectedUserUuid))
					.count();
		}
		mSeenMessageCounts.put(targetUuid, count);
		mChannelListView.updateBadges(mAllMessages, mSeenMessageCounts, mConnectedUserUuid);
		mUserListView.updateBadges(mAllMessages, mSeenMessageCounts, mConnectedUserUuid);
	}

	public void updateAll(Set<Message> messages, Set<User> users, Set<Channel> channels) {
		mAllMessages = messages;

		Set<User> otherUsers = new HashSet<>();
		for (User u : users) {
			if (!u.getUuid().equals(mConnectedUserUuid)
					&& !u.getUuid().equals(Constants.UNKNONWN_USER_UUID)) {
				otherUsers.add(u);
			}
		}
		mUserListView.updateUsers(otherUsers);
		mSendMessageView.setAvailableUsers(otherUsers);

		Set<Channel> visibleChannels = new HashSet<>();
		for (Channel channel : channels) {
			List<User> members = channel.getUsers();
			if (members.isEmpty()) {
				visibleChannels.add(channel);
			} else {
				boolean isMember = members.stream().anyMatch(u -> u.getUuid().equals(mConnectedUserUuid));
				if (isMember) visibleChannels.add(channel);
			}
		}
		mChannelListView.updateChannels(visibleChannels);

		if (!mInitialLoadDone) {
			for (User u : otherUsers) {
				int count = (int) messages.stream()
					.filter(m -> m.getSender().getUuid().equals(u.getUuid()) && m.getRecipient().equals(mConnectedUserUuid))
					.count();
				mSeenMessageCounts.put(u.getUuid(), count);
			}
			for (Channel c : visibleChannels) {
				int count = (int) messages.stream()
					.filter(m -> m.getRecipient().equals(c.getUuid()) && !m.getSender().getUuid().equals(mConnectedUserUuid))
					.count();
				mSeenMessageCounts.put(c.getUuid(), count);
			}
			mInitialLoadDone = true;
		}

		mChannelListView.updateBadges(messages, mSeenMessageCounts, mConnectedUserUuid);
		mUserListView.updateBadges(messages, mSeenMessageCounts, mConnectedUserUuid);
		refreshMessages();
	}
}
