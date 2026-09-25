package com.example.asphodel.neoforge;

import com.example.asphodel.Asphodel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Asphodel.MOD_ID)
public class AsphodelNeoForge {

    public AsphodelNeoForge(IEventBus eventBus) {
        Asphodel.sharedSetup();
        Asphodel.setup21();
    }
}