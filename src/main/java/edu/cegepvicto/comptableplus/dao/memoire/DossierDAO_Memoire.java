package edu.cegepvicto.comptableplus.dao.memoire;

import edu.cegepvicto.comptableplus.dao.DossierDAO;
import edu.cegepvicto.comptableplus.modele.ActionJournal;
import edu.cegepvicto.comptableplus.modele.Client;
import edu.cegepvicto.comptableplus.modele.Dossier;
import edu.cegepvicto.comptableplus.modele.EntreeJournal;
import edu.cegepvicto.comptableplus.modele.StatutDossier;
import edu.cegepvicto.comptableplus.modele.TypeMandat;

import java.time.LocalDateTime;

/**
 * Implémentation en mémoire de {@link DossierDAO}. Les méthodes qui lisent ou modifient les dossiers
 * sont synchronisées sur le dépôt : deux créations simultanées reçoivent des numéros différents.
 */
public class DossierDAO_Memoire implements DossierDAO {

    private static final String FORMAT_NUMERO = "%d-%04d";

    private final DepotMemoire depot;

    /** @param depot le dépôt de données partagé. */
    public DossierDAO_Memoire(DepotMemoire depot) {
        this.depot = depot;
    }

    @Override
    public String prochainNumero(int annee) {
        synchronized (depot) {
            return String.format(FORMAT_NUMERO, annee, derniereSequence(annee) + 1);
        }
    }

    @Override
    public int compterParClient(Client client) {
        synchronized (depot) {
            return (int) depot.dossiers.stream().filter(d -> d.getClient().getId() == client.getId()).count();
        }
    }

    @Override
    public boolean existeDossierActif(Client client, TypeMandat typeMandat, int periodeVisee) {
        synchronized (depot) {
            return depot.dossiers.stream().anyMatch(d -> d.getStatut() == StatutDossier.ACTIF
                    && d.getClient().getId() == client.getId()
                    && d.getTypeMandat().id() == typeMandat.id()
                    && d.getPeriodeVisee() == periodeVisee);
        }
    }

    @Override
    public Dossier creer(Dossier dossier) {
        synchronized (depot) {
            int annee = LocalDateTime.now().getYear();
            String numero = String.format(FORMAT_NUMERO, annee, derniereSequence(annee) + 1);
            Dossier enregistre = dossier.avecIdentification(depot.prochainIdDossier(), numero);
            depot.dossiers.add(enregistre);
            depot.journal.add(new EntreeJournal(LocalDateTime.now(), dossier.getGestionnaireId(),
                    ActionJournal.CREATION_DOSSIER, enregistre.getId()));
            return enregistre;
        }
    }

    /** Plus grande séquence NNNN déjà utilisée pour l'année (0 s'il n'y a aucun dossier). */
    private int derniereSequence(int annee) {
        String prefixe = annee + "-";
        return depot.dossiers.stream()
                .map(Dossier::getNumero)
                .filter(numero -> numero.startsWith(prefixe))
                .mapToInt(numero -> Integer.parseInt(numero.substring(prefixe.length())))
                .max()
                .orElse(0);
    }
}
