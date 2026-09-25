package com.example.asphodel.forge;

import com.example.asphodel.Asphodel;
import net.minecraftforge.fml.common.Mod;

@Mod(Asphodel.MOD_ID)
public class AsphodelForge {

    public AsphodelForge() {
        Asphodel.sharedSetup();
        Asphodel.setup20();
    }
}