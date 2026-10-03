package com.example.mod.forge;

import com.example.mod.Example;
import net.minecraftforge.fml.common.Mod;

@Mod(Example.MOD_ID)
public class ExampleForge {

    public ExampleForge() {
        Example.sharedSetup();
        Example.setup20();
    }
}