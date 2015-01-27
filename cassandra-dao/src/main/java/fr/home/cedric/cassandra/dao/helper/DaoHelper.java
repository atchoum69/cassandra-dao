package fr.home.cedric.cassandra.dao.helper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fr.home.cedric.cassandra.dao.annotation.ColumnFamily;
import fr.home.cedric.cassandra.dao.annotation.ColumnName;
import fr.home.cedric.cassandra.dao.annotation.Key;
import me.prettyprint.cassandra.model.RowImpl;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;
import me.prettyprint.hector.api.query.SliceQuery;

/**
 * Utilitaire pour la couche d'acces au donnees.
 * 
 * @author cedric
 */
public class DaoHelper {

	/**
	 * Constantes pour le message d'erreur d'acces.
	 */
	private static final String ERREUR_ACCES = "Erreur d'accès : {}";

	/**
	 * Constantes pour le message d'erreur pour une methode non trouve.
	 */
	private static final String ERREUR_METHODE = "Erreur sur la méthode : {}";

	/**
	 * Constantes pour le message d'erreur de securite.
	 */
	private static final String ERREUR_SECURITE = "Erreur de sécurité : {}";

	/**
	 * Constantes pour le message d'erreur d'arguments passes a une methode.
	 */
	private static final String ERREUR_ARGUMENTS = "Erreur d'arguments : {}";

	/**
	 * Constantes pour le message d'erreur de target.
	 */
	private static final String ERREUR_TARGET = "Erreur de target : {}";
	
