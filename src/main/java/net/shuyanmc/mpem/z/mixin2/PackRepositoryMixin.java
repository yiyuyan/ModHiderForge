package net.shuyanmc.mpem.z.mixin2;

import net.minecraft.server.packs.repository.PackRepository;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.shuyanmc.mpem.z.ModHiderConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PackRepository.class)
public class PackRepositoryMixin {

    @Unique
    private boolean loaded = false;

    @Inject(method = "reload",at = @At("TAIL"))
    public void initMods(CallbackInfo ci){
        if(loaded) return;
        loaded = true;
        new Thread(()->{
            while (ModList.get()==null) Thread.yield();
            for (ModFile hiddenFile : ModHiderConstants.hiddenFiles) {
                ModHiderConstants.involve(hiddenFile);
            }
        }).start();
        System.out.println("Loading mods...");
    }
}
