package edu.cegepvicto.comptableplus.dao;

import edu.cegepvicto.comptableplus.dao.sql.ClientDAO_SQL;
import edu.cegepvicto.comptableplus.dao.sql.ConnexionBD;
import edu.cegepvicto.comptableplus.dao.sql.DossierDAO_SQL;
import edu.cegepvicto.comptableplus.dao.sql.JournalDAO_SQL;
import edu.cegepvicto.comptableplus.dao.sql.TypeMandatDAO_SQL;
import edu.cegepvicto.comptableplus.modele.ActionJournal;
import edu.cegepvicto.comptableplus.modele.Client;
import edu.cegepvicto.comptableplus.modele.ClientEntreprise;
import edu.cegepvicto.comptableplus.modele.ClientParticulier;
import edu.cegepvicto.comptableplus.modele.Dossier;
import edu.cegepvicto.comptableplus.modele.EntreeJournal;
import edu.cegepvicto.comptableplus.modele.TypeMandat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste les DAO JDBC avec le vrai schéma (schema.sql) sur une base H2 en mémoire, mode MySQL.
 * Couvre T-001-001-13 (échec sans dossier incomplet) et T-001-001-14 (créations simultanées).
 */
class DAOSqlTest {

    private static final int PERIODE = LocalDate.now().getYear() - 1;

    private ConnexionBD connexionBD;
    private ClientDAO_SQL clientDAO;
    private TypeMandatDAO_SQL typeMandatDAO;
    private DossierDAO_SQL dossierDAO;
    private JournalDAO_SQL journalDAO;

