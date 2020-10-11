package edu.rit.codelanx.data.storage;

import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.state.types.StateType;

import java.util.HashMap;
import java.util.Map;

public class RelativeStorage {

    private final Map<Class<?>, StateStorage<?>> states = new HashMap<>();
    private final DataStorage storage;

    public RelativeStorage(DataStorage storage) {
        this.storage = storage;
        for (State.Type type : StateType.values()) {
            this.states.put(type.getConcreteType(), new StateStorage<>(type, storage));
        }
    }

    public <T extends State> void addState(T state) {
        Class<T> clazz = (Class<T>) state.getClass();
        this.getStateStorage(clazz).addState(state);
    }

    @SuppressWarnings("unchecked") //one Storage<T> per Class<T>
    public <T extends State> StateStorage<T> getStateStorage(Class<? extends T> stateType) {
        State.Type t = StateType.fromClass(stateType);
        if (t == null) {
            throw new IllegalArgumentException("Unknown state type: " + stateType.getName());
        }
        return (StateStorage<T>) this.states.computeIfAbsent(stateType, k -> new StateStorage<T>(t, this.storage));
    }

    public StateStorage<?> getStateStorage(State.Type type) {
        return this.getStateStorage(type.getConcreteType());
    }
}
