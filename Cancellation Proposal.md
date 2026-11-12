# Proposal: Order Cancellation and Integer IDs

Project: No Cutting!
Status: agreed by Marcos on November 11, 2026, and applied to `Preliminary Spec.md`. Riya and Raghul have not yet reviewed it.

## Summary

A trader can cancel one of their own waiting orders by its order ID. To make that possible, traders and orders both get `int` IDs, and the book refuses a cancel from anyone other than the order's owner.

The work splits into two parts:

1. **Integer IDs** (traders and orders). This changes signatures the spec already fixed (`submit`, `Order`, `Trade`), so it lands in the Deliverable 3 prototype and is reported in the README's Reflection as a change from the spec. No Java code exists yet on any branch, so the switch costs spec edits only.
2. **Cancellation** itself. This is the Deliverable 4 change feature. Because part 1 already happened, its prediction stays short (see "Prediction for Next Steps").

Keeping part 1 out of the change feature matters for the D4 comparison: if the ID switch were counted as part of cancellation, the prediction would say the feature touched every class, and the comparison would show nothing about the model/view line.

## Part 1: Integer IDs

### Trader IDs

| Decision | Reason | Alternative considered |
|---|---|---|
| A trader ID is a positive primitive `int` the user picks. No registry, no names. | Two traders can no longer collide on spelling ("Alice" vs "alice"), and `int` compares with `==` without the `String` reference-equality trap. A registry adds a class and a lookup the view would need passed to it. | Book-issued IDs with names (`int addTrader(String name)`, `String getTraderName(int id)`). Worth it only if we want names on screen; the view would then need the names handed to it by the controller, since it holds no reference to the book. |
| Fields stay primitive `int`, never `Integer`. | `Integer` objects compared with `==` give false for equal values above 127, which reintroduces the equality bug the switch is meant to remove. | If an ID is ever a `HashMap` key, compare with `.equals`. |
| The model never stores "who is typing". The controller holds `currentTrader`; every model method that needs a trader takes `int traderId` as a parameter. | The book stays testable in JUnit with any number of traders and no session state. "Who am I" is input handling, which the spec gives to the controller. | Storing the current trader in `OrderBook`: ties the model to a single console user and breaks the generated-orders idea, where many traders act in one run. |
| The controller asks for a trader ID at startup, and a `switch <id>` command changes it so the user can act as another trader. | "My orders" needs an ID to compare against, so it is set before the first command. `switch` is required: self-trades are blocked (spec, Where validation lives), so a user who could never change ID could never get a fill when testing by hand. | One ID per run with no `switch`: only workable once generated orders supply the other side. |

Ownership is checked against whatever ID the controller passes in. `switch 3` followed by a cancel of trader 3's order succeeds. This is a rule of the simulation, with no login or password behind it; say so in the README so the TA does not read it as a security claim.

### Order IDs

| Decision | Reason |
|---|---|
| `OrderBook` holds `int nextOrderId`, starting at 1, and gives each accepted order the next value. Rejected orders get no ID. | IDs are unique for the life of the book, and a gap-free sequence of accepted orders is easy to check in tests. |
| `submit` returns the new order's ID (`int` in place of `void`). | The controller can tell the user which number to cancel later, without searching the book. |
| `Order` gets `int id` and `getId()`. | The view prints it beside each waiting order; without it, the user has no way to name the order to cancel. |

The counter also improves an error message for free. Today's spec says a filled order and a nonexistent one "look the same". With a counter, an ID below 1 or at or above `nextOrderId` never existed, and any other ID that is not in the book was filled or cancelled.

### Spec rows that change in Part 1

| Spec location | Change |
|---|---|
| `Order.trader`, `getTrader()` | `String` to `int` |
| `Trade.buyer`, `Trade.seller`, getters | `String` to `int` |
| `Order` | add `int id`, `getId()` |
| `OrderBook` | add `int nextOrderId` |
| `submit(String trader, ...)` | becomes `int submit(int traderId, Side side, int quantity, int price)` |
| Validation row "Blank trader name" | becomes "Trader ID 0 or negative", caught in `submit` |
| New validation row | Text typed as a trader ID: caught in `run` by `Integer.parseInt`, like quantity and price |
| Self-trade check | compares `int`s with `==` |
| Test plan | names become IDs (alice = 1, bob = 2, carol = 3, dave = 4); every `submit` case also checks the returned order ID |
| `ConsoleView.showBook`, `showTrades` | print trader IDs and the order ID column |
| `OrderController.run` | asks for a trader ID once, then reads `buy`, `sell`, `switch <id>` and `quit` as commands, in place of asking for trader, side, quantity and price each time |

## Part 2: Cancellation

### Cancel is a method call

`OrderBook` gets `Order cancel(int traderId, int orderId)`. It finds the order, checks ownership, removes it, and returns it so the controller can report how many shares were left unfilled.

