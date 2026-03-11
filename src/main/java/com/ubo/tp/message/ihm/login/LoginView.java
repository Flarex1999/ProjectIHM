package main.java.com.ubo.tp.message.ihm.login;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Vue du formulaire de connexion.
 */
public class LoginView extends StackPane {

	private TextField mTagField;
	private PasswordField mPasswordField;
	private Label mErrorLabel;
	private Button mLoginButton;
	private Button mSwitchToSignupButton;

	public LoginView() {
		GridPane formPane = this.createFormPanel();
		this.getChildren().add(formPane);
		StackPane.setAlignment(formPane, Pos.CENTER);
	}

	private GridPane createFormPanel() {
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(5);
		grid.setVgap(5);
		grid.setMaxWidth(350);

		ColumnConstraints col0 = new ColumnConstraints();
		col0.setHalignment(HPos.RIGHT);
		ColumnConstraints col1 = new ColumnConstraints();
		col1.setPrefWidth(200);
		grid.getColumnConstraints().addAll(col0, col1);

		int row = 0;

		// Logo
		try {
			Image logoImage = new Image("file:src/main/resources/images/logo_50.png");
			ImageView logoView = new ImageView(logoImage);
			grid.add(logoView, 0, row, 2, 1);
			GridPane.setHalignment(logoView, HPos.CENTER);
			GridPane.setMargin(logoView, new Insets(10, 0, 5, 0));
		} catch (Exception e) {
			// Logo non trouvé
		}
		row++;

		// Titre
		Label titleLabel = new Label("MessageApp");
		titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
		grid.add(titleLabel, 0, row, 2, 1);
		GridPane.setHalignment(titleLabel, HPos.CENTER);
		GridPane.setMargin(titleLabel, new Insets(0, 0, 20, 0));
		row++;

		// Tag
		grid.add(new Label("Tag :"), 0, row);
		mTagField = new TextField();
		mTagField.setPrefHeight(28);
		grid.add(mTagField, 1, row);
		row++;

		// Mot de passe
		grid.add(new Label("Mot de passe :"), 0, row);
		mPasswordField = new PasswordField();
		mPasswordField.setPrefHeight(28);
		grid.add(mPasswordField, 1, row);
		row++;

		// Label d'erreur
		mErrorLabel = new Label(" ");
		mErrorLabel.setTextFill(Color.RED);
		grid.add(mErrorLabel, 0, row, 2, 1);
		GridPane.setHalignment(mErrorLabel, HPos.CENTER);
		GridPane.setMargin(mErrorLabel, new Insets(10, 0, 5, 0));
		row++;

		// Bouton "Se connecter"
		mLoginButton = new Button("Se connecter");
		grid.add(mLoginButton, 0, row, 2, 1);
		GridPane.setHalignment(mLoginButton, HPos.CENTER);
		GridPane.setMargin(mLoginButton, new Insets(10, 0, 10, 0));
		row++;

		// Lien vers inscription
		mSwitchToSignupButton = new Button("Pas de compte ? S'inscrire");
		mSwitchToSignupButton.setStyle("-fx-background-color: transparent; -fx-text-fill: blue; -fx-cursor: hand;");
		mSwitchToSignupButton.setCursor(Cursor.HAND);
		grid.add(mSwitchToSignupButton, 0, row, 2, 1);
		GridPane.setHalignment(mSwitchToSignupButton, HPos.CENTER);

		return grid;
	}

	public void addLoginListener(EventHandler<ActionEvent> listener) {
		mLoginButton.setOnAction(listener);
	}

	public void addSwitchToSignupListener(EventHandler<ActionEvent> listener) {
		mSwitchToSignupButton.setOnAction(listener);
	}

	public void showError(String message) {
		mErrorLabel.setText(message);
	}

	public void resetFields() {
		mTagField.setText("");
		mPasswordField.setText("");
		mErrorLabel.setText(" ");
	}

	public String getTag() {
		return mTagField.getText().trim();
	}

	public String getPassword() {
		return mPasswordField.getText();
	}
}
