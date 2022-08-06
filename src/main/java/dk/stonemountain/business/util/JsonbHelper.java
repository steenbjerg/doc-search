package dk.stonemountain.business.util;

import java.lang.reflect.Type;
import java.util.List;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonbHelper {
	private static final Logger logger = LoggerFactory.getLogger(JsonbHelper.class);
	
	private JsonbHelper() {
		// to prevent instantiation
	}
	
	public static String toJson(Object data) {
		try {
			String content = "";
			JsonbConfig config = new JsonbConfig().withPropertyVisibilityStrategy(new PrivateVisibilityStrategy());
			try (Jsonb jsonb = JsonbBuilder.create(config)) {
				content = jsonb.toJson(data);
			}
			return content;
		} catch (Exception e) {
			throw new JsonSerializationException("Can not generate json", e);
		}
	}

	public static <T> T fromJson(String content, Class<T> responseClass) {
		return fromJson(content, responseClass, false);
	}

	public static <T> T fromJson(String content, Class<T> responseClass, boolean noLogging) {
		if (!noLogging) {
			logger.trace("Unmarshalling : {}", content);
		}
		JsonbConfig config = new JsonbConfig().withPropertyVisibilityStrategy(new PrivateVisibilityStrategy());
		try (Jsonb jsonb = JsonbBuilder.create(config)) {
			return jsonb.fromJson(content, responseClass);
		} catch (Exception e) {
			throw new JsonSerializationException("Failed to handle json", e);
		}
	}
	
	public static <T> List<T> fromJson(String content, Type runtimeType, boolean noLogging) {
		if (!noLogging) {
			logger.trace("Unmarshalling : {}", content);
		}
		JsonbConfig config = new JsonbConfig().withPropertyVisibilityStrategy(new PrivateVisibilityStrategy());
		try (Jsonb jsonb = JsonbBuilder.create(config)) {
			return jsonb.fromJson(content, runtimeType);
		} catch (Exception e) {
			throw new JsonSerializationException("Failed to handle json", e);
		}
	}

	public static <T> List<T> fromJson(String content, Type runtimeType) {
		return fromJson(content, runtimeType, false);
	}
}
