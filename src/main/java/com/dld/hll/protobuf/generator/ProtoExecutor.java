package com.dld.hll.protobuf.generator;

import com.dld.hll.protobuf.generator.scanner.JarFileScanner;
import com.dld.hll.protobuf.generator.scanner.ProjectScanner;
import com.dld.hll.protobuf.generator.scanner.SelectableScanner;
import com.dld.hll.protobuf.generator.selector.ExtendsInterfaceSelector;
import com.dld.hll.protobuf.generator.selector.NamePatternSelector;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Chen Hui
 */
@Setter
public class ProtoExecutor {

    private Builder builder;

    public static Builder newBuilder() {
        return new Builder();
    }

    public void executor() throws Exception {
        ProtoInfoRegistry registry = new ProtoInfoRegistry();
        ProtoInfoReader reader = new ProtoInfoReader();
        reader.setRegistry(registry);

        // 获取当前项目路径
        Path projectPath = getProjectPath(builder.getProjectName());

        // 扫描器
        SelectableScanner scanner;
        if (builder.getJarFile() != null) {
            scanner = new JarFileScanner(builder.getJarFile());
        } else {
            if (builder.getProjectBasePath() != null) {
                scanner = new ProjectScanner(projectPath, builder.getProjectBasePath());
            } else {
                scanner = new ProjectScanner(projectPath);
            }
        }

        // 选择器
        if (builder.getExtendsInterface() != null) {
            scanner.addSelector(new ExtendsInterfaceSelector(builder.getExtendsInterface()));
        }
        if (builder.getNamePattern() != null) {
            scanner.addSelector(new NamePatternSelector(builder.getNamePattern()));
        }
        // 没有选择器的情况下添加默认选择器
        if (scanner.getSelectors() == null) {
            scanner.addSelector(new NamePatternSelector(".*Service$"));
        }

        // 加载全部的接口
        if (builder.getScanPackage() != null) {
            reader.load(scanner.scanServices(builder.getScanPackage()));
        } else {
            reader.load(scanner.scanServices());
        }

        // Proto文件生成器
        ProtoFileGenerator generator = new ProtoFileGenerator();
        generator.setRegistry(registry);

        // 指定注释注解，及获取注释对应的方法
        if (builder.getCommentClass() != null) {
            generator.assignCommon(builder.getCommentClass(), builder.getCommentMethodName());
        }

        // 生成Proto文件
        if (builder.getGeneratePath() != null) {
            generator.generate(new File(builder.getGeneratePath()));
        } else {
            generator.generate(projectPath.resolve("src/main/proto").toFile());
        }
    }

    /**
     * 获取项目所在路径
     */
    private Path getProjectPath(String projectName) {
        /*
         * 获取根项目绝对路径
         * 当项目为子模块，或者子子模块时，获取的路径都仅为根项目路径
         */
        String userDir = System.getProperty("user.dir");

        /*
         * 获取扫描路径
         * 由于 userDir 仅为根项目的路径，如果指定项目为子模块时，需要追加项目名
         */
        Path projectPath;
        if (projectName == null || projectName.equals("")) {
            projectPath = Paths.get(userDir);
        } else {
            if (userDir.endsWith(projectName)) {
                projectPath = Paths.get(userDir);
            } else {
                projectPath = Paths.get(userDir, projectName);

                // 确认目录是否存在
                if (!Files.exists(projectPath)) {
                    throw new RuntimeException("Path [" + projectPath + "] doesn't exists!");
                }
            }
        }
        return projectPath;
    }

    @Getter
    public static final class Builder {
        /**
         * 当项目基础路径未传递时，通过该属性获取
         * 当项目为子模块时，System.getProperty("user.dir") 只能获取根项目的路径
         * 当项目为子模块的子模块时，需要传除根项目的全部子模块名
         */
        private String projectName;

        /**
         * 指定要生成Proto文件的Jar包全路径
         * 当指定当前参数，将忽略 projectBasePath
         */
        private String jarFile;

        /**
         * 指定项目基础路径
         * 默认为 Maven 路径（src/main/java）
         */
        private String projectBasePath;

        /**
         * 扫描Jar包内指定路径
         */
        private String scanPackage;

        /**
         * 类满足继承指定接口
         */
        private Class<?> extendsInterface;

        /**
         * 类名满足指定模式
         */
        private String namePattern;

        /**
         * 解析接口时，注释注解的类
         * 获取注释值的方法名
         */
        private Class<? extends Annotation> commentClass;
        private String commentMethodName;

        /**
         * 生成Proto文件的全路径
         */
        private String generatePath;

        public Builder setProjectName(String projectName) {
            AssertUtil.hasText(projectName);
            this.projectName = projectName;
            return this;
        }

        public Builder setJarFile(String jarFile) {
            AssertUtil.hasText(jarFile);
            this.jarFile = jarFile;
            return this;
        }

        public Builder setProjectBasePath(String projectBasePath) {
            AssertUtil.hasText(projectBasePath);
            this.projectBasePath = projectBasePath;
            return this;
        }

        public Builder setScanPackage(String scanPackage) {
            AssertUtil.hasText(scanPackage);
            this.scanPackage = scanPackage;
            return this;
        }

        public Builder setExtendsInterface(Class<?> extendsInterface) {
            AssertUtil.notNull(extendsInterface);
            this.extendsInterface = extendsInterface;
            return this;
        }

        public Builder setNamePattern(String namePattern) {
            AssertUtil.hasText(namePattern);
            this.namePattern = namePattern;
            return this;
        }

        public Builder setComment(Class<? extends Annotation> commentClass, String commentMethodName) {
            AssertUtil.notNull(commentClass);
            AssertUtil.hasText(commentMethodName);
            this.commentClass = commentClass;
            this.commentMethodName = commentMethodName;
            return this;
        }

        public Builder setGeneratePath(String generatePath) {
            AssertUtil.hasText(generatePath);
            this.generatePath = generatePath;
            return this;
        }

        public ProtoExecutor build() {
            ProtoExecutor executor = new ProtoExecutor();
            executor.setBuilder(this);
            return executor;
        }
    }
}