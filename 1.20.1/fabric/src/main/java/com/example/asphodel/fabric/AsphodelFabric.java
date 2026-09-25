package com.example.asphodel.fabric;

import com.example.asphodel.Asphodel;
import net.fabricmc.api.ModInitializer;

public class AsphodelFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Asphodel.sharedSetup();
        Asphodel.setup20();
    }
}