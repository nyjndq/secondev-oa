package com.weaver.ecode.gradle.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.Jar;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BuildArchPlugin implements Plugin<Project> {

    private void readResourcePathList(String rootPath, Set<String> pathList) {
        File dir = new File(rootPath);
        if (Objects.isNull(dir.listFiles())) return;
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                pathList.add(file.getPath());
                readResourcePathList(file.getPath(), pathList);
            } else {
                pathList.add(file.getPath());
            }
        }
    }

    private void addResourceListToJar(String jarLibPath, Set<String> resPathList, String fileName) {
        StringBuilder text = new StringBuilder("[\n");
        for (String s : resPathList) {
            text.append("\t\"").append(s).append("\"\n");
        }
        text.append("]");
        File zipFile = new File(jarLibPath);
        String tmpFilePath = UUID.randomUUID() + ".zip";
        File tmpZipFile = new File(zipFile.getParentFile(), tmpFilePath);
        try(ZipInputStream zin = new ZipInputStream(Files.newInputStream(zipFile.toPath()));
            ZipOutputStream tmpZipOut = new ZipOutputStream(Files.newOutputStream(tmpZipFile.toPath()));
            InputStream fis = new ByteArrayInputStream(text.toString().getBytes(StandardCharsets.UTF_8));) {
            ZipEntry entry = zin.getNextEntry();
            while (entry != null) {
                tmpZipOut.putNextEntry(new ZipEntry(entry.getName()));
                byte[] buffer = new byte[1024];
                int len;
                while ((len = zin.read(buffer)) > 0) {
                    tmpZipOut.write(buffer, 0, len);
                }
                entry = zin.getNextEntry();
            }
            ZipEntry zipEntry = new ZipEntry("META-INF/" + fileName);
            tmpZipOut.putNextEntry(zipEntry);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                tmpZipOut.write(buffer, 0, len);
            }
            zipFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        tmpZipFile.renameTo(zipFile);
    }

    private Set<String> calcAbsPathList(String rooPath, Set<String> pathList) {
        Set<String> absPaths = new HashSet<>();
        for (String path : pathList) {
            Path basePath = Paths.get(rooPath).toAbsolutePath().normalize();
            Path filePath = new File(path).toPath();
            Path relativize = basePath.relativize(filePath);
            System.out.println("echo path: " + relativize);
            absPaths.add(relativize.toString().replace("\\", "/"));
        }
        return absPaths;
    }

    @Override
    public void apply(Project project) {
        // 扫描 项目下的resources 文件内容
        List<String> pathList = new ArrayList<>();
//        scanFileOrDir()
        System.out.println(project.getRootDir().toPath());
        project.getTasks().forEach(t -> {
            if (t.getName().equalsIgnoreCase("jar")) { // 打包task添加后置处理逻辑
                t.doLast(l -> {
                    String jarName = ((Jar) l).getArchiveBaseName().get();
                    String version = null;
                    try {
                        version = ((Jar) l).getArchiveVersion().get();
                    } catch (Exception ignored) {}
                    String libJarPath = project.getBuildDir().getPath() + "/libs/" + jarName + (Objects.nonNull(version) ? "-" + version : "")  + ".jar";
                    File jarFile = new File(libJarPath);
                    if (jarFile.exists()) {
                        System.out.println("jar file size: " + jarFile.length() + ", libJarPath: " + libJarPath);
                        // 当前目录下生成build.zip
                        // 获取对应模块的resources 目录清单
                        File moduleProjectFile = l.getProject().getProjectDir();
                        Set<String> resourcesList = new HashSet<>();
                        Set<String> srcList = new HashSet<>();
                        readResourcePathList(moduleProjectFile.getPath() + "/src/main/resources", resourcesList);
                        readResourcePathList(moduleProjectFile.getPath() + "/src/main/java", srcList);
                        Set<String> resAbsPath = calcAbsPathList(moduleProjectFile.getPath() + "/src/main/resources", resourcesList);
                        Set<String> srcAbsPath = calcAbsPathList(moduleProjectFile.getPath() + "/src/main/java", srcList);
                        addResourceListToJar(libJarPath, resAbsPath, "res.list");
                        addResourceListToJar(libJarPath, srcAbsPath, "src.list");
                        createBuildZip(libJarPath);
                    } else {
                        System.out.println("archive jar not existed. (path: " + libJarPath + ")");
                    }
                });
            }
        });
    }

    private void createBuildZip(String jarPath) {
        try {
            File jarFile = new File(jarPath);
            String zipFileName = jarFile.getParent() + "/build.zip";
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFileName));
            addFileToZip(new File(jarPath), "", zipOutputStream);
            zipOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addFileToZip(File file, String parentFolder, ZipOutputStream zipOutputStream) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(parentFolder + "/" + file.getName());
        zipOutputStream.putNextEntry(zipEntry);

        byte[] buffer = new byte[1024];
        int len;
        while ((len = fileInputStream.read(buffer)) > 0) {
            zipOutputStream.write(buffer, 0, len);
        }

        fileInputStream.close();
    }

}
