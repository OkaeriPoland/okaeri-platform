package org.example.okaeriplatformtest;

import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.annotation.Label;
import eu.okaeri.commands.annotation.ServiceDescriptor;
import eu.okaeri.commands.bukkit.response.BukkitResponse;
import eu.okaeri.commands.bukkit.response.RawResponse;
import eu.okaeri.commands.bukkit.response.SuccessResponse;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.bukkit.annotation.Bean;

@ServiceDescriptor(label = "testcmd", aliases = "testing")
public class TestCommand implements CommandService {

    @Inject("testString") private String test;
    @Inject("exampleComplexBean") private String complexContent;

    @Executor
    public BukkitResponse example(@Label String label) {
        return SuccessResponse.of("It works! " + label + " [" + this.test + "]");
    }

    @Executor
    public BukkitResponse complex() {
        return RawResponse.of(this.complexContent);
    }

    @Bean(value = "subbean", register = false)
    public String configureExampleSubbean() {
        return "BEAN FROM " + this.getClass() + "!!";
    }
}
