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
    private Path scanPath;
    private int PrePathLength;
    private static final String DEFAULT_PROJECT_BASE_PATH = "src/main/java";

    public ProjectScanner(Path projectPath) {
        this(projectPath, DEFAULT_PROJECT_BASE_PATH);
    }

    public ProjectScanner(Path projectPath, String projectBasePath) {
        scanPath = projectPath.resolve(projectBasePath);
        PrePathLength = scanPath.toFile().getAbsolutePath().length();
        if (!Files.exists(scanPath)) {
            throw new RuntimeException("The scan path [" + scanPath + "] doesn't exists!");
        }
    }

    public List<Class<?>> scanServices() throws ClassNotFoundException {
        return scanServices(null);
    }

    public List<Class<?>> scanServices(String scanPackage) throws ClassNotFoundException {
        if (scanPackage != null) {
            scanPath = scanPath.resolve(scanPackage);
            if (!Files.exists(scanPath)) {
                throw new RuntimeException("The scan package [" + scanPackage + "] doesn't exists!");
            }
        }

        ArrayList<Class<?>> classes = new ArrayList<>();
        scanRecursively(classes, scanPath.toFile());
        return classes;
    }

    /**
     * 递归的方式将全部查询的文件放入到指定集合中
     *
     * @param classes  存放查询结果的集合
     * @param scanPath 搜索目录
     */
    private void scanRecursively(List<Class<?>> classes, File scanPath) throws ClassNotFoundException {
        File[] subFiles = scanPath.listFiles(pathname ->
                (pathname.isDirectory() || (pathname.getName().endsWith(".java") &&
                        !pathname.getName().equals("package-info.java"))));
        if (subFiles == null) {
            return;
        }

        for (File file : subFiles) {
            if (file.isDirectory()) {
                scanRecursively(classes, file.getAbsoluteFile());
            } else {
                String className = file.getAbsolutePath().substring(PrePathLength + 1)
                        .replace(File.separator, ".");

                // 删除后缀名 (.java)
                Class<?> clazz = Class.forName(className.substring(0, className.length() - 5));
                if (clazz.isInterface() && isAcceptable(clazz)) {
                    classes.add(clazz);
                }
            }
        }
    }
}
