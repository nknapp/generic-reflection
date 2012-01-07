package org.knappi.opensource.genericreflection;

import java.util.Collection;
import java.util.HashSet;

import org.knappi.opensource.genericreflection.ResolvedGenericType;


/**
 * Class to test the {@link ResolvedGenericType}. The second parameter is 
 * the parameter of the hashset, so a {@link HashSetOfX}&lt;T,X> should 
 * always be a {@link Collection}&lt;X>.
 * @author knappmeier
 *
 * @param <T> the irrelevant generic type.
 * @param <X> the relevant generic type.
 */
public class HashSetOfX<T,X> extends HashSet<X> implements InterfaceT<T>, InterfaceX<X> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
