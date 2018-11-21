package com.dld.hll.protobuf.generator;

import com.dld.hll.protobuf.generator.scanner.JarFileScanner;
import com.dld.hll.protobuf.generator.scanner.ProjectScanner;
import com.dld.hll.protobuf.generator.scanner.SelectableScanner;
import com.dld.hll.protobuf.generator.selector.ExtendsInterfaceSelector;
import com.dld.hll.protobuf.generator.selector.NamePatternSelector;
import com.dld.hll.protobuf.generator.util.AssertUtils;
import com.dld.hll.protobuf.generator.util.StringUtils;
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

    public void executor() {
        ProtoInfoRegistry registry = new ProtoInfoRegistry();
        ProtoInfoReader reader = new ProtoInfoReader();
        reader.setRegistry(registry);

        // 获取当前项目路径
        Path projectPath;
        if (builder.getProjectPath() != null) {
            projectPath = builder.getProjectPath();
        } else {
            projectPath = getProjectPath(builder.getProjectName());
        }

        // 扫描器
        SelectableScanner scanner;
        if (builder.getJarFile() != null) {
            scanner = new JarFileScanner(builder.getJarFile(), builder.isNeedLoadJarFile());
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
        reader.load(scanner.scanServices(builder.getScanPackage()));

        // Proto文件生成器
        ProtoFileGenerator generator = new ProtoFileGenerator();
        generator.setRegistry(registry);
        generator.setCommonProtoFileName(getCommonProtoFileName(builder.getProjectName()));

        // 指定注释注解，及获取注释对应的方法
        if (builder.getCommentClass() != null) {
            generator.assignCommon(builder.getCommentClass(), builder.getCommentMethodName());
        }

        // 生成Proto文件
        if (builder.getGeneratePath() != null) {
            generator.generate(new File(builder.getGeneratePath()));
        } else {
            generator.generate(projectPath.resolve(builder.getGenerateBasePath()).toFile());
        }
    }

    /**
     * 获取项目所在路径
     */
    private Path getProjectPath(String projectName) {

        /*
         * 获取根项目绝对路径
         */
        String userDir = System.getProperty("user.dir");

        /*
         * 获取指定项目名项目路径
         * 由于 userDir 仅为根项目的路径，如果指定项目为子模块时，需要追加项目名
         */
        Path projectPath;
        if (userDir.endsWith(projectName)) {
            projectPath = Paths.get(userDir);
        } else {
            projectPath = Paths.get(userDir, projectName);

            // 确认目录是否存在
            if (!Files.exists(projectPath)) {
                throw new RuntimeException("Path [" + projectPath + "] doesn't exists!");
            }
        }
        return projectPath;
    }

    /**
     * 获取生成的common proto文件名
     */
    private String getCommonProtoFileName(String projectName) {
        // 拼装文件名
        StringBuilder commonFileName = new StringBuilder();
        for (String str : projectName.split("[-_]")) {
            commonFileName.append(StringUtils.capitalize(str));
        }
        commonFileName.append("Common");
        return commonFileName.toString();
    }

    @Getter
    public static final class Builder {

        /**
         * 必传
         * 当项目为子模块的子模块时，需要传除根项目的全部子模块名
         */
        private String projectName;

        /**
         * 指定要生成Proto文件的Jar包全路径
         * 当指定当前参数，将忽略 projectBasePath
         */
        private String jarFile;

        /**
         * 是否需要加载Jar文件
         * 如果外部完成环境加载，可以不用再次加载
         */
        private boolean isNeedLoadJarFile = true;

        /**
         * 指定项目路径
         */
        private Path projectPath;

        /**
         * 指定项目基础路径
         * 默认为 Maven 路径（src/main/java）
         */
        private String projectBasePath;

        /**
         * 扫描Jar包或者项目基础路径内指定路径
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

        /**
         * 生成Proto文件的相对项目的路径
         */
        private String generateBasePath = "src/main/proto";


        public Builder setProjectName(String projectName) {
            AssertUtils.hasText(projectName);
            this.projectName = projectName;
            return this;
        }

        public Builder setProjectPath(String projectPath) {
            AssertUtils.hasText(projectPath);
            this.projectPath = Paths.get(projectPath);
            if (!Files.exists(this.projectPath)) {
                throw new RuntimeException("Path [" + projectPath + "] doesn't exists!");
            }
            return this;
        }

        public Builder setJarFile(String jarFile) {
            AssertUtils.hasText(jarFile);
            this.jarFile = jarFile;
            return this;
        }

        public Builder setNeedLoadJarFile(boolean isNeedLoadJarFile) {
            this.isNeedLoadJarFile = isNeedLoadJarFile;
            return this;
        }

        public Builder setProjectBasePath(String projectBasePath) {
            AssertUtils.hasText(projectBasePath);
            this.projectBasePath = projectBasePath;
            return this;
        }

        public Builder setScanPackage(String scanPackage) {
            AssertUtils.hasText(scanPackage);
            this.scanPackage = scanPackage;
            return this;
        }

        public Builder setExtendsInterface(Class<?> extendsInterface) {
            AssertUtils.notNull(extendsInterface);
            this.extendsInterface = extendsInterface;
            return this;
        }

        public Builder setNamePattern(String namePattern) {
            AssertUtils.hasText(namePattern);
            this.namePattern = namePattern;
            return this;
        }

        public Builder setComment(Class<? extends Annotation> commentClass, String commentMethodName) {
            AssertUtils.notNull(commentClass);
            AssertUtils.hasText(commentMethodName);
            this.commentClass = commentClass;
            this.commentMethodName = commentMethodName;
            return this;
        }

        public Builder setGeneratePath(String generatePath) {
            AssertUtils.hasText(generatePath);
            this.generatePath = generatePath;
            return this;
        }

        public Builder setGenerateBasePath(String generateBasePath) {
            AssertUtils.hasText(generateBasePath);
            this.generateBasePath = generateBasePath;
            return this;
        }

        public ProtoExecutor build() {
            if (projectName == null) {
                throw new RuntimeException("Must pass parameter [projectName]");
            }

            ProtoExecutor executor = new ProtoExecutor();
            executor.setBuilder(this);
            return executor;
        }
    }
}
