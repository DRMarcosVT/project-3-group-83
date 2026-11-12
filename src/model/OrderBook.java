package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * The order book for one stock. Waiting buy orders (bids) and waiting sell
 * orders (asks) are each kept in a TreeMap from price to a first-in,
 * first-out queue of the orders at that price. A new order fills against the
 * best price on the other side first, and against the oldest order within a
 * price.
 */
public class OrderBook {

    /** The largest quantity one order may have. */
    public static final int MAX_QUANTITY = 10000;
    /** The largest price one order may have, in whole dollars. */
    public static final int MAX_PRICE = 10000;

    private final TreeMap<Integer, LinkedQueue<Order>> bids;
    private final TreeMap<Integer, LinkedQueue<Order>> asks;
    private final ArrayList<Trade> trades;
    private int nextOrderId;

    /**
     * Makes a book with no orders and no trades. The first accepted order
     * gets ID 1.
     */
    public OrderBook() {
        bids = new TreeMap<Integer, LinkedQueue<Order>>(Collections
            .reverseOrder());
        asks = new TreeMap<Integer, LinkedQueue<Order>>();
        trades = new ArrayList<Trade>();
        nextOrderId = 1;
    }


    /**
     * Takes one order. It fills against the other side, best price first and
     * oldest first within a price, at each waiting order's price. Any
     * unfilled remainder waits in line at the order's own price.
     *
     * @param traderId
     *            the ID of the trader placing the order, 1 or more
     * @param side
     *            BUY or SELL
     * @param quantity
     *            the number of shares, 1 to MAX_QUANTITY
     * @param price
     *            the limit price in whole dollars, 1 to MAX_PRICE
     * @return the ID given to the order
     * @throws IllegalArgumentException
     *             if any value is out of range, side is null, or the order
     *             could trade with a waiting order from the same trader; the
     *             book, the trade log and the ID counter are unchanged
     */
    public int submit(int traderId, Side side, int quantity, int price) {
        check(traderId, side, quantity, price);

        Order order = new Order(nextOrderId, traderId, side, price, quantity);
        nextOrderId++;

        TreeMap<Integer, LinkedQueue<Order>> otherSide = (side == Side.BUY)
            ? asks
            : bids;
        while (order.getRemaining() > 0 && !otherSide.isEmpty() && matches(side,
            price, otherSide.firstKey())) {
            int bestPrice = otherSide.firstKey();
            LinkedQueue<Order> line = otherSide.get(bestPrice);
            Order waiting = line.peek();
            int shares = Math.min(order.getRemaining(), waiting.getRemaining());
            if (side == Side.BUY) {
                trades.add(new Trade(traderId, waiting.getTrader(), bestPrice,
                    shares));
            }
            else {
                trades.add(new Trade(waiting.getTrader(), traderId, bestPrice,
                    shares));
            }
            order.fill(shares);
            waiting.fill(shares);
            if (waiting.getRemaining() == 0) {
                line.dequeue();
            }
            if (line.isEmpty()) {
                otherSide.remove(bestPrice);
            }
        }

        if (order.getRemaining() > 0) {
            TreeMap<Integer, LinkedQueue<Order>> ownSide = (side == Side.BUY)
                ? bids
                : asks;
            LinkedQueue<Order> line = ownSide.get(price);
            if (line == null) {
                line = new LinkedQueue<Order>();
                ownSide.put(price, line);
            }
            line.enqueue(order);
        }
        return order.getId();
    }


    /**
     * Lists the waiting buy orders.
     *
     * @return a new list, highest price first and oldest first within a
     *         price
     */
    public ArrayList<Order> getBids() {
        return listOf(bids);
    }


    /**
     * Lists the waiting sell orders.
     *
     * @return a new list, lowest price first and oldest first within a price
     */
    public ArrayList<Order> getAsks() {
        return listOf(asks);
    }


    /**
     * Cancels one of a trader's waiting orders. The order leaves its queue at
     * once, and its price leaves the book if no other order waits there.
     * Shares that already traded stay in the trade log.
     *
     * @param traderId
     *            the ID of the trader asking to cancel
     * @param orderId
     *            the ID of the order to cancel
     * @return the cancelled order; its remaining count is the number of
     *         shares cancelled
     * @throws IllegalArgumentException
     *             if the order never existed, is no longer waiting, or
     *             belongs to another trader; the book is unchanged
     */
    public Order cancel(int traderId, int orderId) {
        if (orderId < 1 || orderId >= nextOrderId) {
            throw new IllegalArgumentException("Order #" + orderId
                + " does not exist.");
        }
        Order order = findWaiting(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order #" + orderId
                + " is no longer waiting; it was filled or cancelled.");
        }
        if (order.getTrader() != traderId) {
            throw new IllegalArgumentException("Order #" + orderId
                + " belongs to another trader.");
        }

