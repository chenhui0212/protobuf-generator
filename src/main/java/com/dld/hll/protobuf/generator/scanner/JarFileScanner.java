package com.dld.hll.protobuf.generator.scanner;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

    public JarFileScanner(String jarFile, boolean isNeedLoad) throws MalformedURLException {
        this.jarFile = jarFile;
        if (isNeedLoad) {
            jcl = new URLClassLoader(new URL[]{new File(jarFile).toURI().toURL()},
                    this.getClass().getClassLoader());
        }
    }

    @Override
    public List<Class<?>> scanServices() throws IOException, ClassNotFoundException {
        return scanServices(null);
    }

    public List<Class<?>> scanServices(String scanPackage) throws IOException, ClassNotFoundException {
        ArrayList<Class<?>> classes = new ArrayList<>();
        JarInputStream inputStream = new JarInputStream(new FileInputStream(jarFile));

        JarEntry entry;
        while ((entry = inputStream.getNextJarEntry()) != null) {
            if (entry.getName().endsWith(".class") && !entry.getName().contains("$")) {
                String className = entry.getName().replace(File.separator, ".");
                if (scanPackage != null && !className.startsWith(scanPackage)) {
                    continue;
                }

                // 删除后缀名 (.class)
                Class<?> clazz = Class.forName(className.substring(0, className.length() - 6), true, jcl);
                if (clazz.isInterface() && isAcceptable(clazz)) {
                    classes.add(clazz);
                }
            }
        }
        return classes;
    }
}
