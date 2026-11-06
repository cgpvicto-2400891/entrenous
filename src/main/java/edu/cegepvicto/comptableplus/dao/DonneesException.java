package edu.cegepvicto.comptableplus.dao;

/**
 * Erreur d'accès aux données (base inaccessible, requête refusée, etc.). Le message est destiné à la
 * personne utilisatrice : il ne contient jamais de détails techniques. La cause technique est
 * conservée (et consignée) pour l'équipe de développement.
 */
public class DonneesException extends Exception {

    /**
     * @param message message sans détails techniques, affichable à l'écran.
     * @param cause   cause technique d'origine, ou {@code null}.
     */
    public DonneesException(String message, Throwable cause) {
        super(message, cause);
    }
}
