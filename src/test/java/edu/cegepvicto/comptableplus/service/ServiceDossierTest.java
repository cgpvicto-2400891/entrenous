package edu.cegepvicto.comptableplus.service;

import edu.cegepvicto.comptableplus.dao.DonneesException;
import edu.cegepvicto.comptableplus.dao.DossierDAO;
import edu.cegepvicto.comptableplus.dao.memoire.ClientDAO_Memoire;
import edu.cegepvicto.comptableplus.dao.memoire.DepotMemoire;
import edu.cegepvicto.comptableplus.dao.memoire.DossierDAO_Memoire;
import edu.cegepvicto.comptableplus.dao.memoire.JournalDAO_Memoire;
import edu.cegepvicto.comptableplus.dao.memoire.TypeMandatDAO_Memoire;
import edu.cegepvicto.comptableplus.modele.ActionJournal;
import edu.cegepvicto.comptableplus.modele.Client;
import edu.cegepvicto.comptableplus.modele.Dossier;
import edu.cegepvicto.comptableplus.modele.EntreeJournal;
import edu.cegepvicto.comptableplus.modele.Role;
import edu.cegepvicto.comptableplus.modele.Session;
import edu.cegepvicto.comptableplus.modele.StatutDossier;
import edu.cegepvicto.comptableplus.modele.TypeMandat;
import edu.cegepvicto.comptableplus.modele.Utilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Plan de test du CU-001-001 sur le service (DAO en mémoire) : T-01 à T-04, T-11, T-13 à T-15. */
class ServiceDossierTest {

    private static final int PERIODE = LocalDate.now().getYear() - 1;

    private DepotMemoire depot;
    private LocalisateurServices services;
    private ServiceDossier service;
    private JournalDAO_Memoire journal;
    private Session sessionGestionnaire;
    private Client marie;
    private Client boulangerie;
    private TypeMandat permis;
    private TypeMandat autre;

    @BeforeEach
    void initialiser() throws DonneesException {
        depot = DepotMemoire.avecDonneesDemo();
        services = LocalisateurServices.enMemoire(depot);
        service = services.dossiers();
        journal = new JournalDAO_Memoire(depot);
        sessionGestionnaire = session(Role.GESTIONNAIRE, Duration.ofHours(1));
        marie = service.rechercherClients("Marie").get(0);
        boulangerie = service.rechercherClients("Boulangerie").get(0);
        permis = typeParNom(marie, "Demande de permis");
        autre = typeParNom(marie, "Autre (sur mesure)");
    }

    private Session session(Role role, Duration validite) {
        Clock horloge = Clock.systemDefaultZone();
        return new Session(new Utilisateur(1, "Gestionnaire", role), Instant.now(horloge).plus(validite), horloge);
    }

    private TypeMandat typeParNom(Client client, String nom) throws DonneesException {
        return service.typesMandatOffertsA(client).stream().filter(t -> t.nom().equals(nom)).findFirst().orElseThrow();
    }

    private Dossier nouveauDossier(Client client, TypeMandat type, String description, int periode) {
        return new Dossier(client, type, LocalDate.now(), periode, description, 1);
    }

    @Test
    void T_001_001_01_clientExistantDossierCreeActifAvecNumeroUnique() throws Exception {
        Dossier cree = service.creerDossier(sessionGestionnaire, nouveauDossier(marie, permis, "Permis de construction", PERIODE));

        assertEquals(StatutDossier.ACTIF, cree.getStatut());
        assertEquals(Year.now().getValue() + "-0053", cree.getNumero());
        assertNotNull(cree.getId());
        assertEquals(1, cree.getGestionnaireId(), "Le dossier est associé au gestionnaire connecté.");
    }

    @Test
    void T_001_001_02_autreDescriptionValideLesDonneesSontConservees() throws Exception {
        Dossier cree = service.creerDossier(sessionGestionnaire, nouveauDossier(marie, permis, "Autre description", PERIODE));

        assertEquals("Autre description", cree.getDescription());
        assertEquals(permis, cree.getTypeMandat());
    }

    @Test
    void T_001_001_03_clientEntrepriseDossierCree() throws Exception {
        TypeMandat tenue = typeParNom(boulangerie, "Tenue de livres");

        Dossier cree = service.creerDossier(sessionGestionnaire, nouveauDossier(boulangerie, tenue, "", PERIODE));

        assertEquals(boulangerie, cree.getClient());
    }

    @Test
    void T_001_001_05_typeAutreAvecDescriptionEstCree() throws Exception {
        Dossier cree = service.creerDossier(sessionGestionnaire, nouveauDossier(marie, autre, "Mandat spécial", PERIODE));

        assertNotNull(cree.getNumero());
    }

    @Test
    void typesDeMandatOffertsDependentDeLaCategorie() throws Exception {
        List<String> pourParticulier = service.typesMandatOffertsA(marie).stream().map(TypeMandat::nom).toList();
        List<String> pourEntreprise = service.typesMandatOffertsA(boulangerie).stream().map(TypeMandat::nom).toList();

        assertTrue(pourParticulier.contains("Déclaration de revenus"));
        assertFalse(pourParticulier.contains("Déclaration de société"));
        assertTrue(pourEntreprise.contains("Déclaration de société"));
        assertFalse(pourEntreprise.contains("Déclaration de revenus"));
    }

    @Test
    void T_001_001_11_dossierActifDuMemeTypeEstDetecte() throws Exception {
        TypeMandat declaration = typeParNom(marie, "Déclaration de revenus");

        assertTrue(service.existeDoublon(marie, declaration, PERIODE), "Un dossier actif existe dans les données de démo.");
        assertFalse(service.existeDoublon(marie, declaration, PERIODE - 1), "Autre période : pas un doublon.");
        assertFalse(service.existeDoublon(marie, permis, PERIODE), "Autre type : pas un doublon.");
    }

