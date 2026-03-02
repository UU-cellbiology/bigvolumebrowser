package bvb.io.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class SerializationIO
{
	public static final ObjectMapper MAPPER = new ObjectMapper()
	        .enable(SerializationFeature.INDENT_OUTPUT);
}
