package net.shuyanmc.mpem.z;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModHiderConstants {
    public static final ArrayList<ModFile> hiddenFiles = new ArrayList<>();

    public static void involve(ModFile hiddenFile) {

        try (JarFile jarFile = new JarFile(hiddenFile.getFilePath().toFile())){
            Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
            while (jarEntryEnumeration.hasMoreElements()){
                JarEntry entry = jarEntryEnumeration.nextElement();
                if(entry.getName().endsWith(".class")){
                    String clazz_s = entry.getName().replace(".class","").replace("/",".");
                    Class<?> clazz = null;
                    try {
                        clazz = Class.forName(clazz_s);
                    } catch (NoClassDefFoundError | Exception e) {
                        //nothing
                        System.out.println("Error in class: "+clazz_s);
                        return;
                    }

                    if(clazz.isAnnotationPresent(Mod.class)){
                        Constructor<?> constructor = clazz.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        constructor.newInstance();
                        System.out.println("Create the mod instance successfully: "+clazz_s);
                        return;
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
