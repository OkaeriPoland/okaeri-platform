//package eu.okaeri.platform.velocity.plan;
//
//import eu.okaeri.acl.guardian.Guardian;
//import eu.okaeri.commands.Commands;
//import eu.okaeri.commands.bungee.CommandsBungee;
//import eu.okaeri.commands.guard.GuardAccessHandler;
//import eu.okaeri.commands.handler.access.MultiAccessHandler;
//import eu.okaeri.commands.injector.CommandsInjector;
//import eu.okaeri.commands.validator.CommandsValidator;
//import eu.okaeri.platform.velocity.OkaeriVelocityPlugin;
//import eu.okaeri.platform.bungee.commands.VelocityCommandsResultHandler;
//import eu.okaeri.platform.bungee.commands.VelocityCommandsTasker;
//import eu.okaeri.platform.core.commands.PlatformGuardianContextProvider;
//import eu.okaeri.platform.core.plan.ExecutionTask;
//import eu.okaeri.tasker.core.Tasker;
//import eu.okaeri.validator.OkaeriValidator;
//import eu.okaeri.validator.policy.NullPolicy;
//import lombok.RequiredArgsConstructor;
//
//import java.util.Arrays;
//
//@RequiredArgsConstructor
//public class VelocityCommandsSetupTask implements ExecutionTask<OkaeriVelocityPlugin> {
//
//    @Override
//    public void execute(OkaeriVelocityPlugin platform) {
//
//        // create bukkit commands
//        Commands commands = CommandsBungee.of(platform);
//
//        // register platform specific result handler
//        commands.resultHandler(new VelocityCommandsResultHandler());
//
//        // use injector in commands
//        commands.registerExtension(new CommandsInjector(platform.getInjector()));
//
//        // enable validation
//        commands.registerExtension(new CommandsValidator(OkaeriValidator.of(NullPolicy.NULLABLE)));
//
//        // enable guard acl
//        platform.getInjector().get("guardian", Guardian.class)
//            .ifPresent(guardian -> {
//                GuardAccessHandler guardAccessHandler = new GuardAccessHandler(guardian, new PlatformGuardianContextProvider(platform));
//                commands.accessHandler(new MultiAccessHandler(Arrays.asList(commands.getAccessHandler(), guardAccessHandler)));
//            });
//
//        // allow @Chain in commands
//        platform.getInjector().get("tasker", Tasker.class)
//            .ifPresent(tasker -> commands.registerExtension(new VelocityCommandsTasker(tasker)));
//
//        // register commands injectable
//        platform.registerInjectable("commands", commands);
//    }
//}
