package co.notime.lwjglnatives;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

/**
 * User: lachlan.krautz
 * Date: 14/09/2014
 * Time: 2:55 PM
 */
public class NativesHandler {

    private static Logger logger = LogManager.getLogger(NativesHandler.class.getName());
    private static final String WINDOWS = "windows";
    private static final String LINUX   = "linux";
    private static final String OSX     = "osx";

    private String relativePath;
    private File cacheDir;
    private File projectDir;

    public NativesHandler () {
        cacheDir      = findCacheDir();
        projectDir    = new File("").getAbsoluteFile();
        logger.info("Cache dir: "   + cacheDir);
        logger.info("Project dir: " + projectDir);
    }


    public boolean canCacheNatives () {
        return cacheDir != null;
    }

    public void cacheNatives () throws IOException {
        logger.info("opening natives");
        if (cacheDir == null) {
            throw new IOException("Cache dir not found");
        }
        NativesList nl = new NativesList();
        JsonNode nativesMap = nl.getNativesMap();
        JsonNode w = nativesMap.get("windows");
        logger.info(w);
        for (JsonNode n: w) {
            logger.info("n: " + n);
        }

        // fixLibraryPath();
    }

    private String getSystemKey () {

    }

    public void cleanupNatives () {
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
        File cacheDir = null;
        String cacheDirPath = findCacheDirPath();
        if (cacheDirPath != null) {
            cacheDir = new File(cacheDirPath);
        }
        return cacheDir;
    }

    private String findCacheDirPath () {
        String cacheDirPath = System.getProperty("deployment.user.cachedir");
        if ((cacheDirPath == null) || (System.getProperty("os.name").startsWith("Win"))) {
            cacheDirPath = System.getProperty("java.io.tmpdir");
        }
        return cacheDirPath;
    }

    public String toString () {
        return "Project dir: " + projectDir + "\r\n"
                + "Cache dir: " + cacheDir;
    }

}
