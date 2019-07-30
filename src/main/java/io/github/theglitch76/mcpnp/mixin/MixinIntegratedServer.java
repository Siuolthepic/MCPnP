package io.github.theglitch76.mcpnp.mixin;

import com.dosse.upnp.UPnP;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import io.github.theglitch76.mcpnp.MCPnP;
import io.github.theglitch76.mcpnp.UPnPUtil;
import io.github.theglitch76.mcpnp.api.LevelInfoGetter;
import io.github.theglitch76.mcpnp.api.WorldDirGetter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.UserCache;
import net.minecraft.world.GameMode;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.level.LevelInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.net.Proxy;

@Mixin(IntegratedServer.class)
public abstract class MixinIntegratedServer extends MinecraftServer implements WorldDirGetter, LevelInfoGetter {

    @Final
    @Shadow
    private MinecraftClient client;

    @Shadow
    private int lanPort;

    @Shadow @Final private LevelInfo levelInfo;

    private File worldDir;

    public MixinIntegratedServer(File file_1, Proxy proxy_1, DataFixer dataFixer_1, CommandManager commandManager_1, YggdrasilAuthenticationService yggdrasilAuthenticationService_1, MinecraftSessionService minecraftSessionService_1, GameProfileRepository gameProfileRepository_1, UserCache userCache_1, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory_1, String string_1) {
        super(file_1, proxy_1, dataFixer_1, commandManager_1, yggdrasilAuthenticationService_1, minecraftSessionService_1, gameProfileRepository_1, userCache_1, worldGenerationProgressListenerFactory_1, string_1);
    }



    @Inject(at = @At("HEAD"), method = "openToLan")
    public void openUpnpPort(GameMode gameMode, boolean cheats, int port, CallbackInfoReturnable<Boolean> cir) {
        //Open it in parallel, on the main thread it causes Windows to report
        client.inGameHud.getChatHud().addMessage(new TranslatableText("mcpnp.upnp.started", port));
        Thread thread = new Thread(() -> {
            //Just to be safe, we register ANOTHER shutdown hook besides stop(). This probably doesn't do anything
            //except add time to shutdown, but better safe than sorry.
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (port != -1) UPnP.closePortTCP(lanPort);
            }));
            UPnPUtil.UPnPResult result = UPnPUtil.init(port);
            switch(result) {
                case SUCCESS:
                    client.inGameHud.getChatHud().addMessage(new TranslatableText("mcpnp.upnp.success", port));
                    client.keyboard.setClipboard(UPnP.getExternalIP() + ":" + port);
                    client.inGameHud.getChatHud().addMessage(new TranslatableText("mcpnp.upnp.success.clipboard", UPnP.getExternalIP(), port));
                    break;
                case FAILED_GENERIC:
                    client.inGameHud.getChatHud().addMessage(new TranslatableText("mcpnp.upnp.failed", port));
                    break;
                case FAILED_MAPPED:
                    client.inGameHud.getChatHud().addMessage(new TranslatableText("mcpnp.upnp.failed.mapped", port));
                    break;
                case FAILED_DISABLED:
                    client.inGameHud.getChatHud().addMessage(new TranslatableText("mcpnp.upnp.failed.disabled"));
                    break;
            }


        });
        thread.start();
    }
    //Hack to get access to the local var
    @ModifyVariable(method = "loadWorld", at = @At("RETURN"))
    private WorldSaveHandler mcpnp_getWorldSaveHandler(WorldSaveHandler worldSaveHandler_1) {
        this.worldDir = worldSaveHandler_1.getWorldDir();
        return worldSaveHandler_1;
    }


    @Inject(at = @At("HEAD"), method = "stop")
    public void closeUpnpPort(boolean boolean_1, CallbackInfo ci) {
        if(lanPort == -1) return;
        MCPnP.LOGGER.info("Closing UPnP port " + lanPort);
        if (!UPnP.closePortTCP(lanPort)) {
            MCPnP.LOGGER.warn("Failed to close port " + lanPort + "! Was it opened in the first place?");
        }
    }

    @Override
    public File getWorldDir() {
        return worldDir;
    }

    @Override
    public LevelInfo getLevelInfo() {
        return levelInfo;
    }
}
