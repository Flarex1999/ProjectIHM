package main.java.com.ubo.tp.message;

/**
 * Point d'entrée pour le JAR exécutable.
 *
 * Cette classe ne doit PAS étendre javafx.application.Application.
 * Cela permet au fat JAR (maven-shade-plugin) de fonctionner correctement :
 * le module JavaFX est chargé par MessageAppLauncher, pas par le manifest.
 */
public class Launcher {

    public static void main(String[] args) {
        MessageAppLauncher.main(args);
    }
}
