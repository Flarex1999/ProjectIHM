package main.java.com.ubo.tp.message.ihm.signup;

import java.util.Set;

import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.session.ISession;
import main.java.com.ubo.tp.message.datamodel.User;

/**
 * Contrôleur du composant d'inscription (SRS-MAP-USR-001).
 *
 * Gère la logique métier de création de compte :
 * - Validation des champs (SRS-MAP-USR-002)
 * - Vérification de l'unicité du tag (SRS-MAP-USR-003)
 * - Création de l'utilisateur et connexion
 *
 * Fait le lien entre :
 * - La vue (SignupView) : lecture des champs, affichage des erreurs
 * - Le DataManager      : accès à la base de données
 * - La Session          : connexion de l'utilisateur après inscription
 */
public class SignupController {

	/** Référence vers le DataManager pour accéder aux utilisateurs en base */
	private DataManager mDataManager;

	/** Référence vers la session pour connecter l'utilisateur */
	private ISession mSession;

	/** Référence vers la vue d'inscription */
	private SignupView mSignupView;

	/**
	 * Constructeur.
	 * Branche l'écouteur sur le bouton "S'inscrire" de la vue.
	 *
	 * @param dataManager le gestionnaire de données
	 * @param session     la session de l'application
	 * @param signupView  la vue d'inscription
	 */
	public SignupController(DataManager dataManager, ISession session, SignupView signupView) {
		this.mDataManager = dataManager;
		this.mSession = session;
		this.mSignupView = signupView;

		// Quand le bouton "S'inscrire" est cliqué, handleSignup() est appelée
		this.mSignupView.addSignupListener(e -> handleSignup());
	}

	/**
	 * Gère l'inscription (création d'un nouveau compte).
	 *
	 * Étapes :
	 * 1. Récupérer les valeurs saisies (tag, nom, mot de passe)
	 * 2. Vérifier que le tag n'est pas vide     (SRS-MAP-USR-002)
	 * 3. Vérifier que le nom n'est pas vide     (SRS-MAP-USR-002)
	 * 4. Vérifier que le mot de passe n'est pas vide
	 * 5. Vérifier que le tag est unique          (SRS-MAP-USR-003)
	 * 6. Créer l'objet User
	 * 7. Écrire le fichier utilisateur via DataManager
	 * 8. Connecter l'utilisateur via la Session
	 */
	private void handleSignup() {
		String tag = mSignupView.getTag();
		String name = mSignupView.getName();
		String password = mSignupView.getPassword();

		if (tag.isEmpty()) {
			mSignupView.showError("Le tag est obligatoire !");
			return;
		}

		if (name.isEmpty()) {
			mSignupView.showError("Le nom est obligatoire !");
			return;
		}

		if (password.isEmpty()) {
			mSignupView.showError("Le mot de passe est obligatoire !");
			return;
		}

		if (isTagAlreadyUsed(tag)) {
			mSignupView.showError("Ce tag est déjà utilisé !");
			return;
		}

		User newUser = new User(tag, password, name);
		newUser.setOnline(true); // Déjà en ligne dès la création
		mDataManager.sendUser(newUser);
		mSession.connect(newUser);
	}

	/**
	 * Vérifie si un tag est déjà utilisé par un utilisateur existant.
	 *
	 * @param tag le tag à vérifier
	 * @return true si le tag est déjà pris, false sinon
	 */
	private boolean isTagAlreadyUsed(String tag) {
		Set<User> users = mDataManager.getUsers();
		for (User user : users) {
			if (user.getUserTag().equals(tag)) {
				return true;
			}
		}
		return false;
	}
}
