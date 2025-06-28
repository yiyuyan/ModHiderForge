package cn.ksmcbrigade.mh_fs;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ModHiderForgeSuper.MODID)
public class ModHiderForgeSuper {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "mh_fs";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public ModHiderForgeSuper() {
        MinecraftForge.EVENT_BUS.register(this);
        //just for test
        System.out.println("Dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd1111d2111111131");
    }
}
