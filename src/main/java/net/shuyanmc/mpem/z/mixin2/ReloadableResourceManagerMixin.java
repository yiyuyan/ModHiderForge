package net.shuyanmc.mpem.z.mixin2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.loading.EarlyLoadingException;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.shuyanmc.mpem.z.ModHiderConstants;
import org.apache.commons.io.FileUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

@Mixin(ReloadableResourceManager.class)
public class ReloadableResourceManagerMixin {

    @Unique
    private static boolean loaded = false;

    @Unique
    private static File CONFIG = new File("mh_fs-config.json");
    @Unique
    private static ArrayList<String> hidden = new ArrayList<>();

    @Inject(method = "<init>",at = @At("TAIL"))
    private void init(PackType p_203820_, CallbackInfo ci){
        try {
            if(loaded) return;
            loaded = true;
            ModLoader loader = ModLoader.get();
            Field loadingModListF = loader.getClass().getDeclaredField("loadingModList");
            loadingModListF.setAccessible(true);
            LoadingModList loadingModList = (LoadingModList) loadingModListF.get(loader);
            LoadingModList newLoadingModList;
            if(!CONFIG.exists()){
                FileUtils.writeStringToFile(CONFIG,"[\"mh_f\",\"mh_fs\"]");
            }
            JsonArray array = JsonParser.parseString(FileUtils.readFileToString(CONFIG)).getAsJsonArray();
            for (JsonElement element : array) {
                hidden.add(element.getAsString());
            }

            System.out.println(Arrays.toString(hidden.toArray()));

            ArrayList<ModFile> hide_modFiles = new ArrayList<>();
            ArrayList<ModFileInfo> modFiles = new ArrayList<>(loadingModList.getModFiles());

            ArrayList<ModInfo> modInfos = new ArrayList<>();
            ArrayList<ModFile> modFiles1 = new ArrayList<>();

            for (ModInfo mod : loadingModList.getMods()) {
                System.out.println(mod.getModId());
                if(!hidden.contains(mod.getModId())){
                    modInfos.add(mod);
                }
                else{

                    hide_modFiles.add(mod.getOwningFile().getFile());
                }
            }

            for (ModFileInfo modFile : modFiles) {
                if(!hide_modFiles.contains(modFile.getFile())){
                    modFiles1.add(modFile.getFile());
                }
            }

            EarlyLoadingException earlyLoadingException = null;
            try {
                earlyLoadingException = loadingModList.getErrors().get(0);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            System.out.println(Arrays.toString(modFiles1.toArray()));
            System.out.println(Arrays.toString(modInfos.toArray()));

            newLoadingModList = LoadingModList.of(modFiles1,modInfos,earlyLoadingException);
            loadingModListF.set(loader,newLoadingModList);

            ModHiderConstants.hiddenFiles.addAll(hide_modFiles);

            System.out.println("Hide successfully!");

        } catch (NoSuchFieldException | IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
