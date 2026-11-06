package edu.cegepvicto.comptableplus.modele;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Règles d'intégrité du dossier (plan de test du CU-001-001 : T-05, T-06, T-07, T-08). */
class DossierTest {

    private static final int PERIODE = LocalDate.now().getYear() - 1;

    private CategorieClient particulier;
    private ClientParticulier client;
    private TypeMandat permis;
    private TypeMandat autre;

    @BeforeEach
    void initialiser() {
        particulier = new CategorieClient(1, "PARTICULIER", "Particulier");
        client = new ClientParticulier(1, particulier, "m@exemple.ca", "819 555-0101", "1, rue A", "Marie", "Tremblay");
        permis = new TypeMandat(4, "Demande de permis", false);
        autre = new TypeMandat(5, "Autre (sur mesure)", true);
    }

    private Dossier dossierAvecDescription(TypeMandat type, String description) {
        return new Dossier(client, type, LocalDate.now(), PERIODE, description, 1);
    }

    @Test
    void T_001_001_01_nouveauDossierEstActifSansNumero() {
        Dossier dossier = dossierAvecDescription(permis, "Permis de construction");

        assertEquals(StatutDossier.ACTIF, dossier.getStatut());
        assertNull(dossier.getNumero(), "Le numéro est attribué à l'enregistrement, pas avant.");
        assertEquals("Permis de construction", dossier.getDescription());
    }

    @Test
    void T_001_001_05_typeAutreAvecDescriptionEstAccepte() {
        Dossier dossier = dossierAvecDescription(autre, "Mandat sur mesure");

        assertEquals("Mandat sur mesure", dossier.getDescription());
    }

    @Test
    void typeAutreSansDescriptionEstRefuse() {
        IllegalArgumentException erreur =
                assertThrows(IllegalArgumentException.class, () -> dossierAvecDescription(autre, "   "));

        assertTrue(erreur.getMessage().contains("obligatoire"));
    }

    @Test
    void T_001_001_06_descriptionDe500CaracteresEstAcceptee() {
        Dossier dossier = dossierAvecDescription(permis, "a".repeat(500));

        assertEquals(500, dossier.getDescription().length());
    }

    @Test
    void T_001_001_07_descriptionDe501CaracteresEstRefusee() {
        IllegalArgumentException erreur =
                assertThrows(IllegalArgumentException.class, () -> dossierAvecDescription(permis, "a".repeat(501)));

        assertTrue(erreur.getMessage().contains("500"));
    }

    @Test
    void T_001_001_08_sansClientLeDossierEstRefuse() {
        assertThrows(IllegalArgumentException.class,
                () -> new Dossier(null, permis, LocalDate.now(), PERIODE, "x", 1));
    }

    @Test
    void dateOuvertureDansLeFuturEstRefusee() {
        assertThrows(IllegalArgumentException.class,
                () -> new Dossier(client, permis, LocalDate.now().plusDays(1), PERIODE, "x", 1));
    }

    @Test
    void descriptionAvecCaractereDeControleEstRefusee() {
        assertThrows(IllegalArgumentException.class, () -> dossierAvecDescription(permis, "abc\u0000def"));
    }

    @Test
    void descriptionPeutContenirDesRetoursDeLigne() {
        Dossier dossier = dossierAvecDescription(permis, "ligne 1\nligne 2");

        assertEquals("ligne 1\nligne 2", dossier.getDescription());
    }

    @Test
    void avecIdentificationConserveLesDonneesEtAjouteLeNumero() {
        Dossier enregistre = dossierAvecDescription(permis, "x").avecIdentification(7, "2026-0053");

        assertEquals(7, enregistre.getId());
        assertEquals("2026-0053", enregistre.getNumero());
        assertEquals(client, enregistre.getClient());
    }
}
