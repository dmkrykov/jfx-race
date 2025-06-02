package org.eclipse.jdt.internal.jarinjarloader;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

public class OneJarURLClassLoader extends URLClassLoader {
	private Hashtable<String, Class<?>> classes = new Hashtable<>();

	public OneJarURLClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return findClass(name);
	}

	@Override
	public synchronized Class<?> findClass(String name) throws ClassNotFoundException {
		Class<?> clazz = classes.get(name);
		if (clazz == null) {
			try {
				clazz = findClassInJars(name);
			} catch (Exception e) {
				throw new ClassNotFoundException(name, e);
			}
			if (clazz == null) throw new ClassNotFoundException(name);
			classes.put(name, clazz);
		}
		return clazz;
	}

	private Class<?> findClassInJars(String name) throws Exception {
		String path = name.replace('.', '/').concat(".class");
		Enumeration<URL> e = getResources(path);
		if (e.hasMoreElements()) {
			URL url = e.nextElement();
			InputStream is = url.openStream();
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int read;
			byte[] data = new byte[8192];
			while ((read = is.read(data)) > -1) {
				buffer.write(data, 0, read);
			}
			is.close();
			byte[] raw = buffer.toByteArray();
			return defineClass(name, raw, 0, raw.length);
		}
		return null;
	}
}