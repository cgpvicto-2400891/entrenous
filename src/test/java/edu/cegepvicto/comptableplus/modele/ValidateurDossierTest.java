package edu.cegepvicto.comptableplus.modele;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Messages d'erreur affichés sous les champs du formulaire. */
class ValidateurDossierTest {

    private final ValidateurDossier validateur = new ValidateurDossier();
    private final LocalDate aujourdhui = LocalDate.of(2026, 10, 26);

    @Test
    void clientObligatoire() {
        assertEquals("Un client doit être sélectionné.", validateur.validerClient(null));
    }

    @Test
    void typeMandatObligatoire() {
        assertEquals("Un type de mandat doit être sélectionné.", validateur.validerTypeMandat(null));
    }

    @Test
    void dateAbsenteOuInvalide() {
        assertFalse(validateur.validerDateOuverture(null, aujourdhui).isEmpty());
    }

    @Test
    void dateDuJourEtDatePasseeSontValides() {
        assertEquals(ValidateurDossier.VALIDE, validateur.validerDateOuverture(aujourdhui, aujourdhui));
        assertEquals(ValidateurDossier.VALIDE, validateur.validerDateOuverture(aujourdhui.minusYears(1), aujourdhui));
    }

    @Test
    void dateFutureEstRefusee() {
        assertFalse(validateur.validerDateOuverture(aujourdhui.plusDays(1), aujourdhui).isEmpty());
    }

    @Test
    void periodeEntreLesBornesEstValide() {
        assertEquals(ValidateurDossier.VALIDE, validateur.validerPeriodeVisee(2025, 2026));
        assertEquals(ValidateurDossier.VALIDE, validateur.validerPeriodeVisee(2026, 2026));
        assertEquals(ValidateurDossier.VALIDE, validateur.validerPeriodeVisee(ValidateurDossier.PERIODE_MIN, 2026));
    }

    @Test
    void periodeHorsBornesOuAbsenteEstRefusee() {
        assertFalse(validateur.validerPeriodeVisee(null, 2026).isEmpty());
        assertFalse(validateur.validerPeriodeVisee(1999, 2026).isEmpty());
        assertFalse(validateur.validerPeriodeVisee(2027, 2026).isEmpty());
    }

    @Test
    void descriptionFacultativeSansTypeObligatoire() {
        TypeMandat facultatif = new TypeMandat(1, "Tenue de livres", false);

        assertEquals(ValidateurDossier.VALIDE, validateur.validerDescription("", facultatif));
        assertEquals(ValidateurDossier.VALIDE, validateur.validerDescription(null, null));
    }

    @Test
    void messageDeDepassementIndiqueLeNombreDeCaracteres() {
        String message = validateur.validerDescription("a".repeat(501), null);

        assertTrue(message.contains("501/500"));
    }
}
