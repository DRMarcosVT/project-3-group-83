# Preliminary Spec

Project Name: No Cutting!

## Class design, split by role

Seven classes in three packages named after the roles: `model`, `view`, `controller`. Each class sits in the package of its role, so no class spans two.

| Class | Role | Responsibility |
|---|---|---|
| `LinkedQueue<T>` | Model | Holds items in arrival order; first in, first out. |
| `Side` | Model | Names the two sides of the book: BUY and SELL. |
| `Order` | Model | One trader's order: who, which side, what price, how many shares are still unfilled. |
| `Trade` | Model | Record of one fill: buyer, seller, price, shares. |
| `OrderBook` | Model | Keeps both sides of the book, checks and matches each new order, records the trades. |
| `ConsoleView` | View | Prints the book, the trade log and messages; reads what the user types. |
| `OrderController` | Controller | Turns typed text into a `submit` call and tells the view what to show. |

`LinkedQueue` also has a private nested class `Node<T>` (model) that links one item to the next.

## System diagram

Arrows are labelled with what moves. The view never calls `OrderBook` and the model never calls the view; everything passes through `OrderController`.

```
 VIEW (package view)
 +--------------------------------------------------------------------+
 | ConsoleView                                                        |
 |   ask   showBook   showTrades   showError                          |
 |   reads Order and Trade through their getters only                 |
 +--------------------------------------------------------------------+
      |                             ^
      | typed line (String)         | ArrayList<Order> bids, asks
      |                             | ArrayList<Trade> trades
      v                             | error message (String)
 ===================== view / controller boundary =====================
 CONTROLLER (package controller)
 +--------------------------------------------------------------------+
 | OrderController                                                    |
 |   main   run                                                       |
 +--------------------------------------------------------------------+
      |                             ^
      | submit(trader, side,        | ArrayList<Order> (getBids, getAsks)
      |        quantity, price)     | ArrayList<Trade> (getTrades)
      v                             | IllegalArgumentException (rejected)
 ==================== controller / model boundary =====================
 MODEL (package model)
 +--------------------------------------------------------------------+
 | OrderBook                                                          |
 |   submit   getBids   getAsks   getTrades                           |
 |   bids, asks : TreeMap<Integer, LinkedQueue<Order>>                |
 |   trades     : ArrayList<Trade>                                    |
 |      |                                          |                  |
 |      | one queue per price                      | one per fill     |
 |      v                                          v                  |
 | +----------------------------------+      +---------------------+  |
 | | LinkedQueue<Order>               |      | Trade               |  |
 | |   enqueue  dequeue  peek         |      |   buyer, seller,    |  |
 | |   isEmpty  toList                |      |   price, quantity   |  |
 | +----------------------------------+      +---------------------+  |
 |      |                                                             |
 |      | holds, oldest at the head                                   |
 |      v                                                             |
 | +----------------------------------+                               |
 | | Order                            |                               |
 | |   trader, side (Side: BUY, SELL),|                               |
 | |   price, remaining               |                               |
 | +----------------------------------+                               |
 +--------------------------------------------------------------------+
```

## The model / view boundary

What the view knows: the classes `Order`, `Trade` and `Side`, and their getters. `showBook` and `showTrades` receive lists that are already in display order, so the view prints them top to bottom.

What the view deliberately does not know: `OrderBook`, the TreeMaps and `LinkedQueue`; how orders get matched; what makes an order invalid. It prints whatever error message it is handed. It holds no reference to the book and never calls `submit`. It cannot change an `Order` either: the one method that changes an order, `fill`, is package-private inside `model`.

The model knows nothing about the other two packages. No class in `model` uses `Scanner` or `System.out`.

How the view learns the model changed: the controller tells it. After every `submit` that succeeds, `run` calls `showBook(book.getBids(), book.getAsks())` and `showTrades(book.getTrades())` with fresh lists. After a rejected `submit` it calls `showError` only, since the book did not change.

Why this fits: the book changes only inside `submit`, and `run` is the only caller of `submit`, so the controller already knows the moment of every change. There is one view. An observer would add a listener interface and a list of listeners to tell the controller about a change it caused itself. If we build the GUI stretch goal, a button handler makes the same three calls.

## Data & state

Where the Queue lives: inside `OrderBook`, as the values of the two TreeMaps. There is one `LinkedQueue<Order>` for each price that has at least one waiting order. It holds the `Order` objects waiting at that price, oldest at the head.

