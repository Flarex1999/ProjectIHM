package main.java.com.ubo.tp.message.ihm.login;

import java.util.Set;

import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.session.ISession;
import main.java.com.ubo.tp.message.datamodel.User;

/**
 * Contrôleur du composant de connexion (SRS-MAP-USR-004).
 *
 * Gère la logique métier de connexion à un compte existant :
 * - Validation des champs
 * - Recherche de l'utilisateur par tag
 * - Vérification du mot de passe
 * - Connexion via la Session
 *
 * Fait le lien entre :
 * - La vue (LoginView) : lecture des champs, affichage des erreurs
 * - Le DataManager     : accès à la base de données
 * - La Session         : connexion de l'utilisateur
 */
public class LoginController {

	/** Référence vers le DataManager pour accéder aux utilisateurs en base */
	private DataManager mDataManager;

	/** Référence vers la session pour connecter l'utilisateur */
	private ISession mSession;

	/** Référence vers la vue de connexion */
	private LoginView mLoginView;

	/**
	 * Constructeur.
	 * Branche l'écouteur sur le bouton "Se connecter" de la vue.
	 *
	 * @param dataManager le gestionnaire de données
	 * @param session     la session de l'application
	 * @param loginView   la vue de connexion
	 */
	public LoginController(DataManager dataManager, ISession session, LoginView loginView) {
		this.mDataManager = dataManager;
		this.mSession = session;
		this.mLoginView = loginView;

		// Quand le bouton "Se connecter" est cliqué, handleLogin() est appelée
		this.mLoginView.addLoginListener(e -> handleLogin());
	}

	/**
	 * Gère la connexion à un compte existant.
	 *
	 * Étapes :
	 * 1. Récupérer les valeurs saisies (tag, mot de passe)
	 * 2. Vérifier que le tag n'est pas vide
	 * 3. Vérifier que le mot de passe n'est pas vide
	 * 4. Chercher l'utilisateur dans la base par son tag
	 * 5. Vérifier que le mot de passe correspond
	 * 6. Connecter l'utilisateur via la Session
	 */
	private void handleLogin() {
		String tag = mLoginView.getTag();
		String password = mLoginView.getPassword();

		if (tag.isEmpty()) {
			mLoginView.showError("Le tag est obligatoire !");
			return;
		}

		if (password.isEmpty()) {
			mLoginView.showError("Le mot de passe est obligatoire !");
			return;
		}

		User foundUser = findUserByTag(tag);

		if (foundUser == null) {
			mLoginView.showError("Aucun compte trouvé avec ce tag !");
			return;
		}

		if (!foundUser.getUserPassword().equals(password)) {
			mLoginView.showError("Mot de passe incorrect !");
			return;
		}

		if (foundUser.isDeleted()) {
			mLoginView.showError("Ce compte a été supprimé !");
			return;
		}

		if (foundUser.isOnline()) {
			mLoginView.showError("Ce compte est déjà connecté sur une autre instance !");
			return;
		}

		mSession.connect(foundUser);
	}

	/**
	 * Recherche un utilisateur dans la base de données par son tag.
	 *
	 * @param tag le tag de l'utilisateur recherché
	 * @return l'objet User correspondant, ou null si aucun utilisateur trouvé
	 */
	private User findUserByTag(String tag) {
		Set<User> users = mDataManager.getUsers();
		for (User user : users) {
			if (user.getUserTag().equals(tag)) {
				return user;
			}
		}
		return null;
	}
}