    @BeforeEach
    void creerBaseDeDonnees() throws Exception {
        connexionBD = new ConnexionBD("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        executerScript("schema.sql");
        executerScript("donnees_demo.sql");
        clientDAO = new ClientDAO_SQL(connexionBD);
        typeMandatDAO = new TypeMandatDAO_SQL(connexionBD);
        dossierDAO = new DossierDAO_SQL(connexionBD);
        journalDAO = new JournalDAO_SQL(connexionBD);
    }

    private void executerScript(String nom) throws Exception {
        String contenu;
        try (InputStream flux = DAOSqlTest.class.getResourceAsStream("/edu/cegepvicto/comptableplus/bd/" + nom)) {
            contenu = new String(flux.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
        String sansCommentaires = String.join("\n",
                contenu.lines().filter(ligne -> !ligne.strip().startsWith("--")).toList());
        try (Connection connexion = connexionBD.ouvrir(); Statement requete = connexion.createStatement()) {
            for (String instruction : sansCommentaires.split(";")) {
                if (!instruction.isBlank()) {
                    requete.execute(instruction);
                }
            }
        }
    }

    private Dossier nouveauDossier(Client client, TypeMandat type, String description, int periode) {
        return new Dossier(client, type, LocalDate.now(), periode, description, 1);
    }

    @Test
    void rechercheTrouveParticuliersEtEntreprisesSansTenirCompteDeLaCasse() throws Exception {
        List<Client> marie = clientDAO.rechercherParNom("tremblay");
        List<Client> boulangerie = clientDAO.rechercherParNom("DUPUIS");

        assertEquals(1, marie.size());
        assertInstanceOf(ClientParticulier.class, marie.get(0));
        assertEquals("Marie Tremblay", marie.get(0).getNomAffichage());
        assertEquals(1, boulangerie.size());
        assertInstanceOf(ClientEntreprise.class, boulangerie.get(0));
        assertEquals("Entreprise", boulangerie.get(0).getCategorie().libelle());
    }

    @Test
    void rechercheNeTraitePasLesJokersEtLesInjectionsCommeDuSql() throws Exception {
        assertTrue(clientDAO.rechercherParNom("%").isEmpty());
        assertTrue(clientDAO.rechercherParNom("_").isEmpty());
        assertTrue(clientDAO.rechercherParNom("x' OR '1'='1").isEmpty());
        assertTrue(clientDAO.rechercherParNom("   ").isEmpty());
    }

    @Test
    void clientEntrepriseSansNumeroEstCharge() throws Exception {
        ClientEntreprise garage = (ClientEntreprise) clientDAO.rechercherParNom("Garage").get(0);

        assertNull(garage.getNumeroEntreprise());
    }

    @Test
    void typesDeMandatSontFiltresParCategorie() throws Exception {
        Client marie = clientDAO.rechercherParNom("Marie").get(0);
        Client boulangerie = clientDAO.rechercherParNom("Boulangerie").get(0);

        List<String> particulier = typeMandatDAO.chargerParCategorie(marie.getCategorie()).stream().map(TypeMandat::nom).toList();
        List<String> entreprise = typeMandatDAO.chargerParCategorie(boulangerie.getCategorie()).stream().map(TypeMandat::nom).toList();

        assertTrue(particulier.contains("Déclaration de revenus") && !particulier.contains("Déclaration de société"));
        assertTrue(entreprise.contains("Déclaration de société") && !entreprise.contains("Déclaration de revenus"));
        TypeMandat autre = typeMandatDAO.chargerParCategorie(marie.getCategorie()).stream()
                .filter(t -> t.nom().startsWith("Autre")).findFirst().orElseThrow();
        assertTrue(autre.descriptionObligatoire());
    }

    @Test
    void creationAttribueNumeroSequentielEtConsigneAuJournal() throws Exception {
        Client marie = clientDAO.rechercherParNom("Marie").get(0);
        TypeMandat permis = typeMandatDAO.chargerParCategorie(marie.getCategorie()).stream()
                .filter(t -> t.nom().equals("Demande de permis")).findFirst().orElseThrow();
        int annee = LocalDate.now().getYear();

        assertEquals(annee + "-0001", dossierDAO.prochainNumero(annee));
        Dossier premier = dossierDAO.creer(nouveauDossier(marie, permis, "Permis de construction", PERIODE));
        Dossier second = dossierDAO.creer(nouveauDossier(marie, permis, "Autre", PERIODE));

        assertEquals(annee + "-0001", premier.getNumero());
        assertEquals(annee + "-0002", second.getNumero());
        assertEquals(annee + "-0003", dossierDAO.prochainNumero(annee));
        List<EntreeJournal> journal = journalDAO.chargerTout();
        assertEquals(2, journal.size());
        assertEquals(ActionJournal.CREATION_DOSSIER, journal.get(0).action());
        assertEquals(premier.getId(), journal.get(0).dossierId());
        assertEquals(2, dossierDAO.compterParClient(marie));
    }

    @Test
    void doublonEstDetecteSeulementPourUnDossierActifDuMemeClientTypeEtPeriode() throws Exception {
        Client marie = clientDAO.rechercherParNom("Marie").get(0);
        List<TypeMandat> types = typeMandatDAO.chargerParCategorie(marie.getCategorie());
        TypeMandat permis = types.stream().filter(t -> t.nom().equals("Demande de permis")).findFirst().orElseThrow();
        TypeMandat livres = types.stream().filter(t -> t.nom().equals("Tenue de livres")).findFirst().orElseThrow();

        assertFalse(dossierDAO.existeDossierActif(marie, permis, PERIODE));
        dossierDAO.creer(nouveauDossier(marie, permis, "x", PERIODE));

        assertTrue(dossierDAO.existeDossierActif(marie, permis, PERIODE));
        assertFalse(dossierDAO.existeDossierActif(marie, permis, PERIODE - 1));
        assertFalse(dossierDAO.existeDossierActif(marie, livres, PERIODE));
    }

    @Test
    void T_001_001_13_echecPendantLaTransactionNeLaisseAucunDossierNiEntreeDeJournal() throws Exception {
        Client marie = clientDAO.rechercherParNom("Marie").get(0);
        TypeMandat inexistant = new TypeMandat(999, "Type fantôme", false);   // viole la clé étrangère

        DonneesException erreur = assertThrows(DonneesException.class,
                () -> dossierDAO.creer(nouveauDossier(marie, inexistant, "x", PERIODE)));

        assertEquals("Le dossier n'a pas pu être créé. Veuillez réessayer.", erreur.getMessage());
        assertFalse(erreur.getMessage().toLowerCase().contains("sql"), "Aucun détail technique affiché.");
        assertEquals(0, dossierDAO.compterParClient(marie));
        assertTrue(journalDAO.chargerTout().isEmpty());
    }

    @Test
    void baseInaccessibleDonneUnMessageSansDetailsTechniques() {
        ConnexionBD inaccessible = new ConnexionBD("jdbc:h2:tcp://localhost:1/inexistante", "sa", "secret");
        DossierDAO_SQL dao = new DossierDAO_SQL(inaccessible);

        DonneesException erreur = assertThrows(DonneesException.class, () -> dao.prochainNumero(2026));

        assertFalse(erreur.getMessage().contains("localhost"));
        assertFalse(inaccessible.toString().contains("secret"), "Le mot de passe n'apparaît jamais dans toString.");
    }

    @Test
    void T_001_001_14_creationsSimultaneesDonnentDesNumerosDifferents() throws Exception {
        Client marie = clientDAO.rechercherParNom("Marie").get(0);
        TypeMandat permis = typeMandatDAO.chargerParCategorie(marie.getCategorie()).stream()
                .filter(t -> t.nom().equals("Demande de permis")).findFirst().orElseThrow();
        int nombreCreations = 6;
        ExecutorService executeur = Executors.newFixedThreadPool(nombreCreations);
        List<Callable<Dossier>> taches = new ArrayList<>();
        for (int i = 0; i < nombreCreations; i++) {
            taches.add(() -> dossierDAO.creer(nouveauDossier(marie, permis, "x", PERIODE)));
        }
        Set<String> numeros = new HashSet<>();
        for (Future<Dossier> resultat : executeur.invokeAll(taches)) {
            numeros.add(resultat.get().getNumero());
        }
        executeur.shutdown();

        assertEquals(nombreCreations, numeros.size());
        assertEquals(nombreCreations, dossierDAO.compterParClient(marie));
        assertEquals(nombreCreations, journalDAO.chargerTout().size());
    }

    @Test
    void journalAccepteUneEntreeSansDossier() throws Exception {
        journalDAO.consigner(new EntreeJournal(LocalDateTime.now(), 5, ActionJournal.ACCES_REFUSE_CREATION_DOSSIER, null));

        EntreeJournal entree = journalDAO.chargerTout().get(0);
        assertEquals(5, entree.utilisateurId());
        assertNull(entree.dossierId());
    }
}