| Class | Field | Type | Changes after construction? |
|---|---|---|---|
| `LinkedQueue<T>` | `head` | `Node<T>` | Yes, on `dequeue` and on the first `enqueue` |
| | `tail` | `Node<T>` | Yes, on every `enqueue` |
| `Node<T>` | `data` | `T` | No |
| | `next` | `Node<T>` | Yes, when the next item is enqueued |
| `Order` | `trader` | `String` | No |
| | `side` | `Side` | No |
| | `price` | `int` (whole dollars) | No |
| | `remaining` | `int` (shares still unfilled) | Yes, goes down on each fill |
| `Trade` | `buyer`, `seller` | `String` | No |
| | `price`, `quantity` | `int` | No |
| `OrderBook` | `bids` | `TreeMap<Integer, LinkedQueue<Order>>` | The map is made once; its contents change on `submit` |
| | `asks` | `TreeMap<Integer, LinkedQueue<Order>>` | Same as `bids` |
| | `trades` | `ArrayList<Trade>` | The list is made once; it grows on each fill |
| | `MAX_QUANTITY`, `MAX_PRICE` | `static final int`, both 10,000 | No |
| `ConsoleView` | `in` | `Scanner` | No |
| `OrderController` | `book` | `OrderBook` | No |
| | `view` | `ConsoleView` | No |

`Side` is an enum with two values and no fields. `bids` is sorted highest price first (`Collections.reverseOrder()`) and `asks` lowest first, so `firstKey()` is the best price on both sides.

## Method signatures

### Model: `LinkedQueue<T>`

| Signature | Behavior | On bad input |
|---|---|---|
| `LinkedQueue()` | Makes an empty queue. | Takes no input. |
| `void enqueue(T item)` | Adds `item` at the tail. | `null`: throws `IllegalArgumentException`; queue unchanged. |
| `T dequeue()` | Removes and returns the head item. | Empty queue: throws `NoSuchElementException`. |
| `T peek()` | Returns the head item and leaves it in place. | Empty queue: throws `NoSuchElementException`. |
| `boolean isEmpty()` | Returns true when the queue holds no items. | Takes no input. |
| `ArrayList<T> toList()` | Returns a new list of the items, head first; the queue is unchanged. | Takes no input; an empty queue gives an empty list. |

### Model: `OrderBook`, `Order`, `Trade`

| Signature | Behavior | On bad input |
|---|---|---|
| `OrderBook()` | Makes a book with no orders and no trades. | Takes no input. |
| `void submit(String trader, Side side, int quantity, int price)` | Fills the order against the other side, best price first and oldest first within a price; the unfilled remainder waits in line. | Throws `IllegalArgumentException` with a message naming the problem; the book and trade log are unchanged. |
| `ArrayList<Order> getBids()` | Returns a new list of waiting buy orders, highest price first, oldest first within a price. | Takes no input; an empty side gives an empty list. |
| `ArrayList<Order> getAsks()` | Returns a new list of waiting sell orders, lowest price first, oldest first within a price. | Takes no input; an empty side gives an empty list. |
| `ArrayList<Trade> getTrades()` | Returns a new list of every trade so far, oldest first. | Takes no input; no trades gives an empty list. |
| `Order`: `String getTrader()`, `Side getSide()`, `int getPrice()`, `int getRemaining()` | Each returns that field. | Takes no input. |
| `Trade`: `String getBuyer()`, `String getSeller()`, `int getPrice()`, `int getQuantity()` | Each returns that field. | Takes no input. |

Only `OrderBook` makes `Order` and `Trade` objects, so their constructors and `Order.fill(int shares)` are package-private and receive values `submit` has already checked.

How `submit` works:

1. Check the input (see Where validation lives). If a check fails, throw before touching the book.
2. Find the best price on the other side: the lowest ask for a BUY, the highest bid for a SELL. It matches when the ask is at or below the buyer's price, or the bid is at or above the seller's price.
3. While the new order has shares left and the best price matches: `peek` the oldest order at that price, trade the smaller of the two share counts at the waiting order's price, add a `Trade` to `trades`, and lower `remaining` on both orders. `dequeue` the waiting order when it reaches 0, and remove the price from the TreeMap when its queue is empty.
4. If the new order still has shares left, `enqueue` it at its own price on its own side, making a new queue if the price is new.

### Controller: `OrderController`

| Signature | Behavior | On bad input |
|---|---|---|
| `OrderController(OrderBook book, ConsoleView view)` | Stores the book and the view it connects. | No check: `main` is the only caller and always passes new objects. |
| `void run()` | Repeats: asks for trader, side, quantity and price, calls `submit`, shows the book and the trade log. Ends when the user types `quit` at the trader prompt. | Calls `showError` with a message and asks for the order again; the book is unchanged. |
| `static void main(String[] args)` | Makes one `OrderBook`, one `ConsoleView` and one `OrderController`, then calls `run`. | Ignores `args`. |

