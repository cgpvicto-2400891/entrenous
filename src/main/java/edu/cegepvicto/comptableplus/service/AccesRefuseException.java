package edu.cegepvicto.comptableplus.service;

/** La personne utilisatrice n'a pas le droit d'effectuer l'opération demandée. */
public class AccesRefuseException extends Exception {

    /** @param message message sans détails techniques, affichable à l'écran. */
    public AccesRefuseException(String message) {
        super(message);
    }
}
