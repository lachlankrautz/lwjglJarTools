package co.notime.lwjglnatives;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * User: lachlan.krautz
 * Date: 14/09/2014
 * Time: 2:55 PM
 */
public class NativesHandler {

    private static Logger logger = LogManager.getLogger(NativesHandler.class.getName());
    private static final String DEFAULT_CACHE_DIR_NAME = "co.notime.lwjglnatives";
    private static final String WINDOWS = "windows";
    private static final String LINUX   = "linux";
    private static final String OSX     = "osx";

    private JarFile      jarFile;
    private List<String> natives;

    public NativesHandler () {
        jarFile       = getRunningJar();
        natives       = findSystemNatives(jarFile);
    }

    public void cacheNatives () {
        cacheNatives(DEFAULT_CACHE_DIR_NAME);
    }

    public void cacheNatives (String cacheDirName) {
        if (canCacheNatives()) {
            File cacheDir = getCacheDir(cacheDirName);
            for (String s : natives) {
                String cacheNativePath = cacheDir + File.separator + s;
                File cacheNative = new File(cacheNativePath);
                if (!cacheNative.exists()) {
                    cacheNative(s, cacheNativePath);
                }
            }
            try {
                jarFile.close();
            } catch (IOException e) {
                logger.error("unable to close jar", e);
            }
            fixLibraryPath(cacheDir);
        }
    }

    private void cacheNative (String nativeName, String cacheNativePath) {
        InputStream in;
        OutputStream out;
        try {
            in  = jarFile.getInputStream(jarFile.getEntry(nativeName));
            out = new FileOutputStream(cacheNativePath);
            byte[] buffer = new byte[65536];
            int bufferSize;
            while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, bufferSize);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            logger.error("unable to write native to cache dir", e);
        } catch (IOException e) {
            logger.error("unable to extract native from jar", e);
        }
    }

    private boolean inJar () {
        return jarFile != null && !jarFile.getName().contains("lwjglnatives");
    }

    private boolean canCacheNatives () {
        return canDetermineSystem() && inJar();
    }

    private List<String> findSystemNatives (JarFile jarFile) {
        List<String> natives = new ArrayList<String>();
        if (jarFile != null) {
            Enumeration<JarEntry> entities = jarFile.entries();
            while (entities.hasMoreElements()) {
                JarEntry entry = entities.nextElement();
                String name    = entry.getName();
                if ((!entry.isDirectory()) && (name.indexOf('/') == -1)) {
                    if (isSystemNativeFile(name)) {
                        natives.add(name);
                    }
                }
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


    private void fixLibraryPath (File cacheDir) {
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

    private File getCacheDir (String cacheDirName) {
        File cacheDir = null;
        String cacheDirPath = findCacheDirPath();
        if (cacheDirPath != null) {
            cacheDirPath += File.separator + cacheDirName;
            cacheDir = new File(cacheDirPath);
            if (!cacheDir.exists() && !cacheDir.mkdir()) {
                logger.error("unable to create cache dir");
            }
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

    private JarFile getRunningJar() {
        JarFile j = null;
        if (NativesHandler.class.getResource("NativesHandler.class").toString().startsWith("jar")) {
            try {
                j = new JarFile(new File(NativesHandler.class.getProtectionDomain().getCodeSource().getLocation().toURI()), false);
            } catch (Exception e) {
                logger.error("not in jar", e);
            }
        }
        return j;
    }

}