### View: `ConsoleView`

| Signature | Behavior |
|---|---|
| `ConsoleView()` | Opens a `Scanner` on `System.in`. |
| `String ask(String prompt)` | Prints `prompt` and returns the line the user types. |
| `void showBook(ArrayList<Order> bids, ArrayList<Order> asks)` | Prints both waiting lines, one order per row. |
| `void showTrades(ArrayList<Trade> trades)` | Prints the trade log. |
| `void showError(String message)` | Prints the message. |

## Where validation lives

A rejected input never changes the book. For every row below the user sees a message and is asked for the order again.

| Bad input from our scope | Caught by | What happens | Why there |
|---|---|---|---|
| Quantity or price of 0 | `OrderBook.submit` | Throws `IllegalArgumentException`; `run` catches it and calls `showError`. | `submit` is the only way into the book, so one method guards it for the console, a GUI and JUnit. |
| Negative quantity or price | `OrderBook.submit` | Same. | Same. |
| Quantity or price above the max (10,000) | `OrderBook.submit` | Same. | Same. A number too big for an `int` is caught earlier, as text. |
| Blank trader name | `OrderBook.submit` | Same. | Same. |
| Self-trade: the trader already has a waiting order on the other side at a price the new order matches | `OrderBook.submit` | Same. Checked before any matching starts. | Only the book knows which orders are waiting. |
| Text ("ten") for quantity or price | `OrderController.run`, when `Integer.parseInt` fails | `run` calls `showError`; `submit` is never called. | `submit` takes an `int`, so text has to become a number before the model is called. Reading typed text is the controller's job. |
| A side other than buy/sell | `OrderController.run`, when `Side.valueOf` fails | `run` calls `showError`; `submit` is never called. | `Side` is an enum, so the model cannot be handed a third side. |
| Cancelling a filled or nonexistent order ID (stretch) | `OrderBook.cancel(int orderId)`, added only if we build cancellation | Throws `IllegalArgumentException`; `run` catches it and calls `showError`. | Only the book knows which IDs are still waiting. A filled order has left its queue, so both cases look the same. |

## Test plan

`LinkedQueueTest` and `OrderBookTest` are JUnit classes in the `model` package. They import nothing from `view` or `controller`, use no `Scanner` and print nothing: every expected result below is a return value or an exception. Methods that take no input have no bad input, so their second case is an empty queue, an empty book, or a caller changing the returned list.

| Method | Normal case and expected result | Bad-input case and expected result |
|---|---|---|
| `enqueue` | Enqueue "a" then "b". `toList()` is [a, b]; `isEmpty()` is false. | `enqueue(null)` throws `IllegalArgumentException`; `toList()` is unchanged. |
| `dequeue` | After enqueueing "a" then "b", `dequeue()` returns "a", then "b"; `isEmpty()` is true. | On an empty queue, throws `NoSuchElementException`. |
| `peek` | After enqueueing "a" then "b", `peek()` returns "a" twice; `toList()` is still [a, b]. | On an empty queue, throws `NoSuchElementException`. |
| `isEmpty` | True on a new queue; false after one `enqueue`. | After one `enqueue` and one `dequeue`, true again. |
| `toList` | After "a", "b", "c", returns [a, b, c]; `dequeue()` still returns "a". | On an empty queue, returns an empty list. |
| `submit` (order waits) | On an empty book, alice SELL 100 at 25. `getAsks()` has one order: alice, 100 left, price 25. `getTrades()` is empty. | Each of quantity 0, -5, 10,001; price 0, -5, 10,001; trader "" or "  "; side `null` throws `IllegalArgumentException`. `getBids()`, `getAsks()` and `getTrades()` are the same as before the call. |
| `submit` (fill, remainder waits) | alice SELL 100 at 25, then bob BUY 150 at 30. One trade: buyer bob, seller alice, 100 shares at 25. `getAsks()` is empty. `getBids()` has bob, 50 left, price 30. | alice SELL 100 at 25, then alice BUY 10 at 25 throws `IllegalArgumentException`. `getAsks()` still has alice, 100 left; `getTrades()` is empty. |
| `submit` (price first, then oldest first) | alice SELL 50 at 26, carol SELL 50 at 25, dave SELL 50 at 25, then bob BUY 120 at 26. Trades in order: carol 50 at 25, dave 50 at 25, alice 20 at 26. `getAsks()` has alice, 30 left. | Covered by the two rows above. |
| `getBids` | alice BUY at 24, bob BUY at 26, carol BUY at 26. Returns bob, carol, alice. | On an empty book, returns an empty list. Clearing the returned list leaves the next `getBids()` unchanged. |
| `getAsks` | alice SELL at 27, bob SELL at 25, carol SELL at 25. Returns bob, carol, alice. | On an empty book, returns an empty list. Clearing the returned list leaves the next `getAsks()` unchanged. |
| `getTrades` | After the price-first case above, returns its three trades, oldest first. | On a new book, returns an empty list. Clearing the returned list leaves the next `getTrades()` unchanged. |
| `Order` and `Trade` getters | Checked by every `submit` case above that reads a trader, price or share count. | They take no input. |

