package com.example.mod.neoforge;

import com.example.mod.Example;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Example.MOD_ID)
public class ExampleNeoForge {

    public ExampleNeoForge(IEventBus eventBus) {
        Example.sharedSetup();
        Example.setup21();
    }
}