We considered making a cancel its own kind of order, held in its own queue and processed like matching. In this program every command finishes inside one call: `run` calls the model, the model succeeds or throws, and `run` redraws. A cancel therefore never waits: by the time the next command is read, it has either removed the order or been rejected. A cancel queue would be empty after every command, and a "pending" state could never be observed. A `Cancel` subclass of `Order` would also carry a side, price and remaining shares that mean nothing for a cancel, and `getBids`/`getAsks` would have to filter it out. The information a cancel needs (who is asking and which order) fits in the method's two parameters.

A cancel queue would become useful if orders ever arrived on a separate thread or in timed batches, so that a cancel could race a fill. Neither is planned.

Cancels are not logged. The trade log records fills; a cancel's effect is visible in the redrawn book, and the controller prints a confirmation. If we later want a cancel history, append a small record (trader, order ID, shares left) to a list on success only; since it is written only after the cancel has happened, it needs no pending state either.

Java's garbage collector frees an `Order` once nothing refers to it, but it never takes anything out of a queue. Our code has to drop the queue's reference first. If we ever add a `HashMap<Integer, Order>` index of live orders, cancel and fill must both remove the entry, or a gone order will look live to the next lookup.

### Removal: eager (agreed) or lazy

Five places read the queues today:

1. `submit`'s best-price check (`firstKey()` of the other side)
2. `submit`'s fill loop (`peek` at the head)
3. the self-trade scan (`toList()`)
4. `getBids()` (`toList()`)
5. `getAsks()` (`toList()`)

**Eager removal (agreed).** `cancel` removes the order from its queue right away with a new `LinkedQueue.remove(T item)`, and removes the price from the TreeMap if that empties its queue. Every order in the book stays live, so none of the five readers change.

**Lazy removal.** `cancel` sets a `cancelled` flag on the order and leaves it in place; the fill loop dequeues flagged orders when they reach the head. `LinkedQueue` stays unchanged, but all five readers must skip flagged orders, and each one we forget is a bug. For example, if the self-trade scan does not skip them, trader 1 cancels a waiting SELL at 25, sends a BUY at 25, and is wrongly rejected as a self-trade. A price level holding only cancelled orders also stays in the TreeMap, so `firstKey()` reports a best price nobody is offering.

Eager removal adds one queue method and its tests; lazy removal touches five methods and adds a flag every reader must remember. We chose eager removal.

The cost: `remove(T item)` takes an item from the middle of the queue, which the scope said nothing would do ("nothing joins the front or leaves the back", Scope line 30). Record this in Reflection/Next Steps as a change the feature forced on the queue. The queue still never adds at the front or removes from the back, and arrival order among the remaining items is preserved, so FIFO fills are unaffected.

### Finding the order

`cancel` scans both TreeMaps' queues for the ID. The book is small in a console session, and a scan needs no extra state to keep in sync. If generated orders make the book large enough for the scan to be slow, add the `HashMap<Integer, Order>` index described above; each `Order` already knows its side and price, so the index leads straight to the right queue.

### Behavior of `cancel(int traderId, int orderId)`

1. If `orderId < 1` or `orderId >= nextOrderId`: throw `IllegalArgumentException` ("order #N does not exist").
2. Scan for the order. If not found: throw ("order #N is no longer waiting; it was filled or cancelled").
3. If `order.getTrader() != traderId`: throw ("order #N belongs to another trader").
4. Remove it from its queue (`LinkedQueue.remove`); if the queue is now empty, remove its price from the TreeMap.
5. Return the order. Its `getRemaining()` is the number of shares cancelled.

Checks 1–3 run before anything changes, so a rejected cancel never changes the book, matching the rule for `submit`. Cancelling a partly filled order cancels only the unfilled shares; its earlier trades stay in `getTrades()`.

### "My orders"

`OrderBook` gets `getBids(int traderId)` and `getAsks(int traderId)`: the same lists as today's getters, filtered to one trader. The controller passes them to the existing `showBook`, so the view needs no new method for this. Filtering lives in the model because it reads the book; the controller only chooses whose orders to ask for.

### Controller commands

`run` reads commands through the existing `ask`. The Deliverable 3 prototype already has `buy`, `sell`, `switch` and `quit`, because Part 1 needs `currentTrader`. Cancellation adds `cancel` and `mine`, and the confirmation lines.

| Command | Lands in | Effect |
|---|---|---|
| `buy` / `sell` | Deliverable 3 | asks quantity and price, calls `submit(currentTrader, ...)`, redraws. Deliverable 4 adds the line "order #N placed". |
| `switch <traderId>` | Deliverable 3 | changes `currentTrader` after checking it is a positive integer |
| `quit` | Deliverable 3 | ends the loop |
| `cancel <orderId>` | Deliverable 4 | calls `cancel(currentTrader, orderId)`, prints "cancelled #N, S shares unfilled", redraws |
| `mine` | Deliverable 4 | calls `showBook(book.getBids(currentTrader), book.getAsks(currentTrader))` |

Confirmations need a non-error output, so `ConsoleView` gets `showMessage(String message)`. Without it, the controller would have to print confirmations through `showError`.

### Validation added by Part 2

