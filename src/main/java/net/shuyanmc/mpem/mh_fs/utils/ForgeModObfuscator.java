package net.shuyanmc.mpem.mh_fs.utils;

import com.google.gson.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

public class ForgeModObfuscator {
    private static final Set<String> EXCLUDED_PACKAGES = new HashSet<>(Arrays.asList(
            "net/minecraft/",
            "net/minecraftforge/",
            "org/spongepowered/asm/",
            "cpw/mods/",
            "java/lang/",
            "java/io/",
            "java/nio/",
            "java/util/",
            "java/nio/",
            "com/google/",
            "org/objectweb/",
            "com/ibm/icu",
            "com/sun",
            "com/mojang",
            "com/electronwill/nightconfig",
            "javax/",
            "org/apache",
            "org/lwjgl",
            "org/slf4j",
            "org/jline",
            "org/openjdk"

    ));

    //private final Map<String, String> packageMapping = new HashMap<>();
    //private final Map<String, String> classMapping = new HashMap<>();
    private final Map<String, String> classMappings = new HashMap<>();
    private final Map<String, String> packageMappings = new HashMap<>();
    private final Random random = new Random();
    private final Map<String, String> nameMappings = new HashMap<>();
    private final String prefix;
    private String modMainClass;
    private final Set<String> protectedPackages = new HashSet<>();

    public ForgeModObfuscator(String prefix) {
        this.prefix = prefix;
    }


    public void obfuscate(Path inputJar, Path outputJar) throws IOException {
        // 第一阶段：建立全局命名映射
        buildNameMappings(inputJar);

        // 第二阶段：应用混淆
        applyObfuscation(inputJar, outputJar);
    }

    private void buildNameMappings(Path jarPath) throws IOException {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.endsWith(".class")) {
                    String className = entryName.substring(0, entryName.length() - 6);
                    if (!shouldExclude(className)) {
                        // 为每个类生成唯一的混淆名称
                        String obfName = generateObfuscatedName(className);
                        nameMappings.put(className, obfName);
                    }
                }
            }
        }
    }

    private String generateObfuscatedName(String originalName) {
        // 保持包结构的同时混淆
        String[] parts = originalName.split("/");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) result.append("/");

            if (i < parts.length - 1) {
                // 混淆包名部分
                String pkgKey = String.join("/", Arrays.copyOfRange(parts, 0, i + 1));
                result.append(prefix).append("pkg").append(pkgKey.hashCode() & 0xffff);
            } else {
                // 混淆类名部分
                result.append(prefix).append("cls").append(originalName.hashCode() & 0xffff);
            }
        }

        return result.toString();
    }

    private void applyObfuscation(Path inputJar, Path outputJar) throws IOException {
        Files.createDirectories(outputJar.getParent());

        try (JarInputStream jis = new JarInputStream(Files.newInputStream(inputJar));
             JarOutputStream jos = new JarOutputStream(Files.newOutputStream(outputJar))) {

            JarEntry entry;
            while ((entry = jis.getNextJarEntry()) != null) {
                byte[] bytes = readAllBytes(jis);
                String entryName = entry.getName();
                String newName = entryName;

                if (entryName.endsWith(".class")) {
                    String className = entryName.substring(0, entryName.length() - 6);
                    if (!shouldExclude(className)) {
                        // 使用预先生成的映射
                        String obfName = nameMappings.get(className);
                        bytes = obfuscateClass(bytes, className, obfName);
                        newName = obfName + ".class";
                    }
                } else if (entryName.endsWith(".json") && entryName.contains("mixins")) {
                    bytes = updateMixinConfig(new String(bytes)).getBytes();
                }

                jos.putNextEntry(new JarEntry(newName));
                jos.write(bytes);
                jos.closeEntry();
            }
        }
    }

    private byte[] obfuscateClass(byte[] original, String originalName, String obfuscatedName) {
        ClassReader cr = new ClassReader(original);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);

        // 使用确保一致的重映射器
        ClassVisitor cv = new ClassRemapper(cw, new ConsistentRemapper(originalName, obfuscatedName));

        try {
            cr.accept(cv, ClassReader.EXPAND_FRAMES);
            return cw.toByteArray();
        } catch (Exception e) {
            System.err.println("Error obfuscating class: " + originalName);
            e.printStackTrace();
            return original;
        }
    }

    private String updateMixinConfig(String config) {
        try {
            JsonObject json = JsonParser.parseString(config).getAsJsonObject();

            if (json.has("package")) {
                String originalPkg = json.get("package").getAsString();
                String obfPkg = mapDotName(originalPkg);
                json.addProperty("package", obfPkg);
            }

            if (json.has("mixins")) {
                JsonArray mixins = json.getAsJsonArray("mixins");
                for (int i = 0; i < mixins.size(); i++) {
                    String original = mixins.get(i).getAsString();
                    String obf = mapDotName(original);
                    mixins.set(i, new JsonPrimitive(obf));
                }
            }

            return new GsonBuilder().setPrettyPrinting().create().toJson(json);
        } catch (Exception e) {
            System.err.println("Error updating mixin config: " + e.getMessage());
            return config;
        }
    }

    private String mapDotName(String dotName) {
        String internalName = dotName.replace('.', '/');
        return nameMappings.getOrDefault(internalName, internalName).replace('/', '.');
    }

    private boolean shouldExclude(String internalName) {
        return EXCLUDED_PACKAGES.stream().anyMatch(internalName::startsWith);
    }

    private byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[16384];
        int nRead;
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    private class ConsistentRemapper extends Remapper {
        private final String originalName;
        private final String obfuscatedName;

        public ConsistentRemapper(String originalName, String obfuscatedName) {
            this.originalName = originalName;
            this.obfuscatedName = obfuscatedName;
        }

        @Override
        public String map(String internalName) {
            if (shouldExclude(internalName)) {
                return internalName;
            }

            // 当前类特殊处理
            if (internalName.equals(originalName)) {
                return obfuscatedName;
            }

            // 其他类使用全局映射
            return nameMappings.getOrDefault(internalName, internalName);
        }
    }

    public static void encode(Path file,Path outPath) {
        ForgeModObfuscator obfuscator = new ForgeModObfuscator("obf_");
        try {
            obfuscator.obfuscate(file, outPath);
            System.out.println("Obfuscation completed successfully!");
        } catch (IOException e) {
            System.err.println("Error during obfuscation:");
            e.printStackTrace();
        }
    }


}
