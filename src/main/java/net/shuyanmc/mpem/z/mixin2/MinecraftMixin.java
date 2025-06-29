package net.shuyanmc.mpem.z.mixin2;

import net.minecraft.client.Minecraft;
import org.apache.commons.io.FileUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "destroy",at = @At("HEAD"))
    public void close(CallbackInfo ci) throws IOException {
        File[] files = new File(".hidden").listFiles();
        File mod = new File("mods");
        if(files!=null){
            for (File file : files) {
                File or = new File(mod.getPath()+"/"+file.getName());
                FileUtils.copyFile(file,or, StandardCopyOption.REPLACE_EXISTING);
                file.delete();
            }
            FileUtils.deleteDirectory(new File(".hidden"));
        }
    }
}
