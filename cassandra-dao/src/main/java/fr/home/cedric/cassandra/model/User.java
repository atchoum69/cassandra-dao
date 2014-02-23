package fr.home.cedric.cassandra.model;

import fr.home.cedric.cassandra.dao.annotation.ColumnFamily;
import fr.home.cedric.cassandra.dao.annotation.ColumnName;
import fr.home.cedric.cassandra.dao.annotation.Key;

/**
 * Objet du domaine représentant un utilisateur.
 * 
 * @author cedric
 */
@ColumnFamily("User")
public class User {

	/**
	 * Identifiant.
	 */
	@Key
	private String identifiant;
	
	/**
	 * Nom.
	 */
	@ColumnName("last")
	private String nom;

	/**
	 * Prénom.
	 */
	@ColumnName("first")
	private String prenom;

	/**
	 * Age.
	 */
	private Integer age;

	/**
	 * Permet de récupérer l'identifiant.
	 * @return String
	 */
	public final String getIdentifiant() {
		return identifiant;
	}

	/**
	 * Permet de modifier l'identifiant.
	 * @param ident identifiant
	 */
	public final void setIdentifiant(final String ident) {
		this.identifiant = ident;
	}

	/**
	 * Permet de récupérer le nom.
	 * @return String
	 */
	public final String getNom() {
		return nom;
	}

	/**
	 * Permet de modifier le nom.
	 * @param last nom
	 */
	public final void setNom(final String last) {
		this.nom = last;
	}

	/**
	 * Permet de récupérer le prénom.
	 * @return String
	 */
	public final String getPrenom() {
		return prenom;
	} 

	/**
	 * Permet de modifier le prénom.
	 * @param first prénom
	 */
	public final void setPrenom(final String first) {
		this.prenom = first;
	}

	/**
	 * Permet de récupérer l'age.
	 * @return Integer
	 */
	public final Integer getAge() {
		return age;
	}

	/**
	 * Permet de modifier l'age.
	 * @param value age
	 */
	public final void setAge(final Integer value) {
		this.age = value;
	}
}
