package edu.cegepvicto.comptableplus.service;

/** La session de la personne utilisatrice n'est plus valide : elle doit se reconnecter. */
public class SessionExpireeException extends AccesRefuseException {

    /** @param message message sans détails techniques, affichable à l'écran. */
    public SessionExpireeException(String message) {
        super(message);
    }
}
