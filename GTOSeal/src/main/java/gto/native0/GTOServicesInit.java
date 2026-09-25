package gto.native0;

import native0.Loader;
import native0.hidden.Hidden0;
import org.slf4j.Logger;

public final class GTOServicesInit {

    public static final String NAME = "GTO Services";

    public static final Logger LOGGER = null;

    static {
        Loader.registerNativesForClass(0, GTOServicesInit.class);
        Hidden0.special_clinit_0_10(GTOServicesInit.class);
    }
}
