package co.notime.natives;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 * User: lachlan.krautz
 * Date: 14/09/2014
 * Time: 2:56 PM
 */
public class Main {

    static Logger logger = Logger.getLogger(Main.class);

    public static void main (String[] args) {
        BasicConfigurator.configure();

        logger.info("Entering application");
        NativesHandler n = new NativesHandler();
        logger.info("Exiting application");
    }

}
