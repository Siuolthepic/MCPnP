package io.github.theglitch76.mcpnp.mixin;

import com.dosse.upnp.UPnP;
import io.github.theglitch76.mcpnp.MCPnP;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IntegratedServer.class)
public abstract class MixinIntegratedServer {

    @Final
    @Shadow
    private MinecraftClient client;

    @Shadow
    private int lanPort;

    @Inject(at = @At("HEAD"), method = "openToLan")
    public void beforeOpenToLan(GameMode gameMode, boolean cheats, int port, CallbackInfoReturnable<Boolean> cir) {
        if (!UPnP.isUPnPAvailable()) {
            client.inGameHud.getChatHud().addMessage(new TranslatableText("mcpnp.upnp.failed.unavailable"));
            return;
        }
        if (UPnP.isMappedTCP(port)) {
            client.inGameHud.getChatHud().addMessage(new TranslatableText("mcpnp.upnp.failed.mapped"));
            return;
        }
        MCPnP.LOGGER.info("Opening port " + port + " via UPnP");
        if (!UPnP.openPortTCP(port)) {
            client.inGameHud.getChatHud().addMessage(new TranslatableText("mcpnp.upnp.failed"));
        }
        client.inGameHud.getChatHud().addMessage(new TranslatableText("mcpnp.upnp.success"));


    }

    @Inject(at = @At("HEAD"), method = "stop")
    public void beforeStop(boolean boolean_1, CallbackInfo ci) {
        MCPnP.LOGGER.info("Closing UPnP port " + lanPort);
        if (!UPnP.closePortTCP(lanPort)) {
            MCPnP.LOGGER.warn("Failed to close port " + lanPort + "! Was it opened in the first place?");
        }
    }
}
