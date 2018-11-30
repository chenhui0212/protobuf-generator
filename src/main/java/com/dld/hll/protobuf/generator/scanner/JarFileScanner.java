package com.dld.hll.protobuf.generator.scanner;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * 扫描Jar包中所有的服务接口
 *
 * @author Chen Hui
 */
@Getter
@Setter
public class JarFileScanner extends SelectableScanner {

    private String jarFile;
    private URLClassLoader jcl;
    private String scanPackage;


    public JarFileScanner(String jarFile, String scanPackage, boolean isNeedLoad) {
        this.jarFile = jarFile;
        this.scanPackage = scanPackage;
        if (isNeedLoad) {
            try {
                jcl = new URLClassLoader(new URL[]{new File(jarFile).toURI().toURL()},
                        this.getClass().getClassLoader());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public List<Class<?>> scanServices() {
        ArrayList<Class<?>> classes = new ArrayList<>();
        try (JarInputStream inputStream = new JarInputStream(new FileInputStream(jarFile))) {
            JarEntry entry;
            while ((entry = inputStream.getNextJarEntry()) != null) {
                Class<?> serviceClass = getServiceClassIfMeet(entry);
                if (serviceClass != null) {
                    classes.add(serviceClass);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return classes;
    }

    private Class<?> getServiceClassIfMeet(JarEntry entry) {
        if (isNameAndPackageMeet(entry)) {
            Class<?> serviceClass = getClass(entry);
            if (serviceClass.isInterface() && isAcceptable(serviceClass)) {
                return serviceClass;
            }
        }
        return null;
    }

    private boolean isNameAndPackageMeet(JarEntry entry) {
        if (!entry.getName().endsWith(".class") || entry.getName().contains("$")) {
            return false;
        }
        return scanPackage == null || entry.getName().startsWith(scanPackage);
    }

    private Class<?> getClass(JarEntry entry) {
        String className = entry.getName().replaceAll(".class$", "").replace("/", ".");
        try {
            return jcl == null ? Class.forName(className) : Class.forName(className, true, jcl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
