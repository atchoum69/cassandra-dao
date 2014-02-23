package fr.home.cedric.cassandra.dao;

import java.util.List;

import fr.home.cedric.cassandra.model.User;

/**
 * Interface représentant la couche d'accès aux données pour les utilisateurs.
 * @author cedric
 */
public interface UserDao {
	
	/**
	 * Methode permettant de récupérer l'utilisateur par son identifiant.
	 * @param identifiant identifiant
	 * @return User
	 */
	User getUserById(String identifiant);
	
	/**
	 * Methode permettant de récupérer la liste des utilisateurs.
	 * @return List<User>
	 */
	List<User> getUsers();
	
	/**
	 * Methode permettant d'insérer un utilisateur.
	 * @param utilisateur utilisateur
	 */
	void insert(User utilisateur);
	
	/**
	 * Methode permettant de supprimer un utilisateur.
	 * @param utilisateur utilisateur
	 */
	void delete(User utilisateur);
}
