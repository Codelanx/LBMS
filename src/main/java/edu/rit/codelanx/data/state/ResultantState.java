package edu.rit.codelanx.data.state;

/**
 * Represents states which are the result of an action. This
 * is more akin to "logging" actions (e.g. checkouts,
 * transactions, visits, etc).
 *
 * @author  sja9291 Spencer Alderman
 */
public abstract class ResultantState implements State {

    protected final long id;

    public ResultantState(long id) {
        this.id = id;
    }

    @Override
    public long getID() {
        return this.id;
    }
}
