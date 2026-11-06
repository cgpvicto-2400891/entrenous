package edu.cegepvicto.comptableplus.controleur;

import edu.cegepvicto.comptableplus.modele.Session;
import edu.cegepvicto.comptableplus.service.LocalisateurServices;

/**
 * Contexte de données du système. Cet objet circule entre les fenêtres (paramètre de tous leurs
 * constructeurs) : il donne accès aux services et à la session de la personne connectée.
 * Ce n'est volontairement pas un singleton, pour ne pas contourner l'encapsulation.
 */
public class DonneesSysteme {

    private final LocalisateurServices services;
    private final Session session;

    /**
     * @param services le localisateur de services de l'application.
     * @param session  la session de la personne connectée.
     */
    public DonneesSysteme(LocalisateurServices services, Session session) {
        this.services = services;
        this.session = session;
    }

    public LocalisateurServices getServices() {
        return services;
    }

    public Session getSession() {
        return session;
    }
}
