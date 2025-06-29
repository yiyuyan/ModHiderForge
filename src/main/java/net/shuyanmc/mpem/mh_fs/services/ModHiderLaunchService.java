package net.shuyanmc.mpem.mh_fs.services;

import net.shuyanmc.mpem.mh_fs.Base;
import net.shuyanmc.mpem.mh_fs.utils.ForgeModObfuscator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class ModHiderLaunchService implements ITransformationService {

    private boolean loaded = false;
    private File super_config = new File("mh_super-config.json");
    public static File hidden_d = new File(".hidden");


    private static File CONFIG = new File("mh_fs-config.json");
    private static ArrayList<String> hidden = new ArrayList<>();

    @Override
    public String name() {
        return "transfer_mods";
    }

    @Override
    public void initialize(IEnvironment iEnvironment) {
        this.onLoad(iEnvironment,Set.of());
    }

    @Override
    public void onLoad(IEnvironment iEnvironment, Set<String> set) {
        try {
            System.out.println(Arrays.toString(set.toArray()));
            if(loaded) return;
            loaded = true;
            new File("mods").mkdirs();
            //System.out.println(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(new File("z.jar"))));
            FileUtils.writeByteArrayToFile(new File("mods/modHider_forge.jar"), Base64.getDecoder().decode(Base.base64_modHider));

            while (!hidden_d.exists() && !hidden_d.mkdirs()){}
            if(!super_config.exists()) FileUtils.writeStringToFile(super_config,"[]");
            JsonArray arrayz = JsonParser.parseString(FileUtils.readFileToString(super_config)).getAsJsonArray();
            ArrayList<String> hide_files = new ArrayList<>();

            for (JsonElement element : arrayz) {
                hide_files.add(element.getAsString());
            }
            File[] files = new File("mods").listFiles();
            if(files!=null){

                for (File file : files) {
                    if(hide_files.contains(file.getName())){
                        File to = new File(hidden_d.getPath()+"/"+file.getName());
                        System.out.println(to);
                        while(!to.exists() && !to.createNewFile()){}
                        System.out.println(file);
                        FileUtils.copyFile(file,to, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("created the back file.");
                        try {
                            ForgeModObfuscator.encode(to.toPath(), file.toPath());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public @NotNull List<ITransformer> transformers() {
        return List.of();
    }


}
