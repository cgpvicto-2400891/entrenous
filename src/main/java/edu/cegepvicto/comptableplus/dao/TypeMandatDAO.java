package edu.cegepvicto.comptableplus.dao;

import edu.cegepvicto.comptableplus.modele.CategorieClient;
import edu.cegepvicto.comptableplus.modele.TypeMandat;

import java.util.List;

/** Accès aux types de mandat. */
public interface TypeMandatDAO {

    /**
     * Charge les types de mandat offerts à une catégorie de clients.
     * @param categorie la catégorie du client.
     * @return les types offerts, triés par nom.
     * @throws DonneesException si les données ne sont pas accessibles.
     */
    List<TypeMandat> chargerParCategorie(CategorieClient categorie) throws DonneesException;
}
