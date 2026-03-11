package main.java.com.ubo.tp.message.ihm.message;

import java.util.UUID;

import main.java.com.ubo.tp.message.common.Constants;
import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.database.DbConnector;
import main.java.com.ubo.tp.message.core.session.ISession;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.ihm.MainContentView;

/**
 * Contrôleur pour l'envoi de messages.
 *
 * Si un canal est sélectionné, le message est envoyé dans ce canal.
 * Sinon, le message est envoyé en "général" (UUID inconnu).
 */
public class SendMessageController {

	private DbConnector mDbConnector;
	private DataManager mDataManager;
	private ISession mSession;
	private SendMessageView mSendMessageView;
	private MainContentView mMainContentView;

	public SendMessageController(DbConnector dbConnector, DataManager dataManager, ISession session,
			SendMessageView sendMessageView, MainContentView mainContentView) {
		this.mDbConnector = dbConnector;
		this.mDataManager = dataManager;
		this.mSession = session;
		this.mSendMessageView = sendMessageView;
		this.mMainContentView = mainContentView;

		this.mSendMessageView.addSendListener(e -> handleSend());
	}

	private void handleSend() {
		String text = mSendMessageView.getText();
		String imageBase64 = mSendMessageView.getSelectedImageBase64();

		if (text.isEmpty() && (imageBase64 == null || imageBase64.isEmpty())) {
			return;
		}

		// Si un canal ou utilisateur est sélectionné, envoyer vers lui. Sinon, UUID inconnu.
		UUID recipient = mMainContentView.getSelectedRecipientUuid();
		if (recipient == null) {
			recipient = Constants.UNKNONWN_USER_UUID;
		}

		Message message = new Message(mSession.getConnectedUser(), recipient, text);
		if (imageBase64 != null && !imageBase64.isEmpty()) {
			message.setImageBase64(imageBase64);
		}

		// 1. Ajouter en BDD (affichage immédiat)
		mDbConnector.addMessage(message);

		// 2. Écrire le fichier .msg dans le répertoire d'échange (persistance)
		mDataManager.sendMessage(message);

		mSendMessageView.clearText();
	}
}
