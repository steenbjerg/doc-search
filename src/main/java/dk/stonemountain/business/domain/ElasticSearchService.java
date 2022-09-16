package dk.stonemountain.business.domain;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.stonemountain.business.domain.Hit.Pair;
import dk.stonemountain.business.dto.Site;
import dk.stonemountain.business.util.JsonbHelper;

@ApplicationScoped
public class ElasticSearchService {
    private static final Logger log = LoggerFactory.getLogger(ElasticSearchService.class);
    
    @FunctionalInterface
    public interface SourceReader<T> {
        T read(String id, JsonObject o);
    }

    public static final SourceReader<Page> pageSourceReader = (id, o) -> JsonbHelper.fromJson(o.toString(), Page.class);
    public static final SourceReader<Site> siteSourceReader = (id, o) -> JsonbHelper.fromJson(o.toString(), Site.class);

    @Inject
    RestClient restClient;
    
    public <T> Optional<T> getById(SourceReader<T> reader, String index, String id) {
        try {
            Request request = new Request(
                    "GET",
                    "/" + index + "/_doc/" + id);
            Response response = restClient.performRequest(request);
            String responseBody = EntityUtils.toString(response.getEntity());
            log.debug("Retrieved entity {} from index {} with id {}", responseBody, index, id);

            try (JsonReader jsonReader = Json.createReader(new StringReader(responseBody))) {
                JsonObject jsonObj = jsonReader.readObject();
                JsonObject sourceObj = jsonObj.getJsonObject("_source");
                return Optional.of(reader.read(id, sourceObj));
            }
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public void addToIndex(String index, String id, Object source) {
        Request request = new Request(
                "PUT",
                "/" + index + "/_doc/" + id); 
        String body = JsonbHelper.toJson(source);
        request.setJsonEntity(body);
        log.debug("Adding with id {} to index {}: {}", id, index, source);
        try {
            restClient.performRequest(request); 
        } catch (IOException e) {
            throw new RuntimeException("Failed to add source " + source + " to index " + index + " with id " + id, e);
        }
    }

    public static class TermMatchPair {
        public enum QueryType {
            TERM, WILDCARD, SIMPLE_QUERY_STRING;

            public String field() {
                return name().toLowerCase();
            }
        }

        public QueryType type;
        public String term;
        public String match;

        public TermMatchPair(QueryType type, String term, String match) {
            this.type = type;
            this.term = term;
            this.match = match;
        }
    }

    public <T> List<Hit<T>> searchByFullTextSearch(String index, SourceReader<T> reader, String query, String... fields) {
        JsonObjectBuilder simpleQueryBuilder = Json.createObjectBuilder();
        JsonObjectBuilder queryBuilder = Json.createObjectBuilder();
        JsonArrayBuilder fieldsBuilder = Json.createArrayBuilder();
        Arrays.asList(fields).forEach(fieldsBuilder::add);
        
        queryBuilder.add("query", query);
        queryBuilder.add("fields", fieldsBuilder.build());
        queryBuilder.add("default_operator", "and");
        queryBuilder.add("analyze_wildcard", true);
        simpleQueryBuilder.add("simple_query_string", queryBuilder.build());

        JsonObjectBuilder hlBuilder = Json.createObjectBuilder();
        JsonObjectBuilder hlFieldsBuilder = Json.createObjectBuilder();
        JsonObjectBuilder hlOrderBuilder = Json.createObjectBuilder();
        JsonArrayBuilder hlPreTagsBuilder = Json.createArrayBuilder();
        JsonArrayBuilder hlPostTagsBuilder = Json.createArrayBuilder();

        hlPreTagsBuilder.add("<b>");
        hlBuilder.add("pre_tags", hlPreTagsBuilder.build());
        
        hlOrderBuilder.add("order", "score");
        Arrays.asList(fields).forEach(f -> hlFieldsBuilder.add(f, hlOrderBuilder.build()));
        hlBuilder.add("fields", hlFieldsBuilder.build());

        hlPostTagsBuilder.add("</b>");
        hlBuilder.add("post_tags", hlPostTagsBuilder.build());

        return search(index, reader, simpleQueryBuilder.build(), Optional.of(hlBuilder.build()), fields);
    }


    public <T> List<Hit<T>> searchByTerms(String index, SourceReader<T> reader, TermMatchPair... termPairs) {
        JsonObjectBuilder boolBuilder = Json.createObjectBuilder();
        JsonObjectBuilder mustBuilder = Json.createObjectBuilder();
        JsonArrayBuilder termsBuilder = Json.createArrayBuilder();

        List.of(termPairs).stream()
            .map(p -> {
                JsonObjectBuilder matchBuilder = Json.createObjectBuilder();
                JsonObjectBuilder termBuilder = Json.createObjectBuilder();
                termBuilder.add(p.term, p.match);
                matchBuilder.add(p.type.field(), termBuilder.build());
                return matchBuilder;
            })
            .forEach(termsBuilder::add);
        mustBuilder.add("must", termsBuilder.build());
        boolBuilder.add("bool", mustBuilder.build());
        return search(index, reader, boolBuilder.build(), Optional.empty());
    }

    public <T> List<Hit<T>> search(String index, SourceReader<T> reader, String term, String match) {
        JsonObjectBuilder matchBuilder = Json.createObjectBuilder();
        JsonObjectBuilder termBuilder = Json.createObjectBuilder();
        termBuilder.add(term, match);
        matchBuilder.add("match", termBuilder.build());
        return search(index, reader, matchBuilder.build(), Optional.empty());
    }

    public <T> List<Hit<T>> searchAll(String index, SourceReader<T> reader) {
        JsonObjectBuilder matchBuilder = Json.createObjectBuilder();
        matchBuilder.add("match_all", JsonValue.EMPTY_JSON_OBJECT);
        return search(index, reader, matchBuilder.build(), Optional.empty());
    }


    public <T> List<Hit<T>> search(String index, SourceReader<T> reader, JsonObject query, Optional<JsonObject> highlight, String... fields) {
        try {
            Request request = new Request(
                    "GET",
                    "/" + index + "/_search");

            JsonObjectBuilder queryBuilder = Json.createObjectBuilder();
            queryBuilder.add("size", 25);
            queryBuilder.add("query", query);
            highlight.ifPresent(h -> queryBuilder.add("highlight", h));
            String queryStr = queryBuilder.build().toString();
            log.debug("query: {}", queryStr);

            request.setJsonEntity(queryStr);
            
            Response response = restClient.performRequest(request);
            String responseBody = EntityUtils.toString(response.getEntity());
            log.trace("Response body: {}", responseBody);


            try (JsonReader jsonReader = Json.createReader(new StringReader(responseBody))) {
                JsonObject jsonObj = jsonReader.readObject();
                JsonObject shardHits = jsonObj.getJsonObject("hits");
                JsonArray hits = shardHits.getJsonArray("hits");
                return hits.stream()
                    .map(JsonValue::asJsonObject)
                    .map(o -> readHit(reader, o, fields))
                    .toList();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to query after query '" + query + "'", e);
        }
    }

    private <T> Hit<T> readHit(SourceReader<T> reader, JsonObject o, String... fields) {
        T source = reader.read(o.getString("_id"), o.getJsonObject("_source"));
        JsonObject highlight = o.getJsonObject("highlight");

        List<Pair<String, List<String>>> highlights = Arrays.stream(fields)
            .map(f -> readHighlight(f, highlight.getJsonArray(f)))
            .filter(Objects::nonNull)
            .toList();

        return new Hit<>(source, highlights);
    }

    private Hit.Pair<String, List<String>> readHighlight(String field, JsonArray highlights) {
        if (highlights == null) {
            return null;
        }
        
        List<String> stringHighlights = highlights.stream().map(this::asString).toList();
        return new Hit.Pair<>(field, stringHighlights);
    }

    private String asString(JsonValue v){
        String s = v.toString();
        if (s.startsWith("\"")) {
            s = s.substring(1);
        }
        if (s.endsWith("\"")) {
            s = s.substring(0, s.length()-1);
        }
        return s;
    }
}
