package io.github.theglitch76.mcpnp.gui.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class PortWidget extends TextFieldWidget {

    public PortWidget(TextRenderer renderer, int x, int y, int width, int height, int port, Text text) {
        super(renderer, x, y, width, height, text);
        this.setText(Integer.toString(port));
        //Max value of a port is 65536
        this.setMaxLength(5);
    }


    @Override
    public void write(String text) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Integer.parseUnsignedInt(text);
        } catch (NumberFormatException ex) {
            return;
        }
        super.write(text);
    }

}
