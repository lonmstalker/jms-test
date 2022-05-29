package io.lonmstalker.jmstest.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

@Slf4j
@UtilityClass
public class ExtensionUtils {

    @NonNull
    public static String writeValueAsStringWithCatch(@NonNull final ObjectMapper obj, @NonNull final Object body) {
        try {
            return obj.writeValueAsString(body);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return "";
        }
    }

}
