package io.github.theglitch76.mcpnp.mixin;

import com.dosse.upnp.UPnP;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import io.github.theglitch76.mcpnp.MCPnP;
import io.github.theglitch76.mcpnp.UPnPUtil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.UserCache;
import net.minecraft.util.registry.RegistryTracker;
import net.minecraft.world.GameMode;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.storage.LevelStorage;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.net.Proxy;

@Mixin(IntegratedServer.class)
public abstract class MixinIntegratedServer extends MinecraftServer {

    @Final
    @Shadow
    private MinecraftClient client;

    @Shadow
    private int lanPort;

	@Shadow public abstract File getRunDirectory();

	private File worldDir;

	public MixinIntegratedServer(Thread thread, RegistryTracker.Modifiable modifiable, LevelStorage.Session session, SaveProperties saveProperties, ResourcePackManager<ResourcePackProfile> resourcePackManager, Proxy proxy, DataFixer dataFixer, ServerResourceManager serverResourceManager, MinecraftSessionService minecraftSessionService, GameProfileRepository gameProfileRepository, UserCache userCache, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory) {
		super(thread, modifiable, session, saveProperties, resourcePackManager, proxy, dataFixer, serverResourceManager, minecraftSessionService, gameProfileRepository, userCache, worldGenerationProgressListenerFactory);
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
            switch (result) {
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

    @Inject(at = @At("HEAD"), method = "stop")
    public void closeUpnpPort(boolean boolean_1, CallbackInfo ci) {
        if(lanPort == -1) return;
        MCPnP.LOGGER.info("Closing UPnP port " + lanPort);
        if (!UPnP.closePortTCP(lanPort)) {
            MCPnP.LOGGER.warn("Failed to close port " + lanPort + "! Was it opened in the first place?");
        }
    }
}
