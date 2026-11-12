package model;

/**
 * One trader's order: its ID, who placed it, which side, what price, and how
 * many shares are still unfilled. Only OrderBook makes and fills orders, so
 * the constructor and fill are package-private and receive values that submit
 * has already checked.
 */
public class Order {

    private final int id;
    private final int trader;
    private final Side side;
    private final int price;
    private int remaining;

    /**
     * Makes an order with every share unfilled.
     *
     * @param id
     *            the order ID issued by the book
     * @param trader
     *            the ID of the trader who placed the order
     * @param side
     *            BUY or SELL
     * @param price
     *            the limit price in whole dollars
     * @param quantity
     *            the number of shares ordered
     */
    Order(int id, int trader, Side side, int price, int quantity) {
        this.id = id;
        this.trader = trader;
        this.side = side;
        this.price = price;
        this.remaining = quantity;
    }


    /**
     * Returns the order ID.
     *
     * @return the ID the book gave this order
     */
    public int getId() {
        return id;
    }


    /**
     * Returns who placed the order.
     *
     * @return the trader ID
     */
    public int getTrader() {
        return trader;
    }


    /**
     * Returns the side of the book the order is on.
     *
     * @return BUY or SELL
     */
    public Side getSide() {
        return side;
    }


    /**
     * Returns the limit price.
     *
     * @return the price in whole dollars
     */
    public int getPrice() {
        return price;
    }


    /**
     * Returns how many shares are still unfilled.
     *
     * @return the unfilled share count
     */
    public int getRemaining() {
        return remaining;
    }


    /**
     * Lowers the unfilled share count after a trade.
     *
     * @param shares
     *            the number of shares that traded, at most the unfilled count
     */
    void fill(int shares) {
        remaining -= shares;
    }
}
