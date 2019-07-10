package io.github.theglitch76.mcpnp.mixin;

import io.github.theglitch76.mcpnp.MCPnP;
import io.github.theglitch76.mcpnp.gui.widget.PortWidget;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OpenToLanScreen.class)
public class MixinOpenToLanScreen extends Screen {

    @Shadow private String gameMode;
    @Shadow private boolean allowCommands;
    private PortWidget portWidget;
    public MixinOpenToLanScreen(Text title) {
        super(title);
    }
    @Inject(at = @At("RETURN"), method = "init")
    public void afterInit(CallbackInfo ci) {

        portWidget = this.addButton(new PortWidget(this.font, this.width / 2 - 155, 125, 150, 20, this.portWidget ,I18n.translate("mcpnp.port")));
        MCPnP.LOGGER.warn(portWidget.getText());
        //TODO mod compat? what if someone mixins at HEAD and adds something
        this.buttons.remove(0);
        this.children.remove(0);
        this.addButton(new ButtonWidget(this.width / 2 - 155, this.height - 28, 150, 20, I18n.translate("lanServer.start"), (buttonWidget_1) -> {

            int port = Integer.parseInt(portWidget.getText());


            if(port < 1024 /*system reserve*/ || port > 65536) {
                return; //TODO add warning for port out of range
            }
            //Close the screen
            this.minecraft.openScreen(null);
            //Output text
            TranslatableText text_2;
            if (this.minecraft.getServer().openToLan(GameMode.byName(this.gameMode), this.allowCommands, port)) {
                text_2 = new TranslatableText("commands.publish.started", port);
            } else {
                text_2 = new TranslatableText("commands.publish.failed");
            }

            this.minecraft.inGameHud.getChatHud().addMessage(text_2);
          }));
    }

}
