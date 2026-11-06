package edu.cegepvicto.comptableplus.dao.memoire;

import edu.cegepvicto.comptableplus.dao.TypeMandatDAO;
import edu.cegepvicto.comptableplus.modele.CategorieClient;
import edu.cegepvicto.comptableplus.modele.TypeMandat;

import java.util.Comparator;
import java.util.List;

/** Implémentation en mémoire de {@link TypeMandatDAO}. */
public class TypeMandatDAO_Memoire implements TypeMandatDAO {

    private final DepotMemoire depot;

    /** @param depot le dépôt de données partagé. */
    public TypeMandatDAO_Memoire(DepotMemoire depot) {
        this.depot = depot;
    }

    @Override
    public List<TypeMandat> chargerParCategorie(CategorieClient categorie) {
        synchronized (depot) {
            return depot.typesParCategorie.getOrDefault(categorie.id(), List.of()).stream()
                    .sorted(Comparator.comparing(TypeMandat::nom, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        }
    }
}
