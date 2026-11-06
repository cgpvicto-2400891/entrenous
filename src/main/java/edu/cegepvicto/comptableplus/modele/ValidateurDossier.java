package edu.cegepvicto.comptableplus.modele;

import java.time.LocalDate;

/**
 * Règles de validation d'un dossier client. Chaque méthode retourne le message d'erreur à afficher,
 * ou une chaîne vide si la valeur est valide. Le formulaire et le modèle {@link Dossier} utilisent
 * les mêmes règles (DRY) : le formulaire affiche le message, le modèle refuse de se construire.
 */
public class ValidateurDossier {

    /** Longueur maximale de la description d'un dossier. */
    public static final int LONGUEUR_MAX_DESCRIPTION = 500;

    /** Première année financière acceptée comme période visée. */
    public static final int PERIODE_MIN = 2000;

    /** Valeur retournée quand aucune erreur n'est détectée. */
    public static final String VALIDE = "";

    /**
     * Vérifie qu'un client est sélectionné.
     * @param client le client choisi, ou {@code null}.
     * @return le message d'erreur, ou {@link #VALIDE}.
     */
    public String validerClient(Client client) {
        return client == null ? "Un client doit être sélectionné." : VALIDE;
    }

    /**
     * Vérifie qu'un type de mandat est sélectionné.
     * @param typeMandat le type choisi, ou {@code null}.
     * @return le message d'erreur, ou {@link #VALIDE}.
     */
    public String validerTypeMandat(TypeMandat typeMandat) {
        return typeMandat == null ? "Un type de mandat doit être sélectionné." : VALIDE;
    }

    /**
     * Vérifie la date d'ouverture : elle est obligatoire et ne peut pas être dans le futur.
     * @param date       la date saisie, ou {@code null}.
     * @param aujourdhui la date du jour.
     * @return le message d'erreur, ou {@link #VALIDE}.
     */
    public String validerDateOuverture(LocalDate date, LocalDate aujourdhui) {
        if (date == null) {
            return "La date d'ouverture est obligatoire (format aaaa-mm-jj).";
        }
        if (date.isAfter(aujourdhui)) {
            return "La date d'ouverture ne peut pas être dans le futur.";
        }
        return VALIDE;
    }

    /**
     * Vérifie la période visée (année financière).
     * @param periode      l'année choisie, ou {@code null}.
     * @param anneeCourante l'année en cours, qui est la dernière période acceptée.
     * @return le message d'erreur, ou {@link #VALIDE}.
     */
    public String validerPeriodeVisee(Integer periode, int anneeCourante) {
        if (periode == null) {
            return "La période visée est obligatoire.";
        }
        if (periode < PERIODE_MIN || periode > anneeCourante) {
            return "La période visée doit être entre " + PERIODE_MIN + " et " + anneeCourante + ".";
        }
        return VALIDE;
    }

    /**
     * Vérifie la description : longueur maximale, caractères permis et présence si le type l'exige.
     * @param description le texte saisi, ou {@code null}.
     * @param typeMandat  le type de mandat choisi, ou {@code null} s'il n'est pas encore choisi.
     * @return le message d'erreur, ou {@link #VALIDE}.
     */
    public String validerDescription(String description, TypeMandat typeMandat) {
        String texte = description == null ? "" : description;
        if (texte.length() > LONGUEUR_MAX_DESCRIPTION) {
            return "La description ne peut pas dépasser " + LONGUEUR_MAX_DESCRIPTION
                    + " caractères (" + texte.length() + "/" + LONGUEUR_MAX_DESCRIPTION + ").";
        }
        if (contientCaractereInterdit(texte)) {
            return "La description contient des caractères non permis.";
        }
        if (typeMandat != null && typeMandat.descriptionObligatoire() && texte.isBlank()) {
            return "La description est obligatoire pour ce type de mandat.";
        }
        return VALIDE;
    }

    /** Les retours de ligne et tabulations sont permis, les autres caractères de contrôle ne le sont pas. */
    private boolean contientCaractereInterdit(String texte) {
        return texte.chars().anyMatch(c -> Character.isISOControl(c) && c != '\n' && c != '\r' && c != '\t');
    }
}
