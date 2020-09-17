package edu.rit.codelanx.data;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class DataFacade implements DataStorage {

    private final Map<Class<? extends State>, Map<Long, State>> data = new HashMap<>();

    public DataFacade() {
    }

    //warns you if you try to modify the elements
    @SuppressWarnings("unchecked") //we control this map, and the (bounded) elements within it
    private <R extends State> Map<Long, R> getStates(Class<R> type) {
        return (Map<Long, R>) this.data.computeIfAbsent(type, k -> new HashMap<>());
    }

    //whilst very similar to the above, this method will warn you if you try to retrieve from it
    @SuppressWarnings("unchecked")
    private Map<Long, ? super State> modStates(Class<? extends State> type) {
        return (Map<Long, ? super State>) this.getStates(type);
    }

    @Override
    public <R extends State> Stream<? extends R> ofLoaded(Class<R> type) {
        return this.getStates(type).values().stream();
    }

    @Override
    public void add(State state) {
        this.modStates(state.getClass()).put(state.getID(), state);
    }

    public <R extends State> R find(long id) {
        //TODO: Querying
        return null;
    }
}
