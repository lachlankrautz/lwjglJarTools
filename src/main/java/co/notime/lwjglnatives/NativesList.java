package co.notime.lwjglnatives;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * User: lachlan.krautz
 * Date: 15/09/2014
 * Time: 10:22 PM
 */
public class NativesList {

    private static Logger logger = LogManager.getLogger(NativesList.class.getName());
    private Map nativesMap;

    public NativesList () {
        InputStream is = NativesList.class.getResourceAsStream("/nativesList.json");
        if (is != null) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[16384];
            int i;
            try {
                while ((i = is.read(buffer, 0, buffer.length)) != -1) {
                    os.write(buffer, 0, i);
                }
                os.flush();
                byte[] jsonData = os.toByteArray();
                ObjectMapper objectMapper = new ObjectMapper();
                nativesMap = objectMapper.readValue(jsonData, HashMap.class);
            } catch (IOException e) {
                logger.error("failed to parse json", e);
            }
        } else {
            logger.error("unable to find json file");
        }
    }

    public Map getNativesMap () {
        return nativesMap;
    }

}