| Bad input | Caught by | What happens | Why there |
|---|---|---|---|
| Cancel ID that never existed (`< 1` or `>= nextOrderId`) | `OrderBook.cancel` | Throws `IllegalArgumentException`; `run` calls `showError` | Only the book holds the counter. |
| Cancel ID that was filled or already cancelled | `OrderBook.cancel` | Same | Only the book knows what is waiting. |
| Cancel someone else's order | `OrderBook.cancel` | Same; the order stays | Only the book knows each order's owner; putting the check in the model guards the console, a GUI and JUnit alike. |
| Text typed as an order ID or trader ID | `OrderController.run`, `Integer.parseInt` fails | `showError`; the model is never called | Same reason as quantity and price in the spec: turning text into numbers is the controller's job. |
| `switch` to 0 or a negative ID | `OrderController.run` | `showError`; `currentTrader` unchanged | `currentTrader` is controller state, and `submit` would reject the ID anyway. |
| Unknown command | `OrderController.run` | `showError` listing the commands | Parsing commands is the controller's job. |

### Tests

`LinkedQueueTest`, for `remove(T item)`:

| Case | Expected |
|---|---|
| Queue a, b, c; remove b | `toList()` is [a, c]; returns true |
| Remove the head (a from a, b, c) | `peek()` is b |
| Remove the tail, then enqueue d | `toList()` is [a, b, d]. This catches a stale `tail` pointer: without the update, d never appears. |
| Remove the only item | `isEmpty()` is true; a following `enqueue` works |
| Remove an item not in the queue | returns false; queue unchanged |
| `remove(null)` | throws `IllegalArgumentException`, matching `enqueue(null)` |

`OrderBookTest`, for `cancel` and the filtered getters:

| Case | Expected |
|---|---|
| Trader 1 SELL 100 at 25 (order 1); `cancel(1, 1)` | returns the order with 100 remaining; `getAsks()` empty |
| Cancel the only order at a price, then trader 2 BUY 10 at 25 | the buy rests in `getBids()`; no trade (the emptied price level is gone) |
| Trader 1 SELL 100 at 25, trader 2 BUY 40 at 25, `cancel(1, 1)` | returns 60 remaining; the 40-share trade stays in `getTrades()` |
| Two orders at one price, cancel the older | the newer one fills first on the next crossing order |
| `cancel(2, 1)` on trader 1's order | throws; order still in `getAsks()` |
| `cancel(1, 0)`, `cancel(1, 99)` on a new book | throws "does not exist" |
| Cancel an order that fully filled | throws "no longer waiting" |
| Cancel the same order twice | second call throws "no longer waiting" |
| `getBids(1)` with orders from traders 1 and 2 | only trader 1's, in the same order as `getBids()` |
| `submit` returns 1, 2, 3 for three accepted orders; a rejected one in between does not use up a number | IDs are 1, 2, 3 |

## Prediction for Next Steps

This assumes Part 1 has landed in the D3 prototype.

| Class | Touched? | Methods |
|---|---|---|
| `LinkedQueue` | Yes | add `boolean remove(T item)` |
| `OrderBook` | Yes | add `Order cancel(int traderId, int orderId)`, `getBids(int traderId)`, `getAsks(int traderId)` |
| `OrderController` | Yes | `run` reads `cancel <orderId>` and `mine`, and calls `showMessage` after `buy`, `sell` and `cancel` |
| `ConsoleView` | Yes | add `showMessage(String message)` |
| `Order` | No | already has `id` from Part 1 |
| `Trade` | No | cancels create no trades |
| `Side` | No | |
| `OrderBook.submit`, `getBids()`, `getAsks()`, `getTrades()` | No | eager removal keeps every order in the book live |
| `ConsoleView.showBook`, `showTrades`, `ask`, `showError` | No | "my orders" reuses `showBook` |

The view change is one output method; no view class learns how the book stores orders. If the build ends up changing any row marked "No", the D4 comparison should say which and why.

## Plan

| Step | What | Owner |
|---|---|---|
| 1 | All three agree to the signature changes in Part 1 (the spec's integration rule 1 requires it) | Marcos, Riya, Raghul |
| 2 | Part 1 in the model: `int` trader IDs, order IDs, `submit` returns the ID; update `OrderBookTest` | Marcos |
| 3 | Part 1 in the view: print trader IDs and the order ID column | Riya |
| 4 | Part 1 in the controller: ask for the trader ID at startup, parse it, and read `buy`, `sell`, `switch` and `quit` as commands | Raghul |
| 5 | Report Part 1 in the README Reflection as a change from the spec, with where it came from | Marcos |
| 6 | `LinkedQueue.remove` and its tests | Marcos |
| 7 | `OrderBook.cancel`, `getBids(int)`, `getAsks(int)` and their tests | Marcos |
| 8 | `ConsoleView.showMessage` | Riya |
| 9 | Add `cancel` and `mine` to `run`, plus the confirmation lines | Raghul |
| 10 | Merge and run the cancel test cases by hand through the console | Raghul |
| 11 | Compare the build against the prediction table for D4 | Marcos, Riya, Raghul |

Steps 2–4 belong to the D3 prototype; steps 6–11 are Deliverable 4.
