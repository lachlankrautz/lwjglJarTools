package co.notime.lwjglnatives;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.lang.reflect.Field;

/**
 * User: lachlan.krautz
 * Date: 14/09/2014
 * Time: 2:55 PM
 */
public class NativesHandler {

    private static Logger logger = LogManager.getLogger(NativesHandler.class.getName());

    private File cacheDir;
    private File projectDir;

    public NativesHandler () {
        cacheDir   = findCacheDir();
        projectDir = new File("").getAbsoluteFile();
        logger.info("Cache dir: "   + cacheDir);
        logger.info("Project dir: " + projectDir);
    }

    public void openNatives () {
        logger.info("opening natives");
        // fixLibraryPath();
    }

    public void closeNatives () {
        logger.info("closing natives");
    }


    private void fixLibraryPath () {
        System.setProperty( "java.library.path", "natives" );
        Field fieldSysPath;
        try {
            fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible( true );
            fieldSysPath.set( null, null );
        } catch (NoSuchFieldException e) {
            logger.warn("Sys path not found", e);
        } catch (IllegalAccessException e) {
            logger.warn("Illegal access on sys path", e);
        }
    }

    private File findCacheDir () {
        String cacheDirPath = System.getProperty("deployment.user.cachedir");
        if ((cacheDirPath == null) || (System.getProperty("os.name").startsWith("Win"))) {
            cacheDirPath = System.getProperty("java.io.tmpdir");
        }
        return new File(cacheDirPath);
    }

    public String toString () {
        return "Project dir: " + projectDir + "\r\n"
                + "Cache dir: " + cacheDir;
    }

}
