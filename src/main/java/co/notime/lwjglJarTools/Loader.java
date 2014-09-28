package co.notime.lwjglJarTools;

import org.newdawn.slick.opengl.PNGImageData;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * User: lachlan.krautz
 * Date: 23/08/2014
 * Time: 3:22 PM
 */
public class Loader {

    public static InputStream loadFileStream (String filePath) throws IOException {
        InputStream is = Loader.class.getResourceAsStream(filePath);
        if (is == null) {
            throw new IOException("File not found");
        }
        return is;
    }

    public static ByteBuffer loadByteBufferImage (String imagePath) throws IOException {
        PNGImageData pid = new PNGImageData();
        return pid.loadImage(loadFileStream(imagePath));
    }

    public static Texture loadTexture (String texturePath) throws IOException {
        InputStream is = loadFileStream(texturePath);
        return TextureLoader.getTexture("PNG", is);
    }

    public static ByteBuffer[] loadIconSet (String... paths) throws IOException {
        ByteBuffer[] iconSet = new ByteBuffer[paths.length];
        for (int i = 0; i < paths.length; i++) {
            iconSet[i] = Loader.loadByteBufferImage(paths[i]);
        }
        return iconSet;
    }

}
