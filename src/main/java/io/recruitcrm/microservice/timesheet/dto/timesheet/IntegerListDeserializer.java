package io.recruitcrm.microservice.timesheet.dto.timesheet;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

/**
 * Accepts both a single integer and a JSON array of integers for the same field. e.g.
 * both {@code 23} and {@code [23, 45]} are treated as a list.
 */
public class IntegerListDeserializer extends StdDeserializer<List<Integer>> {

	public IntegerListDeserializer() {
		super(List.class);
	}

	@Override
	public List<Integer> deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
		List<Integer> result = new ArrayList<>();

		if (parser.currentToken() == JsonToken.START_ARRAY) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				result.add(this.parseIntegerValue(parser));
			}
		}
		else if (parser.currentToken().isNumeric()) {
			result.add(this.parseIntegerValue(parser));
		}

		return result;
	}

	private Integer parseIntegerValue(JsonParser parser) throws IOException {
		BigInteger numericValue = parser.getBigIntegerValue();
		if ((numericValue.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0)
				|| (numericValue.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0)) {
			throw InvalidFormatException.from(parser, String.format("Numeric value (%s) out of range of int (%d - %d)",
					numericValue, Integer.MIN_VALUE, Integer.MAX_VALUE), numericValue, Integer.class);
		}
		return numericValue.intValue();
	}

}
