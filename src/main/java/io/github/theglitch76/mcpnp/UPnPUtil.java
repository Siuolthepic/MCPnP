package io.github.theglitch76.mcpnp;

import com.dosse.upnp.UPnP;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.text.TranslatableText;

public class UPnPUtil {
    public enum UPnPResult {
        SUCCESS,
        FAILED_GENERIC,
        FAILED_MAPPED,
        FAILED_DISABLED
    }

    public static UPnPResult init(int port) {
        if (!UPnP.isUPnPAvailable()) {
            return UPnPResult.FAILED_DISABLED;
        }
        if (UPnP.isMappedTCP(port)) {

            return UPnPResult.FAILED_MAPPED;
        }
        MCPnP.LOGGER.info("Opening port " + port + " via UPnP");
        if (!UPnP.openPortTCP(port)) {
            return UPnPResult.FAILED_GENERIC;
        }
        return UPnPResult.SUCCESS;
    }
}
