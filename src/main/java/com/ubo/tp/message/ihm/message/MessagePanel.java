package main.java.com.ubo.tp.message.ihm.message;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import main.java.com.ubo.tp.message.common.EmojiImageUtil;
import main.java.com.ubo.tp.message.datamodel.Message;

/**
 * Composant graphique représentant UN message — design bulles de chat.
 */
public class MessagePanel extends VBox {

	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

	private static final String[] AVATAR_COLORS = {
		"#1DA1F2", "#E74C3C", "#2ECC71", "#F39C12", "#9B59B6", "#E91E63"
	};

	private static final String[][] REACTIONS = {
		{"heart","❤️"}, {"thumbsup","👍"}, {"laugh","😂"}, {"wow","😮"}, {"sad","😢"}
	};

	public MessagePanel(Message message) {
		this(message, null, null, null);
	}

	public MessagePanel(Message message, UUID connectedUserUuid, Runnable onDelete) {
		this(message, connectedUserUuid, onDelete, null);
	}

	public MessagePanel(Message message, UUID connectedUserUuid, Runnable onDelete, BiConsumer<Message, String> onReact) {
		this.setSpacing(0);
		this.setPadding(new Insets(3, 12, 3, 12));

		boolean isMyMessage = connectedUserUuid != null
				&& message.getSender().getUuid().equals(connectedUserUuid);

		// --- Avatar ---
		String userTag = message.getSender().getUserTag();
		int colorIdx = Math.abs(userTag.hashCode()) % AVATAR_COLORS.length;
		String avatarColor = AVATAR_COLORS[colorIdx];
		String initial = message.getSender().getName().substring(0, 1).toUpperCase();
		Label avatarLabel = new Label(initial);
		avatarLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
		avatarLabel.setStyle("-fx-text-fill: white;");
		Circle avatarBg = new Circle(14, Color.web(avatarColor));
		StackPane avatar = new StackPane(avatarBg, avatarLabel);
		avatar.setMinSize(28, 28);
		avatar.setMaxSize(28, 28);

		// --- Contenu de la bulle ---
		VBox bubbleContent = new VBox(4);

		// Nom de l'expéditeur (seulement pour les autres)
		if (!isMyMessage) {
			boolean senderDeleted = message.getSender().isDeleted();
			String senderDisplayName = senderDeleted
				? "(supprimé) @" + message.getSender().getUserTag()
				: message.getSender().getName();
			Label senderLabel = new Label(senderDisplayName);
			senderLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
			senderLabel.setStyle("-fx-text-fill: #5865F2;");
			bubbleContent.getChildren().add(senderLabel);
		}

		// Texte du message
		if (!message.getText().isEmpty()) {
			Label textLabel = new Label(message.getText());
			textLabel.setFont(Font.font("Arial", 13));
			textLabel.setStyle(isMyMessage ? "-fx-text-fill: white;" : "-fx-text-fill: #2C3E50;");
			textLabel.setWrapText(true);
			textLabel.setMaxWidth(380);
			bubbleContent.getChildren().add(textLabel);
		}

		// Image dans la bulle
		if (message.hasImage()) {
			try {
				byte[] imageBytes = Base64.getDecoder().decode(message.getImageBase64());
				Image image = new Image(new ByteArrayInputStream(imageBytes));
				ImageView imageView = new ImageView(image);
				imageView.setPreserveRatio(true);
				imageView.setFitWidth(320);
				imageView.setSmooth(true);
				bubbleContent.getChildren().add(imageView);
			} catch (Exception e) {
				Label errorLabel = new Label("[Image non affichable]");
				errorLabel.setStyle("-fx-text-fill: #BDC3C7; -fx-font-style: italic;");
				bubbleContent.getChildren().add(errorLabel);
			}
		}

		// Footer : timestamp + bouton supprimer
		String timeStr = TIME_FORMAT.format(new Date(message.getEmissionDate()));
		Label timeLabel = new Label(timeStr);
		timeLabel.setFont(Font.font("Arial", 9));
		timeLabel.setStyle(isMyMessage ? "-fx-text-fill: rgba(255,255,255,0.7);" : "-fx-text-fill: #95A5A6;");

		HBox footer = new HBox(6, timeLabel);
		footer.setAlignment(isMyMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

		if (isMyMessage && onDelete != null) {
			Button deleteBtn = new Button("🗑");
			deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.7); -fx-cursor: hand; -fx-font-size: 10; -fx-padding: 0;");
			deleteBtn.setOnAction(e -> onDelete.run());
			footer.getChildren().add(deleteBtn);
		}

		bubbleContent.getChildren().add(footer);

		// --- Bulle (StackPane avec style arrondi) ---
		StackPane bubble = new StackPane(bubbleContent);
		bubble.setPadding(new Insets(8, 12, 8, 12));
		if (isMyMessage) {
			bubble.setStyle(
				"-fx-background-color: #5865F2;" +
				"-fx-background-radius: 16 16 4 16;"
			);
		} else {
			bubble.setStyle(
				"-fx-background-color: #F2F3F5;" +
				"-fx-background-radius: 16 16 16 4;"
			);
		}

		// --- Ligne principale : [spacer] [bulle] [avatar] ou [avatar] [bulle] ---
		HBox messageRow = new HBox(8);
		messageRow.setAlignment(Pos.TOP_CENTER);
		messageRow.setPadding(new Insets(3, 0, 3, 0));

		if (isMyMessage) {
			Region spacer = new Region();
			HBox.setHgrow(spacer, Priority.ALWAYS);
			messageRow.getChildren().addAll(spacer, bubble, avatar);
		} else {
			Region spacer = new Region();
			HBox.setHgrow(spacer, Priority.ALWAYS);
			messageRow.getChildren().addAll(avatar, bubble, spacer);
		}

		this.getChildren().add(messageRow);

		// Barre de réactions
		HBox reactionBar = buildReactionBar(message, connectedUserUuid, onReact, isMyMessage);
		this.getChildren().add(reactionBar);
	}

	private HBox buildReactionBar(Message message, UUID connectedUserUuid, BiConsumer<Message, String> onReact, boolean isMyMessage) {
		HBox bar = new HBox(4);
		bar.setPadding(new Insets(2, isMyMessage ? 44 : 0, 2, isMyMessage ? 0 : 44));
		bar.setAlignment(isMyMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
		for (String[] r : REACTIONS) {
			String code = r[0], symbol = r[1];
			List<UUID> reactors = message.getReactions().getOrDefault(code, new ArrayList<>());
			boolean reacted = connectedUserUuid != null && reactors.contains(connectedUserUuid);
			String label = reactors.isEmpty() ? symbol : symbol + " " + reactors.size();
			Button btn = new Button(label);
			btn.setStyle(reacted
				? "-fx-background-color:#EBF5FF;-fx-border-color:#5865F2;-fx-border-radius:12;-fx-background-radius:12;-fx-font-size:14;-fx-cursor:hand;-fx-padding:2 8;"
				: "-fx-background-color:#F8F9FA;-fx-border-color:#E0E0E0;-fx-border-radius:12;-fx-background-radius:12;-fx-font-size:14;-fx-cursor:hand;-fx-padding:2 8;");
			if (onReact != null) btn.setOnAction(e -> onReact.accept(message, code));
			bar.getChildren().add(btn);
		}
		return bar;
	}
}
