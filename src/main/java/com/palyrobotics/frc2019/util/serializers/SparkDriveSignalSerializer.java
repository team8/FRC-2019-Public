package com.palyrobotics.frc2019.util.serializers;

import java.io.IOException;
import java.lang.reflect.Type;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.ser.SerializerBase;

import com.palyrobotics.frc2019.util.SparkDriveSignal;

public class SparkDriveSignalSerializer extends SerializerBase<SparkDriveSignal> {

	public SparkDriveSignalSerializer(Class<SparkDriveSignal> t) {
		super(t);
	}

	@Override
	public void serialize(SparkDriveSignal value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonGenerationException {
		// jgen.writeStartObject();
		jgen.writeStartArray();
		jgen.writeStartObject();
		jgen.writeFieldName("leftOutput");
		jgen.writeNumber(value.leftOutput.getReference());
		jgen.writeEndObject();
		jgen.writeStartObject();
		jgen.writeFieldName("rightOutput");
		jgen.writeNumber(value.rightOutput.getReference());
		jgen.writeEndObject();
		// jgen.writeNumberField("leftOutput", value.leftOutput.getReference());
		// jgen.writeNumberField("rightOutput", value.rightOutput.getReference());
		jgen.writeEndArray();
		// jgen.writeEndObject();
	}

	@Override
	public void serializeWithType(SparkDriveSignal value, JsonGenerator jgen, SerializerProvider provider,
			TypeSerializer typeSer) throws IOException, JsonProcessingException {
		typeSer.writeTypePrefixForScalar(value, jgen);
		serialize(value, jgen, provider);
		typeSer.writeTypeSuffixForScalar(value, jgen);
	}

	@Override
	public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
		return createSchemaNode("string", true);
	}

}
