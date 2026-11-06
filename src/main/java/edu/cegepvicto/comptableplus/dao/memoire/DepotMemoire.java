package edu.cegepvicto.comptableplus.dao.memoire;

import edu.cegepvicto.comptableplus.modele.CategorieClient;
import edu.cegepvicto.comptableplus.modele.Client;
import edu.cegepvicto.comptableplus.modele.ClientEntreprise;
import edu.cegepvicto.comptableplus.modele.ClientParticulier;
import edu.cegepvicto.comptableplus.modele.Dossier;
import edu.cegepvicto.comptableplus.modele.EntreeJournal;
import edu.cegepvicto.comptableplus.modele.StatutDossier;
import edu.cegepvicto.comptableplus.modele.TypeMandat;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Données conservées en mémoire pour les DAO « mémoire ». Elles servent au développement et aux tests
 * quand aucune base de données n'est configurée : tout est perdu à la fermeture de l'application.
 * Les DAO d'une même application partagent le même dépôt.
 */
public class DepotMemoire {

    final List<Client> clients = new ArrayList<>();
    final List<Dossier> dossiers = new ArrayList<>();
    final List<EntreeJournal> journal = new ArrayList<>();
    /** Types de mandat offerts, par identifiant de catégorie de client. */
    final Map<Integer, List<TypeMandat>> typesParCategorie = new LinkedHashMap<>();
    private int dernierIdDossier = 0;

    /** Crée un dépôt vide. */
    public DepotMemoire() {
    }

    /**
     * Crée un dépôt rempli de données de démonstration (clients, types de mandat et un dossier existant).
     * Le prochain dossier créé reçoit le numéro {@code AAAA-0053}.
     * @return le dépôt de démonstration.
     */
    public static DepotMemoire avecDonneesDemo() {
        DepotMemoire depot = new DepotMemoire();
        CategorieClient particulier = new CategorieClient(1, "PARTICULIER", "Particulier");
        CategorieClient entreprise = new CategorieClient(2, "ENTREPRISE", "Entreprise");

        ClientParticulier marie = new ClientParticulier(1, particulier, "marie.tremblay@exemple.ca",
                "819 555-0101", "12, rue des Érables, Victoriaville", "Marie", "Tremblay");
        depot.clients.add(marie);
        depot.clients.add(new ClientParticulier(2, particulier, "jean.gagnon@exemple.ca",
                "819 555-0102", "45, boulevard Bois-Francs, Victoriaville", "Jean", "Gagnon"));
        depot.clients.add(new ClientParticulier(3, particulier, "sophie.lavoie@exemple.ca",
                "819 555-0103", "8, rue Notre-Dame, Warwick", "Sophie", "Lavoie"));
        depot.clients.add(new ClientEntreprise(4, entreprise, "info@boulangeriedupuis.ca",
                "819 555-0201", "100, rue Principale, Victoriaville", "Boulangerie Dupuis inc.",
                "1234567890", "Luc Dupuis"));
        depot.clients.add(new ClientEntreprise(5, entreprise, "admin@constructionbeauce.ca",
                "819 555-0202", "250, route 116, Princeville", "Construction Beauce ltée",
                "9876543210", "Isabelle Roy"));
        depot.clients.add(new ClientEntreprise(6, entreprise, "garage.belanger@exemple.ca",
                "819 555-0203", "3, rue du Garage, Kingsey Falls", "Garage Bélanger",
                null, "Paul Bélanger"));

        TypeMandat declarationRevenus = new TypeMandat(1, "Déclaration de revenus", false);
        TypeMandat tenueLivres = new TypeMandat(2, "Tenue de livres", false);
        TypeMandat declarationSociete = new TypeMandat(3, "Déclaration de société", false);
        TypeMandat demandePermis = new TypeMandat(4, "Demande de permis", false);
        TypeMandat autre = new TypeMandat(5, "Autre (sur mesure)", true);
        depot.typesParCategorie.put(particulier.id(), List.of(declarationRevenus, demandePermis, tenueLivres, autre));
        depot.typesParCategorie.put(entreprise.id(), List.of(declarationSociete, demandePermis, tenueLivres, autre));

        // Un dossier actif existe déjà : il permet de voir l'avertissement de doublon (scénario 5a).
        int annee = Year.now().getValue();
        depot.dernierIdDossier = 52;
        depot.dossiers.add(new Dossier(52, annee + "-0052", LocalDate.now().minusDays(30), StatutDossier.ACTIF,
                annee - 1, "Déclaration de l'année précédente", marie, declarationRevenus, 1));
        return depot;
    }

    /** @return le prochain identifiant de dossier (à appeler dans un bloc synchronisé sur le dépôt). */
    int prochainIdDossier() {
        dernierIdDossier++;
        return dernierIdDossier;
    }
}
