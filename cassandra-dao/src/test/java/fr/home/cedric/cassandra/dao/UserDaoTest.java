//CHECKSTYLE:OFF
package fr.home.cedric.cassandra.dao;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.home.cedric.cassandra.model.User;

/**
 * Classe permettant de tester la DAO pour les utilisateurs.
 * 
 * @author cedric
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = 
	{ "classpath:applicationContext-cassandra-test.xml" })
public class UserDaoTest {

	/**
	 * Age du user1.
	 */
	private static final int AGE_USER1 = 34;
	
	/**
	 * Dao pour accèder aux utilisateurs.
	 */
	@Autowired
	private transient UserDao userDao;

	/**
	 * Permet de tester la méthode getUserById de la Dao.
	 */
	@Test
	public final void getUserById() {
		final String identifiant = "user1"; // NOPMD variable locale

		// cas d'un utilisateur trouvé
		User user = userDao.getUserById(identifiant);
		Assert.assertNotNull("L'utilisateur aurait du être trouvé", user);
		Assert.assertEquals("L'identifiant de l'utilisateur n'est pas le même",
				identifiant, user.getIdentifiant());
		Assert.assertEquals("Le nom de l'utilisateur n'est pas le même",
				"Lefort", user.getNom());
		Assert.assertEquals("Le prénom de l'utilisateur n'est pas le même",
				"Cedric", user.getPrenom());
		Assert.assertEquals("L'age n'est pas correct", AGE_USER1, 
				user.getAge().intValue());
		
		// cas d'un utilisateur non trouvé
		user = userDao.getUserById("zorg");
		Assert.assertNull("L'utilisateur n'aurait pas du être trouvé", user);
	}
	
	/**
	 * Permet de tester la méthode getUsers de la Dao.
	 */
	@Test
	public final void getUsers() {
		final String identifiant = "user1"; // NOPMD variable locale

		// cas d'un utilisateur trouvé
		final List<User> users = userDao.getUsers();
		Assert.assertNotNull("La liste ne devrait pas être null", users);
		Assert.assertFalse("La liste ne devrait pas être vide", 
				users.isEmpty());
		
		int index = 0;
		for (User user : users) {
			if (user.getIdentifiant().equals(identifiant)) {
				break;
			}
			index++;
		}
		
		final User user = users.get(index);
		Assert.assertNotNull("L'utilisateur aurait du être trouvé", user);
		Assert.assertEquals("L'identifiant de l'utilisateur n'est pas le même",
				identifiant, user.getIdentifiant());
		Assert.assertEquals("Le nom de l'utilisateur n'est pas le même",
				"Lefort", user.getNom());
		Assert.assertEquals("Le prénom de l'utilisateur n'est pas le même",
				"Cedric", user.getPrenom());
		Assert.assertEquals("L'age n'est pas correct", AGE_USER1, 
				user.getAge().intValue());
	}
	
	/**
	 * Permet de tester les méthodes insert et delete de la Dao.
	 */
	@Test
	public final void insertAndDelete() {
		
		final User utilisateur = new User();
		utilisateur.setIdentifiant("user2");
		utilisateur.setNom("Marlin");
		utilisateur.setPrenom("Aurelie");
		utilisateur.setAge(AGE_USER1);
		
		// insert l'utilisateur
		userDao.insert(utilisateur);
		
		final User user = userDao.getUserById(utilisateur.getIdentifiant());
		Assert.assertNotNull("L'utilisateur aurait du être trouvé", user);
		Assert.assertEquals("L'identifiant de l'utilisateur n'est pas le même",
				utilisateur.getIdentifiant(), user.getIdentifiant());
		Assert.assertEquals("Le nom de l'utilisateur n'est pas le même",
				"Marlin", user.getNom());
		Assert.assertEquals("Le prénom de l'utilisateur n'est pas le même",
				"Aurelie", user.getPrenom());
		Assert.assertEquals("L'age n'est pas correct", AGE_USER1, 
				user.getAge().intValue());
		
		// charge l'utilisateur 2
		final User user2 = userDao.getUserById("user2");
		Assert.assertNotNull("L'utilisateur aurait du être trouvé", user2);
		Assert.assertEquals("L'identifiant de l'utilisateur n'est pas le même",
				"user2", user2.getIdentifiant());
		Assert.assertEquals("Le nom de l'utilisateur n'est pas le même",
				"Marlin", user2.getNom());
		Assert.assertEquals("Le prénom de l'utilisateur n'est pas le même",
				"Aurelie", user2.getPrenom());
		Assert.assertEquals("L'age n'est pas correct", AGE_USER1, 
				user2.getAge().intValue());
		
		// insert l'utilisateur
		userDao.delete(user2);
		
		final User utilisateur2 = userDao.getUserById(user2.getIdentifiant());
		Assert.assertNull("L'utilisateur n'aurait pas du être trouvé", utilisateur2);
	}
}

// CHECKSTYLE:ON