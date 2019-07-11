package io.github.theglitch76.mcpnp.gui.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class PortWidget extends TextFieldWidget {

    public PortWidget(TextRenderer renderer, int x, int y, int width, int height, int port, String string_1) {
        super(renderer, x, y, width, height, string_1);
        this.setText(Integer.toString(port));
        //Max value of a port is 65536
        this.setMaxLength(5);

    }



    @Override
    public void addText(String string_1) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Integer.parseUnsignedInt(string_1);
        } catch (NumberFormatException ex) {
            return;
        }
        super.addText(string_1);
    }

}