	/**
	 * Nombre de résultat max par requête.
	 */
	private static final int MAX_RESULT = 100000;

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DaoHelper.class);

	/**
	 * Keyspace cassandra.
	 */
	@Autowired
	private transient Keyspace keyspace;

	/**
	 * Methode permettant de calculer le nom de la famille de colonne.
	 * 
	 * @param classe
	 *            classe
	 * @return String
	 */
	private String getColumnFamily(final Class<?> classe) {
		String columFamilyName;
		// test si on a une annotation sur l'objet du domaine
		if (classe.isAnnotationPresent(ColumnFamily.class)) {
			// dans ce cas, on recupère le nom de la colum family
			columFamilyName = classe.getAnnotation(ColumnFamily.class).value();
		} else {
			// par defaut, on prend le nom de la classe
			columFamilyName = classe.getSimpleName();
		}
		return columFamilyName;
	}

	/**
	 * Methode permettant de calculer la liste des colums name.
	 * 
	 * @param classe
	 *            classe
	 * @return String[]
	 */
	private String[] getColumnNames(final Class<?> classe) { // NOPMD :
																// Suppression
																// d'une erreur
																// UR
		final List<String> colums = new ArrayList<String>();
		for (final Field attribut : classe.getDeclaredFields()) {
			if (attribut.isAnnotationPresent(ColumnName.class)) {
				// si l'annotation est presente, on prend le nom specifié
				colums.add(attribut.getAnnotation(ColumnName.class).value());
			} else if (!attribut.isAnnotationPresent(Key.class)) {
				// par defaut, on prend le nom de l'attribut
				// attention, l'attribut qui a l'annotation key ne fait pas
				// parti des column name (pas pris en compte)
				colums.add(attribut.getName());
			}
		}
		return colums.toArray(new String[colums.size()]);
	}

	/**
	 * Methode permettant de mapper les valeurs dans l'objet de retour.
	 * 
	 * @param classe
	 *            classe
	 * @param identifiant
	 *            identifiant
	 * @param colonnes
	 *            columnSlice
	 * @param <T>
	 *            Objet du domaine
	 * @return T
	 */
	private <T extends Object> T mapperObject(final Class<T> classe, // NOPMD :
																		// Suppression
																		// d'une
																		// erreur
																		// UR
			final String identifiant, final ColumnSlice<String, String> colonnes) {
		T objet = null; // NOPMD : Suppression d'une erreur DD
		try {
			objet = classe.newInstance();

			// mise a jour de la clé
			setKeyValue(objet, identifiant);

			// mise a jour des colonnes
			for (final HColumn<String, String> colonne : colonnes.getColumns()) {
				setValue(objet, colonne);
			}
		} catch (InstantiationException e) {
			LOGGER.error("Erreur d'instantiation : {}", e.getMessage());
		} catch (IllegalAccessException e) {
			LOGGER.error(ERREUR_ACCES, e.getMessage());
		}
		return objet;
	}

	/**
	 * Methode permettant de setter la valeur de la clé.
	 * 
	 * @param objet
	 *            objet
	 * @param identifiant
	 *            identifiant
	 */
	private void setKeyValue(final Object objet, final String identifiant) {
		final Field key = trouverKey(objet);
		try {
			final Method method = objet.getClass().getMethod(
					"set" + StringUtils.capitalize(key.getName()),
					key.getType());
			method.invoke(objet, identifiant);
		} catch (NoSuchMethodException e) {
			LOGGER.error(ERREUR_METHODE, e.getMessage());
		} catch (SecurityException e) {
			LOGGER.error(ERREUR_SECURITE, e.getMessage());
		} catch (IllegalAccessException e) {
			LOGGER.error(ERREUR_ACCES, e.getMessage());
		} catch (IllegalArgumentException e) {
			LOGGER.error(ERREUR_ARGUMENTS, e.getMessage());
		} catch (InvocationTargetException e) {
			LOGGER.error(ERREUR_TARGET, e.getMessage());
		}
	}

	/**
	 * Methode permettant de récupérer la valeur de la clé.
	 * 
	 * @param objet
	 *            objet
	 * @return String
	 */
	private String getKeyValue(final Object objet) {
		String retour = null; // NOPMD : Suppression d'une erreur DD
		final Field key = trouverKey(objet);
		try {
			final Method method = objet.getClass().getMethod(
					"get" + StringUtils.capitalize(key.getName()),
					new Class[] {});
			retour = (String) method.invoke(objet, (Object[]) null);
		} catch (NoSuchMethodException e) {
			LOGGER.error(ERREUR_METHODE, e.getMessage());
		} catch (SecurityException e) {
			LOGGER.error(ERREUR_SECURITE, e.getMessage());
		} catch (IllegalAccessException e) {
			LOGGER.error(ERREUR_ACCES, e.getMessage());
		} catch (IllegalArgumentException e) {
			LOGGER.error(ERREUR_ARGUMENTS, e.getMessage());
		} catch (InvocationTargetException e) {
			LOGGER.error(ERREUR_TARGET, e.getMessage());
		}
		return retour;
	}

	/**
	 * Methode permettant de setter la valeur.
	 * 
	 * @param objet
	 *            objet
	 * @param colonne
	 *            colonne
	 */
	private void setValue(final Object objet,
			final HColumn<String, String> colonne) {
		final Field attribut = trouverAttribut(objet, colonne.getName());
		try {
			final Method method = objet.getClass() // NOPMD : Suppression d'une
													// erreur DU
					.getMethod(
							"set" + StringUtils.capitalize(attribut.getName()),
							attribut.getType());
			if (attribut.getType().isAssignableFrom(String.class)) {
				method.invoke(objet, colonne.getValue());
			} else if (attribut.getType().isAssignableFrom(Integer.class)) {
				method.invoke(objet, Integer.valueOf(colonne.getValue()));
			} else {
				LOGGER.warn("type non géré : {}", attribut.getType());
			}
		} catch (NoSuchMethodException e) {
			LOGGER.error(ERREUR_METHODE, e.getMessage());
		} catch (SecurityException e) {
			LOGGER.error(ERREUR_SECURITE, e.getMessage());
		} catch (IllegalAccessException e) {
			LOGGER.error(ERREUR_ACCES, e.getMessage());
		} catch (IllegalArgumentException e) {
			LOGGER.error(ERREUR_ARGUMENTS, e.getMessage());
		} catch (InvocationTargetException e) {
			LOGGER.error(ERREUR_TARGET, e.getMessage());
		}
	}

	/**
	 * Methode permettant de récupérer la valeur.
	 * 
	 * @param objet
	 *            objet
	 * @param colonneName
	 *            colonne
	 * @return String
	 */
	private String getValue(final Object objet, final String colonneName) {
		String retour = null; // NOPMD : Suppression d'une erreur DD
		final Field attribut = trouverAttribut(objet, colonneName);
		try {
			final Method method = objet.getClass() // NOPMD : Suppression d'une
													// erreur DU
					.getMethod(
							"get" + StringUtils.capitalize(attribut.getName()),
							new Class[] {});
			if (attribut.getType().isAssignableFrom(String.class)) {
				retour = (String) method.invoke(objet, (Object[]) null);
			} else if (attribut.getType().isAssignableFrom(Integer.class)) {
				retour = Integer.toString((Integer) method.invoke(objet,
						(Object[]) null));
			} else {
				LOGGER.warn("type non géré : {}", attribut.getType());
			}
		} catch (NoSuchMethodException e) {
			LOGGER.error(ERREUR_METHODE, e.getMessage());
		} catch (SecurityException e) {
			LOGGER.error(ERREUR_SECURITE, e.getMessage());
		} catch (IllegalAccessException e) {
			LOGGER.error(ERREUR_ACCES, e.getMessage());
		} catch (IllegalArgumentException e) {
			LOGGER.error(ERREUR_ARGUMENTS, e.getMessage());
		} catch (InvocationTargetException e) {
			LOGGER.error(ERREUR_TARGET, e.getMessage());
		}
		return retour;
	}

	/**
	 * Methode permettant de rechercher l'attribut dans la classe.
	 * 
	 * @param objet
	 *            objet
	 * @param attributName
	 *            nom de l'attribut
	 * @return Field
	 */
	private Field trouverAttribut(final Object objet, // NOPMD : Suppression
														// d'une erreur UR
			final String attributName) {
		Field retour = null; // NOPMD : Suppression d'une erreur DD
		for (final Field attribut : objet.getClass().getDeclaredFields()) {
			if (attribut.getName().equals(attributName)
					|| (attribut.isAnnotationPresent(ColumnName.class) && attribut
							.getAnnotation(ColumnName.class).value()
							.equals(attributName))) {
				retour = attribut;
				break;
			}
		}
		return retour;
	}

	/**
	 * Methode permettant de rechercher la clé dans la classe.
	 * 
	 * @param objet
	 *            objet
	 * @return Field
	 */
	private Field trouverKey(final Object objet) { // NOPMD : Suppression d'une
													// erreur UR
		Field retour = null; // NOPMD : Suppression d'une erreur DD
		for (final Field attribut : objet.getClass().getDeclaredFields()) {
			if (attribut.isAnnotationPresent(Key.class)) {
				retour = attribut;
				break;
			}
		}
		return retour;
	}

	/**
	 * Methode permettant de recherche par l'identifiant.
	 * 
	 * @param classe
	 *            classe
	 * @param identifiant
	 *            identifiant
	 * @param <T>
	 *            Objet du domaine
	 * @return Object
	 */
	public final <T extends Object> T findById(final Class<T> classe,
			final String identifiant) {

		T retour = null; // NOPMD : Suppression d'une erreur DD

		// construit la requête
		final SliceQuery<String, String, String> query = HFactory
				.createSliceQuery(keyspace, StringSerializer.get(),
						StringSerializer.get(), StringSerializer.get());
		query.setColumnFamily(getColumnFamily(classe)).setKey(identifiant)
				.setColumnNames(getColumnNames(classe));

		// exécute la requête
		final QueryResult<ColumnSlice<String, String>> result = query.execute();
		final ColumnSlice<String, String> colonnes = result.get();

		if (!colonnes.getColumns().isEmpty()) {
			retour = mapperObject(classe, identifiant, colonnes);
		}

		return retour;
	}

	/**
	 * Methode permettant de rechercher tous les objets.
	 * 
	 * @param classe
	 *            classe
	 * @param <T>
	 *            Objet du domaine
	 * @return List<?>
	 */
	public final <T extends Object> List<T> findAll(final Class<T> classe) {

		final List<T> retour = new ArrayList<T>();

		// construit la requête
		final RangeSlicesQuery<String, String, String> query = HFactory
				.createRangeSlicesQuery(keyspace, StringSerializer.get(),
						StringSerializer.get(), StringSerializer.get());
		query.setColumnFamily(getColumnFamily(classe)).setKeys(null, null)
				.setColumnNames(getColumnNames(classe));
		query.setRowCount(MAX_RESULT);

		// exécute la requête
		final QueryResult<OrderedRows<String, String, String>> result = query
				.execute();
		final OrderedRows<String, String, String> rows = result.get();

		for (final Iterator<?> iterator = rows.iterator(); iterator.hasNext();) {
			@SuppressWarnings("unchecked")
			final RowImpl<String, String, String> ligne = (RowImpl<String, String, String>) iterator
					.next();
			final T objet = mapperObject(classe, ligne.getKey(),
					ligne.getColumnSlice());
			retour.add(objet);
		}

		return retour;
	}

	/**
	 * Methode permettant d'inserer un objet.
	 * 
	 * @param type
	 *            objet a inserer
	 * @param <T>
	 *            Object du domaine
	 */
	public final <T extends Object> void insertRow(final T type) { // NOPMD :
																	// Suppression
																	// d'une
																	// erreur UR

		final Mutator<String> mutator = HFactory.createMutator(keyspace,
				StringSerializer.get());

		// recupere la cle
		final String identifiant = getKeyValue(type); // NOPMD : Suppression
														// d'une erreur DU

		// recupere la column family
		final String columnFamily = getColumnFamily(type.getClass()); // NOPMD :
																		// Suppression
																		// d'une
																		// erreur
																		// DU

		// recupere la liste des colonnes
		final String[] columnNames = getColumnNames(type.getClass());

		for (final String colonne : columnNames) {
			mutator.addInsertion(identifiant, columnFamily, HFactory
					.createStringColumn(colonne, getValue(type, colonne)));
		}

		// execute l'insertion
		mutator.execute();
	}

	/**
	 * Methode permettant d'inserer un objet.
	 * 
	 * @param type
	 *            objet a inserer
	 * @param <T>
	 *            Object du domaine
	 */
	public final <T extends Object> void deleteRow(final T type) {

		final Mutator<String> mutator = HFactory.createMutator(keyspace,
				StringSerializer.get());

		// recupere la cle
		final String identifiant = getKeyValue(type);

		// recupere la column family
		final String columnFamily = getColumnFamily(type.getClass());

		// execute la suppression
		mutator.addDeletion(identifiant, columnFamily, null, StringSerializer.get());
		mutator.execute();
	}
}
