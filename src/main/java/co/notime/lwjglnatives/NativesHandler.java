package co.notime.lwjglnatives;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

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
    private String systemKey;
    private File cacheDir;
    private File projectDir;

    public NativesHandler () {
        systemKey     = getSystemKey();
        cacheDir      = findCacheDir();
        projectDir    = new File("").getAbsoluteFile();
        logger.info("Cache dir: "   + cacheDir);
        logger.info("Project dir: " + projectDir);
    }

    public static boolean inJar () {
        return Main.class.getResource("Main.class").toString().startsWith("jar");
    }

    /**
     * Check if natives handler is able to find and cache the natives for this system.
     *
     * @return Can or cannot find & cache
     */
    public boolean canCacheNatives () {
        return cacheDir != null && systemKey != null && inJar();
    }

    public void cacheNatives () throws Exception {
        logger.info("opening natives");
        checkCapable();
        List<String> natives = findSystemNatives();
        for (String s : natives) {
            logger.info("native found: " + s);
        }


        // fixLibraryPath();
    }

    /*
    private void findSystemNatives () {
        NativesList nl = new NativesList();
        JsonNode nativesMap = nl.getNativesMap();
        JsonNode w = nativesMap.get("windows");
        logger.info(w);
        for (JsonNode n: w) {
            logger.info("n: " + n);
        }
    }
    */

    public List<String> findSystemNatives () throws Exception {
        List<String> natives = new ArrayList<String>();
        JarFile jarFile = getRunningJar();
        Enumeration<JarEntry> entities = jarFile.entries();
        while (entities.hasMoreElements()) {
            JarEntry entry = entities.nextElement();
            if ((!entry.isDirectory()) && (entry.getName().indexOf('/') == -1)) {
                if (isSystemNativeFile(entry.getName())) {
                    natives.add(entry.getName());
                }
            }
        }
        jarFile.close();
        return natives;
    }

    public boolean isSystemNativeFile (String entryName) {
        String osName = System.getProperty("os.name");
        String fileName   = entryName.toLowerCase();
        return isWindowsNative(osName, fileName)
                || isLinuxNative(osName, fileName)
                || isOsxNative(osName, fileName);
    }

    private boolean isWindowsNative (String osName, String fileName) {
        return osName.startsWith("Win")
                && fileName.endsWith(".dll");
    }

    private boolean isLinuxNative (String osName, String fileName) {
        return osName.startsWith("Linux") && fileName.endsWith(".so");
    }

    private boolean isOsxNative (String osName, String fileName) {
        return (((osName.startsWith("Mac")) || (osName.startsWith("Darwin"))) && (
                (fileName.endsWith(".jnilib")) || (fileName.endsWith(".dylib"))));
    }


    private void checkCapable () throws Exception {
        if (cacheDir == null) {
            throw new IOException("Cache dir not found");
        }
        if (systemKey == null) {
            throw new Exception("Unable to determine system");
        }
    }

    private String getSystemKey () {
        String osName = System.getProperty("os.name");
        String systemKey = null;
        if (osName.startsWith("Win")) {
            systemKey = WINDOWS;
        } else if (osName.startsWith("Linux")) {
            systemKey = LINUX;
        } else if ((osName.startsWith("Mac")) || (osName.startsWith("Darwin"))) {
            systemKey = OSX;
        }
        return systemKey;
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

    public JarFile getRunningJar() {
        JarFile j = null;
        try {
            j = new JarFile(new File(NativesHandler.class.getProtectionDomain().getCodeSource().getLocation().toURI()), false);
        } catch (Exception e) {
            logger.error("can't find running jar", e);
        }
        return j;
    }

    public String toString () {
        return "Project dir: " + projectDir + "\r\n"
                + "Cache dir: " + cacheDir;
    }

}
