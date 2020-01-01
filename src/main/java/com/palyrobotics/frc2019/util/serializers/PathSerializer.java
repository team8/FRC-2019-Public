package com.palyrobotics.frc2019.util.serializers;

import java.io.IOException;
import java.lang.reflect.Type;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.ser.SerializerBase;

import com.palyrobotics.frc2019.util.trajectory.Path;

public class PathSerializer extends SerializerBase<Path> {

	public PathSerializer(Class<Path> t) {
		super(t);
	}

	@Override
	public void serialize(Path value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		// jgen.writeStartObject();

		// jgen.writeFieldName("path");
		System.out.println(value.getWayPoints().get(value.getWayPoints().size()-1).position.getX());
		jgen.writeStartArray();
		for (var i = 0; i < value.getWayPoints().size(); i++) {
			jgen.writeStartObject();
			jgen.writeFieldName("Waypoint");
			jgen.writeStartArray();
			// pos x
			jgen.writeStartObject();
			jgen.writeFieldName("x");
			jgen.writeNumber(value.getWayPoints().get(i).position.getX());
			jgen.writeEndObject();
			// pos y
			jgen.writeStartObject();
			jgen.writeFieldName("y");
			jgen.writeNumber(value.getWayPoints().get(i).position.getY());
			jgen.writeEndObject();
			// speed
			jgen.writeStartObject();
			jgen.writeFieldName("speed");
			jgen.writeNumber(value.getWayPoints().get(i).speed);
			jgen.writeEndObject();
			jgen.writeEndArray();
			jgen.writeEndObject();
		}
		jgen.writeEndArray();

		// jgen.writeEndObject();

	}

	@Override
	public void serializeWithType(Path value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer)
			throws IOException, JsonProcessingException {
		typeSer.writeTypePrefixForScalar(value, jgen);
		serialize(value, jgen, provider);
		typeSer.writeTypeSuffixForScalar(value, jgen);
	}

	@Override
	public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
		return createSchemaNode("array", true);
	}

}