## Division of work

| Piece | Owner | Builds |
|---|---|---|
| Model | [name] | `LinkedQueue`, `Side`, `Order`, `Trade`, `OrderBook`, `LinkedQueueTest`, `OrderBookTest` |
| View | [name] | `ConsoleView` |
| Controller | [name] | `OrderController` |

How we integrate:

1. The signatures in this spec are the contract. Changing one needs all three of us to agree first.
2. We share one GitHub repo. Each owner works on their own branch and merges into `main` once their code compiles and their tests pass.
3. The view and controller owners can start right away: `ConsoleView` needs only `Order` and `Trade`, which the model owner pushes first as fields and getters.
4. First join: the controller owner, [name], merges all three pieces on [date], as soon as `LinkedQueueTest` and the first `OrderBookTest` case pass. They run `main` and type in the three `submit` normal cases from the test plan by hand, checking the printed book against the expected results.

## Revised scope

| What changed since the scope doc | Why | Came from | Cost or saving in the build |
|---|---|---|---|
| Prices are whole dollars, stored as an `int`. The scope did not say. | An `int` works as a TreeMap key with no rounding, and "10.5" is rejected by the same check as "ten". | GenAI (Claude) | Saves a decimal-format check and rounding code. Costs the user the ability to enter cents. |
| Self-trades are blocked by rejecting the new order when the trader has a waiting order on the other side at a matching price. The scope said "blocked" and no more. | Rejecting before matching starts keeps our rule that a rejected input never changes the book. | GenAI (Claude) | Costs one loop over the matching prices in `submit`. |
| `LinkedQueue` gets a fifth method, `toList()`. | The view has to show every waiting order, and dequeueing them to read them would empty the book. The self-trade check reads the queue the same way. | GenAI (Claude) | Costs one short method and one test pair. |
| The max is 10,000 for both quantity and price. The scope said "a set max". | 10,000 times 10,000 still fits in an `int`, so the volume stretch goal cannot overflow. | GenAI (Claude) | Costs two constants. |
| A trade happens at the waiting order's price. The scope did not say. | The waiting order committed to its price first, the same reasoning as our FIFO rule. | GenAI (Claude) | Costs nothing: one line in `submit`. |
| MVP orders have no ID. Cancellation stays a stretch goal, and so does our open question about removing an order from the middle of a queue. | Nothing in the MVP looks an order up by ID. | GenAI (Claude) | Saves an ID counter and a remove method on `LinkedQueue`. |

## Work Attribution

| Section | Team member(s) | Contribution (how) |
|---|---|---|
| Class design | [name] | Drafted with GenAI (Claude); [who checked or revised it, and how] |
| System diagram | [name] | Drafted with GenAI (Claude); [who checked or revised it, and how] |
| Model / view boundary | [name] | Drafted with GenAI (Claude); [who checked or revised it, and how] |
| Data & state | [name] | Drafted with GenAI (Claude); [who checked or revised it, and how] |
| Method signatures | [name] | Drafted with GenAI (Claude); [who checked or revised it, and how] |
| Where validation lives | [name] | Drafted with GenAI (Claude); [who checked or revised it, and how] |
| Test plan | [name] | Drafted with GenAI (Claude); [who checked or revised it, and how] |
| Division of work | [name] | Drafted with GenAI (Claude); [who checked or revised it, and how] |
| Revised scope | [name] | Drafted with GenAI (Claude); [who checked or revised it, and how] |

## GenAI note

We gave Claude our scope document and the Deliverable 2 rubric and asked it for a minimal first draft of this spec. Claude wrote the first draft of every section, including the system diagram. The design choices that came from Claude are the six rows in Revised scope, plus three more: putting every value check in `OrderBook.submit`, having the controller tell the view when to redraw, and splitting the classes into three packages. [Add what we asked Claude about the model/view boundary and what we changed after reviewing the draft.]
