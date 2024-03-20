package dk.stonemountain.search.util;

public class JsonSerializationException extends RuntimeException {
	public JsonSerializationException(Exception e) {
		super ("Could not craete Json serializer", e);
	}

	public JsonSerializationException(String message, Exception e) {
		super(message, e);
	}
}

