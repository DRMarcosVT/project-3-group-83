package model;

/**
 * The two sides of the order book.
 */
public enum Side {
    /** An order to buy shares; it waits among the bids. */
    BUY,
    /** An order to sell shares; it waits among the asks. */
    SELL
}