    @Test
    void T_001_001_11_creationApresAvertissementEstPermise() throws Exception {
        TypeMandat declaration = typeParNom(marie, "Déclaration de revenus");

        Dossier cree = service.creerDossier(sessionGestionnaire, nouveauDossier(marie, declaration, "", PERIODE));

        assertNotNull(cree.getNumero());
        assertEquals(2, service.nombreDossiers(marie));
    }

    @Test
    void T_001_001_13_echecDeLaBaseAucunDossierIncompletEtDonneesInchangees() {
        DossierDAO reel = new DossierDAO_Memoire(depot);
        DossierDAO enPanne = new DossierDAO() {
            @Override
            public String prochainNumero(int annee) throws DonneesException {
                return reel.prochainNumero(annee);
            }

            @Override
            public int compterParClient(Client client) throws DonneesException {
                return reel.compterParClient(client);
            }

            @Override
            public boolean existeDossierActif(Client client, TypeMandat type, int periode) throws DonneesException {
                return reel.existeDossierActif(client, type, periode);
            }

            @Override
            public Dossier creer(Dossier dossier) throws DonneesException {
                throw new DonneesException("Le dossier n'a pas pu être créé. Veuillez réessayer.", null);
            }
        };
        ServiceDossier serviceEnPanne = new LocalisateurServices(new ClientDAO_Memoire(depot),
                new TypeMandatDAO_Memoire(depot), enPanne, journal).dossiers();

        DonneesException erreur = assertThrows(DonneesException.class,
                () -> serviceEnPanne.creerDossier(sessionGestionnaire, nouveauDossier(marie, permis, "x", PERIODE)));

        assertEquals("Le dossier n'a pas pu être créé. Veuillez réessayer.", erreur.getMessage());
        assertEquals(1, new DossierDAO_Memoire(depot).compterParClient(marie), "Seul le dossier de démo existe.");
    }

    @Test
    void T_001_001_14_deuxGestionnairesEnMemeTempsObtiennentDesNumerosDifferents() throws Exception {
        int nombreCreations = 20;
        ExecutorService executeur = Executors.newFixedThreadPool(8);
        List<Callable<Dossier>> taches = new ArrayList<>();
        for (int i = 0; i < nombreCreations; i++) {
            taches.add(() -> service.creerDossier(sessionGestionnaire, nouveauDossier(marie, permis, "x", PERIODE)));
        }
        Set<String> numeros = new HashSet<>();
        for (Future<Dossier> resultat : executeur.invokeAll(taches)) {
            numeros.add(resultat.get().getNumero());
        }
        executeur.shutdown();

        assertEquals(nombreCreations, numeros.size());
    }

    @Test
    void T_001_001_15_technicienRefuseEtTentativeConsignee() {
        Session technicien = session(Role.TECHNICIEN, Duration.ofHours(1));

        AccesRefuseException erreur = assertThrows(AccesRefuseException.class,
                () -> service.creerDossier(technicien, nouveauDossier(marie, permis, "x", PERIODE)));

        assertEquals("Accès refusé.", erreur.getMessage());
        List<EntreeJournal> entrees = journal.chargerTout();
        assertEquals(1, entrees.size());
        assertEquals(ActionJournal.ACCES_REFUSE_CREATION_DOSSIER, entrees.get(0).action());
        assertNull(entrees.get(0).dossierId());
        assertEquals(1, new DossierDAO_Memoire(depot).compterParClient(marie), "Aucun dossier créé.");
    }

    @Test
    void sessionExpireeDemandeDeSeReconnecter() {
        Session expiree = session(Role.GESTIONNAIRE, Duration.ofSeconds(-1));

        assertThrows(SessionExpireeException.class,
                () -> service.creerDossier(expiree, nouveauDossier(marie, permis, "x", PERIODE)));
    }

    @Test
    void boutonDesactiveSeulementPourUnNonGestionnaireOuUneSessionExpiree() {
        ServiceAutorisation autorisation = services.autorisation();

        assertTrue(autorisation.peutCreerDossier(sessionGestionnaire));
        assertFalse(autorisation.peutCreerDossier(session(Role.TECHNICIEN, Duration.ofHours(1))));
        assertFalse(autorisation.peutCreerDossier(session(Role.GESTIONNAIRE, Duration.ofSeconds(-1))));
    }

    @Test
    void dossierDunAutreGestionnaireEstRefuse() {
        Dossier dossierAutreGestionnaire = new Dossier(marie, permis, LocalDate.now(), PERIODE, "x", 99);

        assertThrows(IllegalArgumentException.class,
                () -> service.creerDossier(sessionGestionnaire, dossierAutreGestionnaire));
    }

    @Test
    void creationEstConsigneeAuJournalAvecLeGestionnaire() throws Exception {
        Dossier cree = service.creerDossier(sessionGestionnaire, nouveauDossier(marie, permis, "x", PERIODE));

        EntreeJournal entree = journal.chargerTout().get(0);
        assertEquals(ActionJournal.CREATION_DOSSIER, entree.action());
        assertEquals(1, entree.utilisateurId());
        assertEquals(cree.getId(), entree.dossierId());
    }

    @Test
    void rechercheIgnoreLaCasseEtRetourneVideSiTexteVide() throws Exception {
        assertEquals(1, service.rechercherClients("tremblay").size());
        assertTrue(service.rechercherClients("  ").isEmpty());
        assertTrue(service.rechercherClients("zzz").isEmpty());
    }
}
