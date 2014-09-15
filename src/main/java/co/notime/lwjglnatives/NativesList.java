package co.notime.lwjglnatives;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: lachlan.krautz
 * Date: 15/09/2014
 * Time: 10:22 PM
 */
public class NativesList {

    private static Logger logger = LogManager.getLogger(NativesList.class.getName());

    public NativesList () throws IOException {
        File f = new File(".");
        String[] sl = f.list();
        if (sl != null) {
            for (String s : sl) {
                logger.info(s);
            }
        } else {
            logger.error("empty list");
        }
        /*
        byte[] mapData = Files.readAllBytes(Paths.get("nativesList.json"));
        ObjectMapper objectMapper = new ObjectMapper();
        HashMap myMap = objectMapper.readValue(mapData, HashMap.class);
        logger.info("Map is: " + myMap);
        */
    }

}
