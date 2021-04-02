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
    @Inject private TestConfig config;

    // testcmd|testing example
    @Executor
    public BukkitResponse example(@Label String label) {
        return SuccessResponse.of("It works! " + label + " [" + this.test + "]");
    }

    // testcmd|testing complex
    @Executor(async = true, description = "wow async execution, db calls go brrr")
    public BukkitResponse complex() {
        return RawResponse.of(this.complexContent, Thread.currentThread().getName());
    }

    // testcmd|testing greet|greeting
    @Executor(pattern = {"greet", "greeting"}, description = "greets you :O")
    public BukkitResponse greet() {
        return RawResponse.of(this.config.getGreeting());
    }

    @Bean(value = "subbean", register = false)
    public String configureExampleSubbean() {
        return "BEAN FROM " + this.getClass() + "!!";
    }
}
