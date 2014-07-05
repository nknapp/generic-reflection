generic-reflection
==================

While generic type information (type parameters as in `List<String>`)  is usually removed from java byte-code, there are some situations where it is still available.
When retrieving a field-type, method-return-type or method-parameter-type via reflection 
(e.g. by calling [getGenericParameterTypes](http://docs.oracle.com/javase/7/docs/api/java/lang/reflect/Method.html#getGenericParameterTypes()),
a Type-object is available which contains information about the type parameter.

This library currently contains only one class [ResolvedGenericType](apidocs/org/knappi/opensource/genericreflection/ResolvedGenericType.html). 
This class can be used to answer the following question:

Given a class <code>Child</code>&lt;X,Y>:  
 
1. For a given X and Y what is the direct superclass of `Child<X,Y>` (including generic parameters)?
2. For a given X and Y what are the direct interfaces implemented by `Child<X,Y>` (including generic parameters)?
3. Given `Parent<T>` a transitive generic superclass or generic interface of `Child`, what is T for `Child`?  
4. Is `Child<String,Integer>` assignable to `Parent<Integer>`?

The class only works in the simple case where type parameters are themselves concrete classes. Generics within type parameters 
and parameters that are resolved from elsewhere won't work at the moment. Examples can be found in the test case 
`org.knappi.opensource.genericreflection.ResolvedGenericTypeTest`.
