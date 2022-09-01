package dk.stonemountain.business.domain;

import java.util.List;

public class Hit<T> {
    public static class Pair<S, T> {
        public final S first;
        public final T second;

        public Pair(S first, T second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public String toString() {
            return "Pair [first=" + first + ", second=" + second + "]";
        }
    }

    public Hit(T source, List<Pair<String, List<String>>> highlights) {
        this.source = source;
        this.highlights = highlights;
    }

    public final T source;
    public final List<Pair<String, List<String>>> highlights;
    
    @Override
    public String toString() {
        return "Hit [highlights=" + highlights + ", source=" + source + "]";
    }
}
