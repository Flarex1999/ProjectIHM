package main.java.com.ubo.tp.message.ihm.channel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import main.java.com.ubo.tp.message.datamodel.Channel;

/**
 * Composant graphique représentant UN canal.
 */
public class ChannelPanel extends VBox {

	private static final String DEFAULT_STYLE = "-fx-background-color: #2C2F33; -fx-border-color: #3A3D41; -fx-border-width: 0 0 1 0;";
	private static final String SELECTED_STYLE = "-fx-background-color: #5865F2; -fx-border-color: #3A3D41; -fx-border-width: 0 0 1 0;";

	private Channel mChannel;
	private Circle mUnreadDot;

	public ChannelPanel(Channel channel) {
		this.mChannel = channel;
		this.setSpacing(2);
		this.setPadding(new Insets(8, 12, 8, 12));
		this.setStyle(DEFAULT_STYLE);
		this.setCursor(Cursor.HAND);

		Label nameLabel = new Label("# " + channel.getName());
		nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
		nameLabel.setStyle("-fx-text-fill: white;");

		// Point rouge non-lu (remplace le badge numérique)
		mUnreadDot = new Circle(5, Color.web("#ED4245"));
		mUnreadDot.setVisible(false);

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		HBox nameRow = new HBox(5, nameLabel, spacer, mUnreadDot);
		nameRow.setAlignment(Pos.CENTER_LEFT);

		Label creatorLabel = new Label("@" + channel.getCreator().getUserTag());
		creatorLabel.setFont(Font.font("Arial", 10));
		creatorLabel.setStyle("-fx-text-fill: #96989D;");

		this.getChildren().addAll(nameRow, creatorLabel);
	}

	public void setUnreadCount(int count) {
		mUnreadDot.setVisible(count > 0);
	}

	public Channel getChannel() { return mChannel; }

	public void setSelected(boolean selected) {
		setStyle(selected ? SELECTED_STYLE : DEFAULT_STYLE);
	}

	public void addClickListener(Runnable listener) {
		this.setOnMouseClicked(event -> listener.run());
	}
}
