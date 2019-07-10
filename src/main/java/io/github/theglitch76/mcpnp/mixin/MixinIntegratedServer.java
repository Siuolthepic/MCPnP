package io.github.theglitch76.mcpnp.mixin;

import com.dosse.upnp.UPnP;
import io.github.theglitch76.mcpnp.MCPnP;
import io.github.theglitch76.mcpnp.UPnPUtil;
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
        //Open it in parallel, on the main thread it causes Windows to report
        client.inGameHud.getChatHud().addMessage(new TranslatableText("mcpnp.upnp.started", port));

        Thread thread = new Thread(() -> {
            UPnPUtil.UPnPResult result = UPnPUtil.init(port);
            switch(result) {
                case SUCCESS:
                    client.inGameHud.getChatHud().addMessage(new TranslatableText("mcpnp.upnp.success", port));
                    break;
                case FAILED_GENERIC:
                    client.inGameHud.getChatHud().addMessage(new TranslatableText("mcpnp.upnp.failed"));
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
    public void beforeStop(boolean boolean_1, CallbackInfo ci) {
        MCPnP.LOGGER.info("Closing UPnP port " + lanPort);
        if (!UPnP.closePortTCP(lanPort)) {
            MCPnP.LOGGER.warn("Failed to close port " + lanPort + "! Was it opened in the first place?");
        }
    }
}
