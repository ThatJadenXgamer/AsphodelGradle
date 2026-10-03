package com.example.mod.fabric;

import com.example.mod.Example;
import net.fabricmc.api.ModInitializer;

public class ExampleFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Example.sharedSetup();
        Example.setup20();
    }
}