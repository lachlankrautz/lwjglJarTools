package co.notime.lwjglnatives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: lachlan.krautz
 * Date: 15/09/2014
 * Time: 10:22 PM
 */
public class NativesList {

    private static Logger logger = LogManager.getLogger(NativesList.class.getName());
    private JsonNode nativesMap;

    public NativesList () {
        InputStream is = NativesList.class.getResourceAsStream("/nativesList.json");
        if (is != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                nativesMap = objectMapper.readValue(is, JsonNode.class);
            } catch (IOException e) {
                logger.error("failed to parse json", e);
            }
        } else {
            logger.error("unable to find json file");
        }
    }

    public JsonNode getNativesMap () {
        return nativesMap;
    }

}
