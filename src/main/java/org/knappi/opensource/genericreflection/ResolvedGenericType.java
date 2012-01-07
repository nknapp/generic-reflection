package org.knappi.opensource.genericreflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * This class is a utility for resolving type parameters of generic type. The problem it was created for, is the following:
 * Given a field with any generic type, (i.e. <code>HashSetOfX&lt;String,Integer&gt;</code>) 
 * which is defined as 
 * <blockquote><code>public class HashSetOfX&lt;T,X&gt; extends HashSet&lt;X&gt;</code></blockquote>
 * we want to find out, 
 * <ul>
 * 	<li>whether it is assignable to a {@link Collection}</li>
 *  <li>what the actual type parameter of the {@link Collection} (i.e. the type of the stored objects)
 * </ul>
 * 
 * In order to achieve this goals,this class contains simplified information of {@link ParameterizedType} 
 * and assumes that {@link #rawType} and {@link #actualParameters} can be represented as class object.
 * 
 * <p>It then provides methods to resolve the superclass and interfaces of the resolved type whereby the 
 * actual type parameters (&lt;String,Integer&gt;) are mapped to the {@link TypeVariable}s of the raw superclass
 * (Hashset&lt;T&gt;) via the definition of the generic superclass ( <code>&lt;P,X&gt;</code> &nbsp;-&gt;&nbsp; </code>&lt;X&gt;</code>).
 * </p>
 * Thus, the following chain of calls {@link #resolveSuperclass()} and calls {@link #resolveInterfaces()} can be resolved for <code>HashSetOfX&lt;String,Integer&gt;</code>
 * <p>
 * <table border="1" width="50%" align="center">
 * 		<tr>
 * 			<th colspan="2">Transformation</th>
 * 			<th rowspan="2">ResolvedGenericType</th>
 * 		</tr>
 * 		<tr>
 * 			<th>From</th>
 * 			<th>To</th>
 *  	<tr>
 *  		<td colspan="2" align="center">Initial generic type</td>
 *  		<td><code>HashSetOfX&lt;String,Integer&gt;</code></td>
 *  	</tr>
 *  	<tr>
 *  		<td><code>HashSetOfX&lt;T,X&gt;</code></td>
 *  		<td></code>HashSet&lt;X&gt;</code></td>
 *  		<td><code>HashSet&lt;String&gt;</code></td>
 *  	<tr>
 *  	<tr>
 *  		<td><code>HashSet&lt;E&gt;</code></td>
 *			<td></code>AbstractSet&lt;E&gt;</code></td>
 *  		<td><code>AbstracSet&lt;String&gt;</code></td>
 *  	</tr>
 *  	<tr>
 *  		<td><code>AbstractSet&lt;E&gt;</code></td>
 *			<td></code>Set&lt;E&gt;</code></td>
 *  		<td><code>Set&lt;String&gt;</code></td>
 *  	</tr>
 *  	<tr>
 *  		<td><code>Set&lt;E&gt;</code></td>
 *			<td></code>Collection&lt;E&gt;</code></td>
 *  		<td><b><code>Collection&lt;String&gt;</code></b></td>
 *  	</tr>
 *  </table>
 *  	</p>
 * This is done by the method {@link #resolveTo(Class)}. The code snippet for the given problem would be, given 
 * class
 * <pre>  public class Demo {
 *      public HashSetOfX&lt;String,Integer&gt; field;
 *  }
 *  
 *  [ ... ]
 *  
 *  Type genericType = Demo.class.getField("field").getGenericType();
 *  ResolvedGenericType type = ResolvedGenericType.resolveType(genericType);   // Initial resolving, contains HashSetOfX&lt;String,Integer&gt;
 *  ResolvedGenericType collectionType = type.resolveTo(Collection.class);  // Resolve up to collection.
 *  List&lt;Class&lt;?>> params = collectionType.getActualParameters(); // Get resolved type parameters
 * </pre>
 * @author knappmeier
 *
 */
public class ResolvedGenericType {
	/**
	 * The raw type
	 */
	private Class<?> rawType;
	/**
	 * The resolved type parameters
	 */
	private Class<?>[] actualParameters;
	
	
	/**
	 * Package private for use in test cases
	 * @param rawType
	 * @param actualParameters
	 */
	ResolvedGenericType(Class<?> rawType, Type[] actualParameters) {
		this.rawType = rawType;
		this.actualParameters = new Class<?>[actualParameters.length];
		for (int i=0;i<actualParameters.length; i++) {
			this.actualParameters[i] = (Class<?>)actualParameters[i];
		}
	}
	
	/**
	 * Returns the resolved type parameters
	 * @return
	 */
	public List<Class<?>> getActualParameters() {
		return Collections.unmodifiableList(Arrays.asList(actualParameters));
	}
	
	/**
	 * Returns the raw type
	 * @return
	 */
	public Class<?> getRawType() {
		return rawType;
	}

	
	/**
	 * Factory method: Create a new {@link ResolvedGenericType} given a {@link Type}
	 * @param type
	 * @return
	 * @throws IllegalArgumentException if the {@link Type} is neither a {@link ParameterizedType}, nor a {@link Class}
	 */
	public static ResolvedGenericType resolveType(Type type) {
		if (type instanceof ParameterizedType) {
			Type rawType = ((ParameterizedType) type).getRawType();
			Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
			return new ResolvedGenericType((Class<?>)rawType, actualTypeArguments);
		} else if (type instanceof Class) {
			return new ResolvedGenericType((Class<?>)type, new Type[0]);
		}
		throw new IllegalArgumentException("Unable to handle "+type);
	}
	
	
	/**
	 * Resolve the super class (as described in the class comment of {@link ResolvedGenericType})
	 * @return
	 * @throws IllegalArgumentException if the raw type does not have a superclass (i.e. because it is an interface)
	 */
	public ResolvedGenericType resolveSuperclass() {
		Type genericSuperclass = getRawType().getGenericSuperclass();
		if (genericSuperclass == null) throw new IllegalArgumentException("Type does not have a superclass "+this);
		return resolveSuper(genericSuperclass);
	}
	
	
	/**
	 * Resolve all interfaces (as described in the class comment of {@link ResolvedGenericType})
	 * @return
	 */
	public ResolvedGenericType[] resolveInterfaces() {
		Type[] genericInterfaces = getRawType().getGenericInterfaces();
		ResolvedGenericType[] result = new ResolvedGenericType[genericInterfaces.length]; 
		for (int i=0; i<genericInterfaces.length; i++) {
			result[i] = resolveSuper(genericInterfaces[i]);
		}
		return result;
	}
	
	
	/**
	 * Try to perform multiple recursive calls of {@link #resolveInterfaces()} and {@link #resolveSuperclass()}
	 * until reaching a {@link ResolvedGenericType} where {@link #getRawType()} equals the parameter "to".
	 * The method performs the algorithm described in the class comment.
	 * @param to the raw type of the target generic type
	 * @return a {@link ResolvedGenericType} that has "to" as raw type.
	 * @throws IllegalArgumentException if this {@link ResolvedGenericType} 
	 *    (and thus not supertype of interface) is assignable from "to". 
	 */
	public ResolvedGenericType resolveTo(Class<?> to) {
		if (getRawType().equals(to)) {
			return this;
		}
		if (!getRawType().isInterface()) {
			ResolvedGenericType superclass = resolveSuperclass();
			if (to.isAssignableFrom(superclass.getRawType())) {
				return superclass.resolveTo(to);
			}
		}
		for (ResolvedGenericType iface : resolveInterfaces()) {
			if (to.isAssignableFrom(iface.getRawType())) {
				return iface.resolveTo(to);
			}
		}
		throw new IllegalArgumentException("No supertype or interface is assignable to "+to);
	}
	
	
	/**
	 * Helper method for {@link #resolveInterfaces()} and {@link #resolveSuperclass()}
	 * @param genericSuperType
	 * @return
	 */
	private ResolvedGenericType resolveSuper(Type genericSuperType) {
		if (genericSuperType instanceof Class) {
			return new ResolvedGenericType((Class<?>)genericSuperType, new Class<?>[0]);
		} else if (genericSuperType instanceof ParameterizedType) {
			TypeVariable<?>[] typeParameters = getRawType().getTypeParameters();
			Type[] superTypeArguments = ((ParameterizedType) genericSuperType).getActualTypeArguments();
			for (int i=0; i<superTypeArguments.length; i++) {
				for (int j=0; j<typeParameters.length; j++) {
					if (superTypeArguments[i]==typeParameters[j]) {
						superTypeArguments[i]=this.actualParameters[j];
					}
				}
			}
			return new ResolvedGenericType((Class<?>) ((ParameterizedType) genericSuperType).getRawType(), superTypeArguments);
		}
		throw new IllegalArgumentException("Unable to handle type "+this);
	}
	
	
	@Override
	public String toString() {
		return "ResolvedType [rawType=" + rawType + ", actualParameters="
				+ Arrays.toString(actualParameters) + "]";
	}

	/**
	 * Implemented to compute hash code from {@link #rawType} and {@link #actualParameters}.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(actualParameters);
		result = prime * result
				+ ((rawType == null) ? 0 : rawType.hashCode());
		return result;
	}

	/**
	 * Implemented to compare {@link #rawType} and {@link #actualParameters}.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResolvedGenericType other = (ResolvedGenericType) obj;
		if (!Arrays.equals(actualParameters, other.actualParameters))
			return false;
		if (rawType == null) {
			if (other.rawType != null)
				return false;
		} else if (!rawType.equals(other.rawType))
			return false;
		return true;
	}
}