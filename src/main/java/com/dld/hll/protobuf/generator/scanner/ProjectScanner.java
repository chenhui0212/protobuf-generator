package com.dld.hll.protobuf.generator.scanner;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 扫描项目或指定目录下所有的服务接口
 *
 * @author Chen Hui
 */
public class ProjectScanner extends SelectableScanner {

    /**
     * 项目路径 + 类似 Maven Project directory layout (src/main/java) + scanPackage
     */
    private Path scanPath;

    /**
     * 项目路径 + 类似 Maven Project directory(src/main/java) 长度
     */
    private int projectJavaPathLength;


    public ProjectScanner(Path projectJavaPath, String scanPackage) {
        this.projectJavaPathLength = projectJavaPath.toFile().getAbsolutePath().length();
        this.scanPath = projectJavaPath.resolve(scanPackage);
        if (!Files.exists(this.scanPath)) {
            throw new RuntimeException("The scan path [" + this.scanPath + "] doesn't exists!");
        }
    }

    @Override
    public List<Class<?>> scanServices() {
        ArrayList<Class<?>> classes = new ArrayList<>();
        scanRecursively(scanPath.toFile(), classes);
        return classes;
    }

    /**
     * 递归的方式将全部满足条件的Class放入到指定集合中
     *
     * @param scanPath 搜索目录
     * @param classes  存放查询结果的集合
     */
    private void scanRecursively(File scanPath, List<Class<?>> classes) {
        File[] subFiles = getSubFiles(scanPath);
        if (subFiles == null) {
            return;
        }

        for (File file : subFiles) {
            if (file.isDirectory()) {
                scanRecursively(file, classes);
            } else {
                Class<?> serviceClass = getServiceClassIfMeet(file);
                if (serviceClass != null) {
                    classes.add(serviceClass);
                }
            }
        }
    }

    private File[] getSubFiles(File scanPath) {
        return scanPath.listFiles(pathname -> (pathname.isDirectory() ||
                (pathname.getName().endsWith(".java") && !pathname.getName().equals("package-info.java"))));
    }

    private Class<?> getServiceClassIfMeet(File file) {
        Class<?> serviceClass = getClass(file);
        if (serviceClass.isInterface() && isAcceptable(serviceClass)) {
            return serviceClass;
        }
        return null;
    }

    private Class<?> getClass(File file) {
        String className = file.getAbsolutePath().substring(projectJavaPathLength + 1)
                .replaceAll(".java$", "").replace(File.separator, ".");
        try {
            return Class.forName(className);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