        TreeMap<Integer, LinkedQueue<Order>> ownSide = (order
            .getSide() == Side.BUY) ? bids : asks;
        LinkedQueue<Order> line = ownSide.get(order.getPrice());
        line.remove(order);
        if (line.isEmpty()) {
            ownSide.remove(order.getPrice());
        }
        return order;
    }


    /**
     * Lists one trader's waiting buy orders.
     *
     * @param traderId
     *            the trader whose orders to list
     * @return a new list in the same order as getBids(), keeping only that
     *         trader's orders
     */
    public ArrayList<Order> getBids(int traderId) {
        return onlyTrader(getBids(), traderId);
    }


    /**
     * Lists one trader's waiting sell orders.
     *
     * @param traderId
     *            the trader whose orders to list
     * @return a new list in the same order as getAsks(), keeping only that
     *         trader's orders
     */
    public ArrayList<Order> getAsks(int traderId) {
        return onlyTrader(getAsks(), traderId);
    }


    /**
     * Lists every trade so far.
     *
     * @return a new list, oldest trade first
     */
    public ArrayList<Trade> getTrades() {
        return new ArrayList<Trade>(trades);
    }


    /**
     * Rejects a bad order before anything in the book changes.
     */
    private void check(int traderId, Side side, int quantity, int price) {
        if (traderId < 1) {
            throw new IllegalArgumentException(
                "Trader ID must be 1 or more, got " + traderId + ".");
        }
        if (side == null) {
            throw new IllegalArgumentException("Side must be BUY or SELL.");
        }
        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be from 1 to "
                + MAX_QUANTITY + ", got " + quantity + ".");
        }
        if (price < 1 || price > MAX_PRICE) {
            throw new IllegalArgumentException("Price must be from 1 to "
                + MAX_PRICE + ", got " + price + ".");
        }

        // Self-trade: the same trader is waiting on the other side at a price
        // this order would trade at. Prices come out best first, so the scan
        // can stop at the first price that does not match.
        TreeMap<Integer, LinkedQueue<Order>> otherSide = (side == Side.BUY)
            ? asks
            : bids;
        for (Map.Entry<Integer, LinkedQueue<Order>> level : otherSide
            .entrySet()) {
            if (!matches(side, price, level.getKey())) {
                break;
            }
            for (Order waiting : level.getValue().toList()) {
                if (waiting.getTrader() == traderId) {
                    throw new IllegalArgumentException("Trader " + traderId
                        + " already has order #" + waiting.getId()
                        + " waiting on the other side at a price this order"
                        + " would trade at.");
                }
            }
        }
    }


    /**
     * Says whether a new order at its limit price can trade with a waiting
     * order at the other side's price.
     */
    private boolean matches(Side side, int limitPrice, int otherPrice) {
        if (side == Side.BUY) {
            return otherPrice <= limitPrice;
        }
        return otherPrice >= limitPrice;
    }


    /**
     * Finds a waiting order by ID on either side, or returns null.
     */
    private Order findWaiting(int orderId) {
        for (Order order : getBids()) {
            if (order.getId() == orderId) {
                return order;
            }
        }
        for (Order order : getAsks()) {
            if (order.getId() == orderId) {
                return order;
            }
        }
        return null;
    }


    /**
     * Keeps only one trader's orders, in the same order.
     */
    private ArrayList<Order> onlyTrader(ArrayList<Order> orders,
        int traderId) {
        ArrayList<Order> mine = new ArrayList<Order>();
        for (Order order : orders) {
            if (order.getTrader() == traderId) {
                mine.add(order);
            }
        }
        return mine;
    }


    /**
     * Copies one side of the book into a list, best price first.
     */
    private ArrayList<Order> listOf(
        TreeMap<Integer, LinkedQueue<Order>> oneSide) {
        ArrayList<Order> orders = new ArrayList<Order>();
        for (LinkedQueue<Order> line : oneSide.values()) {
            orders.addAll(line.toList());
        }
        return orders;
    }
}
