package co.notime.lwjglnatives;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: lachlan.krautz
 * Date: 14/09/2014
 * Time: 2:56 PM
 */
public class Main {

    private static Logger logger = LogManager.getLogger(Main.class.getName());

    public static void main (String[] args) {
        logger.info("Entering application");
        if (NativesHandler.inJar()) {
            NativesHandler n = new NativesHandler();
            try {
                n.cacheNatives();
            } catch (Exception e) {
                logger.error("Unable to cache natives", e);
            }
        }
        logger.info("Exiting application");
    }



}
