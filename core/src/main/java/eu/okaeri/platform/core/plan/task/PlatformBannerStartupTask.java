package eu.okaeri.platform.core.plan.task;

import eu.okaeri.platform.core.OkaeriPlatform;
import eu.okaeri.platform.core.plan.ExecutionTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class PlatformBannerStartupTask implements ExecutionTask<OkaeriPlatform> {

    @Override
    public void execute(OkaeriPlatform platform) {

        InputStream bannerResource = Thread.currentThread().getContextClassLoader().getResourceAsStream("banner.txt");
        if (bannerResource == null) {
            platform.log("\n   ____  __                   _    ____  __      __  ____                   \n" +
                    "  / __ \\/ /______ ____  _____(_)  / __ \\/ /___ _/ /_/ __/___  _________ ___ \n" +
                    " / / / / //_/ __ `/ _ \\/ ___/ /  / /_/ / / __ `/ __/ /_/ __ \\/ ___/ __ `__ \\\n" +
                    "/ /_/ / ,< / /_/ /  __/ /  / /  / ____/ / /_/ / /_/ __/ /_/ / /  / / / / / /\n" +
                    "\\____/_/|_|\\__,_/\\___/_/  /_/  /_/   /_/\\__,_/\\__/_/  \\____/_/  /_/ /_/ /_/ \n" +
                    "\n            https://github.com/OkaeriPoland/okaeri-platform\n");
            return;
        }

        platform.log(new BufferedReader(new InputStreamReader(bannerResource))
                .lines()
                .collect(Collectors.joining("\n")));
    }
}
