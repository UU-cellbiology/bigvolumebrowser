package bvb.io.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SerializationIO
{
	public static final ObjectMapper MAPPER = new ObjectMapper()
	        .enable(SerializationFeature.INDENT_OUTPUT);
	
    public static String getTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        return LocalDateTime.now().format(formatter);
    }
}
