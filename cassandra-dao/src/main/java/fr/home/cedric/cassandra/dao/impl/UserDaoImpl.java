package fr.home.cedric.cassandra.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import fr.home.cedric.cassandra.dao.UserDao;
import fr.home.cedric.cassandra.dao.helper.DaoHelper;
import fr.home.cedric.cassandra.model.User;

/**
 * Implémentation de la couche d'accès aux données pour les utilisateurs.
 * 
 * @author cedric
 */
@Repository
public class UserDaoImpl extends DaoHelper implements UserDao {

	/**
	 * {@inheritDoc}
	 */
	public final User getUserById(final String identifiant) {
		return findById(User.class, identifiant);
	}

	/**
	 * {@inheritDoc}
	 */
	public final List<User> getUsers() {
		return findAll(User.class);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void insert(final User utilisateur) {
		insertRow(utilisateur);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final void delete(final User utilisateur) {
		deleteRow(utilisateur);
	}
}
