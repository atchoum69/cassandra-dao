package fr.home.cedric.cassandra.dao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation permettant de spécifier la column family associée a la classe.
 * @author cedric
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnFamily {
	/**
	 * Nom de la column family.
	 */
	String value();
}
