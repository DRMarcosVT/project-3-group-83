package model;

/**
 * A record of one fill: who bought, who sold, at what price, and how many
 * shares. Only OrderBook makes trades, so the constructor is package-private.
 */
public class Trade {

    private final int buyer;
    private final int seller;
    private final int price;
    private final int quantity;

    /**
     * Makes a trade record.
     *
     * @param buyer
     *            the buying trader's ID
     * @param seller
     *            the selling trader's ID
     * @param price
     *            the price in whole dollars
     * @param quantity
     *            the number of shares that changed hands
     */
    Trade(int buyer, int seller, int price, int quantity) {
        this.buyer = buyer;
        this.seller = seller;
        this.price = price;
        this.quantity = quantity;
    }


    /**
     * Returns who bought.
     *
     * @return the buying trader's ID
     */
    public int getBuyer() {
        return buyer;
    }


    /**
     * Returns who sold.
     *
     * @return the selling trader's ID
     */
    public int getSeller() {
        return seller;
    }


    /**
     * Returns the price the shares traded at.
     *
     * @return the price in whole dollars
     */
    public int getPrice() {
        return price;
    }


    /**
     * Returns how many shares changed hands.
     *
     * @return the share count
     */
    public int getQuantity() {
        return quantity;
    }
}
