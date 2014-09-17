package co.notime.lwjglnatives;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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

    private JarFile      jarFile;
    private List<String> natives;
    private File         cacheDir;
    private File         projectDir;

    public NativesHandler () {
        jarFile       = getRunningJar();
        natives       = findSystemNatives(jarFile);
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
        return cacheDir != null && canDetermineSystem() && inJar();
    }

    public void cacheNatives () throws Exception {
        logger.info("opening natives");
        checkCapable();
        createDir(cacheDir);
        for (String s : natives) {
            cacheNative(s);
        }
        fixLibraryPath();
    }

    public void cleanupNatives () {
        logger.info("closing natives");
        deleteDir(cacheDir);
    }

    private void createDir (File dir) {
        if (!dir.exists() && !dir.mkdirs()) {
            System.out.println("Unable to make missing dir");
        }
    }

    private void cacheNative (String nativeName) throws Exception {
        logger.info("caching native: " + nativeName);
        InputStream in = jarFile.getInputStream(jarFile.getEntry(nativeName));
        OutputStream out = new FileOutputStream(cacheDir + File.separator + nativeName);
        byte[] buffer = new byte[65536];
        int bufferSize;
        while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, bufferSize);
        }
        in.close();
        out.close();
    }

    private List<String> findSystemNatives (JarFile jarFile) {
        List<String> natives = new ArrayList<String>();
        if (jarFile != null) {
            Enumeration<JarEntry> entities = jarFile.entries();
            while (entities.hasMoreElements()) {
                JarEntry entry = entities.nextElement();
                if ((!entry.isDirectory()) && (entry.getName().indexOf('/') == -1)) {
                    if (isSystemNativeFile(entry.getName())) {
                        natives.add(entry.getName());
                    }
                }
            }
            try {
                jarFile.close();
            } catch (IOException e) {
                logger.error("unable to close jar", e);
            }
        }
        return natives;
    }

    private boolean isSystemNativeFile (String entryName) {
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
        if (!canDetermineSystem()) {
            throw new Exception("Unable to determine system");
        }
        if (jarFile == null) {
            throw new Exception("Not running from jar");
        }
    }

    private boolean canDetermineSystem () {
        String osName = System.getProperty("os.name");
        String systemKey = null;
        if (osName.startsWith("Win")) {
            systemKey = WINDOWS;
        } else if (osName.startsWith("Linux")) {
            systemKey = LINUX;
        } else if ((osName.startsWith("Mac")) || (osName.startsWith("Darwin"))) {
            systemKey = OSX;
        }
        return systemKey != null;
    }

    private void deleteDir (File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.delete()) {
                    System.out.println("Unable to remove native file: " + file.toString());
                }
            }
        }
        if (!dir.delete()) {
            System.out.println("Unable to remove native dir");
        }
    }


    private void fixLibraryPath () {
        System.setProperty("java.library.path", cacheDir.getAbsolutePath());
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
        cacheDirPath += File.separator + "natives" + new Random().nextInt();
        return cacheDirPath;
    }

    private JarFile getRunningJar() {
        JarFile j = null;
        if (inJar()) {
            try {
                j = new JarFile(new File(NativesHandler.class.getProtectionDomain().getCodeSource().getLocation().toURI()), false);
            } catch (Exception e) {
                logger.error("can't find running jar", e);
            }
        }
        return j;
    }

    public String toString () {
        return "Project dir: " + projectDir + "\r\n"
                + "Cache dir: " + cacheDir;
    }

}
