package edu.cegepvicto.comptableplus.modele;

import java.time.LocalDate;

/**
 * Dossier ouvert pour un client. Un dossier est immuable : le numéro et l'identifiant sont attribués
 * par la base de données au moment de l'enregistrement (voir {@link #avecIdentification(int, String)}).
 * Le constructeur refuse toute donnée qui ne respecte pas les règles de {@link ValidateurDossier}.
 */
public class Dossier {

    private static final ValidateurDossier VALIDATEUR = new ValidateurDossier();

    private final Integer id;
    private final String numero;
    private final LocalDate dateOuverture;
    private final StatutDossier statut;
    private final int periodeVisee;
    private final String description;
    private final Client client;
    private final TypeMandat typeMandat;
    private final int gestionnaireId;

    /**
     * Prépare un nouveau dossier : statut « Actif », sans numéro ni identifiant.
     * @param client         le client du dossier.
     * @param typeMandat     le type de mandat.
     * @param dateOuverture  la date d'ouverture (au plus aujourd'hui).
     * @param periodeVisee   l'année financière visée.
     * @param description    la description (500 caractères au maximum), possiblement vide.
     * @param gestionnaireId l'identifiant du gestionnaire connecté (base centrale).
     * @throws IllegalArgumentException si une donnée ne respecte pas les règles du dossier.
     */
    public Dossier(Client client, TypeMandat typeMandat, LocalDate dateOuverture, int periodeVisee,
                   String description, int gestionnaireId) {
        this(null, null, dateOuverture, StatutDossier.ACTIF, periodeVisee, description, client, typeMandat,
                gestionnaireId);
    }

    /**
     * Reconstruit un dossier déjà enregistré (chargement depuis la base de données).
     * @param id             identifiant du dossier, ou {@code null} s'il n'est pas encore enregistré.
     * @param numero         numéro au format AAAA-NNNN, ou {@code null} s'il n'est pas encore attribué.
     * @param dateOuverture  la date d'ouverture.
     * @param statut         le statut du dossier.
     * @param periodeVisee   l'année financière visée.
     * @param description    la description, possiblement vide.
     * @param client         le client du dossier.
     * @param typeMandat     le type de mandat.
     * @param gestionnaireId l'identifiant du gestionnaire (base centrale).
     * @throws IllegalArgumentException si une donnée ne respecte pas les règles du dossier.
     */
    public Dossier(Integer id, String numero, LocalDate dateOuverture, StatutDossier statut, int periodeVisee,
                   String description, Client client, TypeMandat typeMandat, int gestionnaireId) {
        LocalDate aujourdhui = LocalDate.now();
        exiger(VALIDATEUR.validerClient(client));
        exiger(VALIDATEUR.validerTypeMandat(typeMandat));
        exiger(VALIDATEUR.validerDateOuverture(dateOuverture, aujourdhui));
        exiger(VALIDATEUR.validerPeriodeVisee(periodeVisee, aujourdhui.getYear()));
        exiger(VALIDATEUR.validerDescription(description, typeMandat));
        if (statut == null) {
            throw new IllegalArgumentException("Un dossier doit avoir un statut.");
        }
        this.id = id;
        this.numero = numero;
        this.dateOuverture = dateOuverture;
        this.statut = statut;
        this.periodeVisee = periodeVisee;
        this.description = description == null ? "" : description.strip();
        this.client = client;
        this.typeMandat = typeMandat;
        this.gestionnaireId = gestionnaireId;
    }

    /**
     * Retourne une copie du dossier avec l'identifiant et le numéro attribués à l'enregistrement.
     * @param nouvelId     identifiant généré par la base de données.
     * @param nouveauNumero numéro au format AAAA-NNNN.
     * @return le dossier enregistré.
     */
    public Dossier avecIdentification(int nouvelId, String nouveauNumero) {
        return new Dossier(nouvelId, nouveauNumero, dateOuverture, statut, periodeVisee, description, client,
                typeMandat, gestionnaireId);
    }

    private void exiger(String messageErreur) {
        if (!ValidateurDossier.VALIDE.equals(messageErreur)) {
            throw new IllegalArgumentException(messageErreur);
        }
    }

    /** @return l'identifiant, ou {@code null} si le dossier n'est pas encore enregistré. */
    public Integer getId() {
        return id;
    }

    /** @return le numéro AAAA-NNNN, ou {@code null} s'il n'est pas encore attribué. */
    public String getNumero() {
        return numero;
    }

    public LocalDate getDateOuverture() {
        return dateOuverture;
    }

    public StatutDossier getStatut() {
        return statut;
    }

    public int getPeriodeVisee() {
        return periodeVisee;
    }

    public String getDescription() {
        return description;
    }

    public Client getClient() {
        return client;
    }

    public TypeMandat getTypeMandat() {
        return typeMandat;
    }

    public int getGestionnaireId() {
        return gestionnaireId;
    }
}
