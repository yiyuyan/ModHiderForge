package cn.ksmcbrigade.mh_fs.mixin;

import cn.ksmcbrigade.mh_fs.ModHiderConstants;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
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
        for (ModFile hiddenFile : ModHiderConstants.hiddenFiles) {
            ModHiderConstants.involve(hiddenFile);
        }
    }
}
