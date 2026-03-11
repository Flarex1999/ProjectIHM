package main.java.com.ubo.tp.message.datamodel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Classe du modèle représentant un message.
 *
 * @author S.Lucas
 */
public class Message extends AbstractMessageAppObject {

	/**
	 * Utilisateur source du message.
	 */
	protected final User mSender;

	/**
	 * Destinataire du message.
	 */
	protected final UUID mRecipient;

	/**
	 * Date d'émission du message.
	 */
	protected final long mEmissionDate;

	/**
	 * Corps du message.
	 */
	protected final String mText;

	/**
	 * Image jointe au message (encodée en base64, peut être null).
	 */
	protected String mImageBase64 = null;

	/**
	 * Réactions au message : code emoji → liste des UUID des réacteurs.
	 */
	protected Map<String, List<UUID>> mReactions = new LinkedHashMap<>();

	/**
	 * Constructeur.
	 *
	 * @param sender    utilisateur à l'origine du message.
	 * @param recipient destinataire du message.
	 * @param text      corps du message.
	 */
	public Message(User sender, UUID recipient, String text) {
		this(UUID.randomUUID(), sender, recipient, System.currentTimeMillis(), text);
	}

	/**
	 * Constructeur.
	 *
	 * @param messageUuid  identifiant du message.
	 * @param sender       utilisateur à l'origine du message.
	 * @param recipient    destinataire du message.
	 * @param emissionDate date d'émission du message.
	 * @param text         corps du message.
	 */
	public Message(UUID messageUuid, User sender, UUID recipient, long emissionDate, String text) {
		super(messageUuid);
		mSender = sender;
		mRecipient = recipient;
		mEmissionDate = emissionDate;
		mText = text;
	}

	/**
	 * @return l'utilisateur source du message.
	 */
	public User getSender() {
		return mSender;
	}

	/**
	 * @return le destinataire du message.
	 */
	public UUID getRecipient() {
		return mRecipient;
	}

	/**
	 * @return le corps du message.
	 */
	public String getText() {
		return mText;
	}

	/**
	 * Retourne la date d'émission.
	 */
	public long getEmissionDate() {
		return this.mEmissionDate;
	}

	public String getImageBase64() {
		return mImageBase64;
	}

	public void setImageBase64(String imageBase64) {
		this.mImageBase64 = imageBase64;
	}

	public boolean hasImage() {
		return mImageBase64 != null && !mImageBase64.isEmpty();
	}

	public Map<String, List<UUID>> getReactions() { return mReactions; }

	public void setReactions(Map<String, List<UUID>> reactions) {
		this.mReactions = reactions != null ? reactions : new LinkedHashMap<>();
	}

	public void toggleReaction(String emojiCode, UUID userUuid) {
		mReactions.computeIfAbsent(emojiCode, k -> new ArrayList<>());
		List<UUID> reactors = mReactions.get(emojiCode);
		if (reactors.contains(userUuid)) {
			reactors.remove(userUuid);
			if (reactors.isEmpty()) mReactions.remove(emojiCode);
		} else {
			reactors.add(userUuid);
		}
	}

	public boolean hasReacted(String emojiCode, UUID userUuid) {
		return mReactions.containsKey(emojiCode) && mReactions.get(emojiCode).contains(userUuid);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("[");
		sb.append(this.getClass().getName());
		sb.append("] : ");
		sb.append(this.getUuid());
		sb.append(" {");
		sb.append(this.getText());
		sb.append("}");

		return sb.toString();
	}
}
