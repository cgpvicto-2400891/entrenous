package edu.cegepvicto.comptableplus.service;

import edu.cegepvicto.comptableplus.dao.DonneesException;
import edu.cegepvicto.comptableplus.dao.JournalDAO;
import edu.cegepvicto.comptableplus.modele.ActionJournal;
import edu.cegepvicto.comptableplus.modele.EntreeJournal;
import edu.cegepvicto.comptableplus.modele.Role;
import edu.cegepvicto.comptableplus.modele.Session;
import edu.cegepvicto.comptableplus.modele.Utilisateur;

import java.time.LocalDateTime;

/**
 * Vérifie les droits d'accès (principe du moindre privilège). L'interface cache les boutons
 * interdits, mais ce service refuse quand même l'opération si elle est appelée directement,
 * et il consigne la tentative au journal.
 */
public class ServiceAutorisation {

    private final JournalDAO journalDAO;

    /** @param journalDAO le journal où les accès refusés sont consignés. */
    public ServiceAutorisation(JournalDAO journalDAO) {
        this.journalDAO = journalDAO;
    }

    /**
     * Indique si la session permet de créer un dossier, sans rien consigner.
     * Sert à activer ou désactiver le bouton de l'interface.
     * @param session la session de la personne connectée.
     * @return {@code true} si la session est valide et que la personne est gestionnaire.
     */
    public boolean peutCreerDossier(Session session) {
        return session != null && !session.estExpiree()
                && session.getUtilisateur().role() == Role.GESTIONNAIRE;
    }

    /**
     * Exige le droit de créer un dossier. Un refus pour cause de rôle est consigné au journal.
     * @param session la session de la personne connectée.
     * @throws SessionExpireeException si la session est absente ou expirée.
     * @throws AccesRefuseException    si la personne n'est pas gestionnaire.
     */
    public void verifierCreationDossier(Session session) throws AccesRefuseException {
        if (session == null || session.estExpiree()) {
            throw new SessionExpireeException("Votre session a expiré. Veuillez vous reconnecter.");
        }
        Utilisateur utilisateur = session.getUtilisateur();
        if (utilisateur.role() != Role.GESTIONNAIRE) {
            consignerRefus(utilisateur);
            throw new AccesRefuseException("Accès refusé.");
        }
    }

    private void consignerRefus(Utilisateur utilisateur) {
        try {
            journalDAO.consigner(new EntreeJournal(LocalDateTime.now(), utilisateur.id(),
                    ActionJournal.ACCES_REFUSE_CREATION_DOSSIER, null));
        } catch (DonneesException exception) {
            // L'accès est refusé même si le journal est indisponible; la cause est déjà consignée dans les logs.
        }
    }
}
