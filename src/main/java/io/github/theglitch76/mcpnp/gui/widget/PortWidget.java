package io.github.theglitch76.mcpnp.gui.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NetworkUtils;

public class PortWidget extends TextFieldWidget {

    public PortWidget(TextRenderer renderer, int x, int y, int width, int height,TextFieldWidget oldTextWidget, String string_1) {
        super(renderer, x, y, width, height, string_1);
        if(oldTextWidget == null) {
            this.setText(Integer.toString(NetworkUtils.findLocalPort()));
        } else {
            this.setText(oldTextWidget.getText());
        }
        //Max value of a port is 65536
        this.setMaxLength(5);

    }



    @Override
    public void addText(String string_1) {
        try {
            Integer.parseUnsignedInt(string_1);
        } catch (NumberFormatException ex) {
            return;
        }
        super.addText(string_1);
    }

}
