package main.java.com.ubo.tp.message.ihm.user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import main.java.com.ubo.tp.message.datamodel.User;

/**
 * Composant graphique représentant UN utilisateur.
 */
public class UserPanel extends VBox {

	private static final String DEFAULT_STYLE = "-fx-background-color: #2C2F33; -fx-border-color: #3A3D41; -fx-border-width: 0 0 1 0;";
	private static final String SELECTED_STYLE = "-fx-background-color: #5865F2; -fx-border-color: #3A3D41; -fx-border-width: 0 0 1 0;";

	private User mUser;
	private Circle mUnreadDot;

	public UserPanel(User user) {
		this.mUser = user;
		this.setSpacing(2);
		this.setPadding(new Insets(8, 12, 8, 12));
		this.setStyle(DEFAULT_STYLE);
		this.setCursor(Cursor.HAND);

		Circle statusDot = new Circle(4, user.isOnline() ? Color.web("#3BA55D") : Color.web("#747F8D"));

		String displayName = user.isDeleted()
			? "(utilisateur supprimé) @" + user.getUserTag()
			: user.getName();
		Label nameLabel = new Label(displayName);
		nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
		nameLabel.setStyle(user.isDeleted()
			? "-fx-text-fill: #96989D; -fx-font-style: italic;"
			: "-fx-text-fill: white;");

		mUnreadDot = new Circle(5, Color.web("#ED4245"));
		mUnreadDot.setVisible(false);

		HBox nameRow = new HBox(6, statusDot, nameLabel);
		nameRow.setAlignment(Pos.CENTER_LEFT);

		Label tagLabel = new Label("@" + user.getUserTag());
		tagLabel.setFont(Font.font("Arial", 10));
		tagLabel.setStyle("-fx-text-fill: #96989D;");

		HBox tagRow = new HBox(6, tagLabel, mUnreadDot);
		tagRow.setAlignment(Pos.CENTER_LEFT);

		this.getChildren().addAll(nameRow, tagRow);
	}

	public void setUnreadCount(int count) {
		mUnreadDot.setVisible(count > 0);
	}

	public User getUser() { return mUser; }

	public void setSelected(boolean selected) {
		setStyle(selected ? SELECTED_STYLE : DEFAULT_STYLE);
	}

	public void addClickListener(Runnable listener) {
		this.setOnMouseClicked(event -> listener.run());
	}
}
