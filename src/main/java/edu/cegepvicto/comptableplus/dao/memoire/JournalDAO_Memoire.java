package edu.cegepvicto.comptableplus.dao.memoire;

import edu.cegepvicto.comptableplus.dao.JournalDAO;
import edu.cegepvicto.comptableplus.modele.EntreeJournal;

import java.util.List;

/** Implémentation en mémoire de {@link JournalDAO}. */
public class JournalDAO_Memoire implements JournalDAO {

    private final DepotMemoire depot;

    /** @param depot le dépôt de données partagé. */
    public JournalDAO_Memoire(DepotMemoire depot) {
        this.depot = depot;
    }

    @Override
    public void consigner(EntreeJournal entree) {
        synchronized (depot) {
            depot.journal.add(entree);
        }
    }

    @Override
    public List<EntreeJournal> chargerTout() {
        synchronized (depot) {
            return List.copyOf(depot.journal);
        }
    }
}
