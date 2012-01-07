package org.knappi.opensource.genericreflection;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;



public class ResolvedGenericTypeTest {

	public HashSetOfX<Integer, String> test;
	
	public List<String> testList;
		

	@Test
	public void testResolveType() throws Exception {
		Type type = ResolvedGenericTypeTest.class.getField("test").getGenericType();
		ResolvedGenericType resolvedType = ResolvedGenericType.resolveType(type);
		Assert.assertEquals(HashSetOfX.class,resolvedType.getRawType());
		Assert.assertEquals(Integer.class,resolvedType.getActualParameters().get(0));
		Assert.assertEquals(String.class,resolvedType.getActualParameters().get(1));
	}

	
	@Test
	public void testResolveSuperclass() throws Exception {
		Type type = ResolvedGenericTypeTest.class.getField("test").getGenericType();
		ResolvedGenericType resolvedType = ResolvedGenericType.resolveType(type);
		ResolvedGenericType resolvedSuperclass = resolvedType.resolveSuperclass();
		Assert.assertEquals(new ResolvedGenericType(HashSet.class, new Class<?>[] { String.class }),resolvedSuperclass);
	}

	@Test
	public void testResolveInterfaces() throws Exception {
		Type type = ResolvedGenericTypeTest.class.getField("test").getGenericType();
		ResolvedGenericType resolvedType = ResolvedGenericType.resolveType(type);
		Assert.assertEquals(HashSetOfX.class,resolvedType.getRawType());
		Assert.assertEquals(Integer.class,resolvedType.getActualParameters().get(0));
		Assert.assertEquals(String.class,resolvedType.getActualParameters().get(1));
		ResolvedGenericType[] resolvedInterfaces = resolvedType.resolveInterfaces();
		Assert.assertEquals(new ResolvedGenericType(InterfaceT.class, new Class<?>[] { Integer.class}),resolvedInterfaces[0]);
		Assert.assertEquals(new ResolvedGenericType(InterfaceX.class, new Class<?>[] { String.class}),resolvedInterfaces[1]);
	}

	
	@Test
	public void testResolveTo() throws Exception {
		Type type = ResolvedGenericTypeTest.class.getField("test").getGenericType();
		ResolvedGenericType resolvedType = ResolvedGenericType.resolveType(type);
		ResolvedGenericType collectionType = resolvedType.resolveTo(Collection.class);
		Assert.assertEquals(Collection.class, collectionType.getRawType());
		Assert.assertEquals(String.class, collectionType.getActualParameters().get(0));
		Assert.assertEquals(1,collectionType.getActualParameters().size());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testResolveSuperClassOfInterface() throws Exception {
		Type type = ResolvedGenericTypeTest.class.getField("testList").getGenericType();
		ResolvedGenericType resolvedType = ResolvedGenericType.resolveType(type);
		resolvedType.resolveSuperclass();
	}
	
	
	@Test
	public void testResolveToWithInterface() throws Exception {
		Type type = ResolvedGenericTypeTest.class.getField("testList").getGenericType();
		ResolvedGenericType resolvedType = ResolvedGenericType.resolveType(type);
		ResolvedGenericType collectionType = resolvedType.resolveTo(Collection.class);
		Assert.assertEquals(Collection.class, collectionType.getRawType());
		Assert.assertEquals(String.class, collectionType.getActualParameters().get(0));
		Assert.assertEquals(1,collectionType.getActualParameters().size());
	}
	
}
