package edu.rit.codelanx.network.io;

/**
 * Interface represents either response or request
 * @param <T> type of message
 * @author sja9291  Spencer Alderman
 */
public interface Message<T> {
    /**
     * request for message data
     * @return request/response content when called
     */
    public T getData();
}
