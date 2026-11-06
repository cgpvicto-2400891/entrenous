package edu.cegepvicto.comptableplus.dao;

import edu.cegepvicto.comptableplus.modele.EntreeJournal;

import java.util.List;

/** Accès au journal des actions. */
public interface JournalDAO {

    /**
     * Ajoute une entrée au journal.
     * @param entree l'action à consigner.
     * @throws DonneesException si l'entrée ne peut pas être enregistrée.
     */
    void consigner(EntreeJournal entree) throws DonneesException;

    /**
     * Charge toutes les entrées, de la plus ancienne à la plus récente.
     * @return les entrées du journal.
     * @throws DonneesException si les données ne sont pas accessibles.
     */
    List<EntreeJournal> chargerTout() throws DonneesException;
}
