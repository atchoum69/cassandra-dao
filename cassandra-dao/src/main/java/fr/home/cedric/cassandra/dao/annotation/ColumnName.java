package fr.home.cedric.cassandra.dao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation permettant de spécifier le nom d'une column cassandra associée a
 * un attribut.
 * 
 * @author cedric
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnName {
	/**
	 * Nom de la column.
	 */
	String value();
}
