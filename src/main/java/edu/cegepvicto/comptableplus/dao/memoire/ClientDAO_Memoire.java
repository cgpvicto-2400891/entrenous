package edu.cegepvicto.comptableplus.dao.memoire;

import edu.cegepvicto.comptableplus.dao.ClientDAO;
import edu.cegepvicto.comptableplus.modele.Client;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Implémentation en mémoire de {@link ClientDAO}. */
public class ClientDAO_Memoire implements ClientDAO {

    private final DepotMemoire depot;

    /** @param depot le dépôt de données partagé. */
    public ClientDAO_Memoire(DepotMemoire depot) {
        this.depot = depot;
    }

    @Override
    public List<Client> rechercherParNom(String texte) {
        String recherche = texte == null ? "" : texte.strip().toLowerCase(Locale.ROOT);
        if (recherche.isEmpty()) {
            return List.of();
        }
        synchronized (depot) {
            return depot.clients.stream()
                    .filter(client -> client.getNomAffichage().toLowerCase(Locale.ROOT).contains(recherche))
                    .sorted(Comparator.comparing(Client::getNomAffichage, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        }
    }
}
