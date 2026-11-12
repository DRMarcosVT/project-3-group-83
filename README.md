# No Cutting!

An order book simulator for one stock. Traders send orders to buy or sell shares at a price. An order fills against the best price on the other side, oldest order first within a price, and any unfilled remainder waits in line.

This is the Deliverable 3 prototype: the model, its JUnit tests, a driver that runs the model alone, and a thin console view that goes through a controller. Order cancellation, our Deliverable 4 change feature, was also built, after its prediction was written; "Built early: prediction against the result" under Next Steps compares the two.

## How to run

You need JDK 17. Run every command from this folder.

### The model, with no view

**Driver.** This compiles the five model classes and the driver only; `view` and `controller` are not on the command line.

```
javac -d bin src/model/LinkedQueue.java src/model/Side.java src/model/Order.java src/model/Trade.java src/model/OrderBook.java src/driver/ModelDriver.java
java -cp bin driver.ModelDriver
```

The driver sends five good orders and four bad ones to an `OrderBook` and prints each answer, then both sides of the book and the trade log. The last lines should show three trades: 50 at $25, 50 at $25, 20 at $26.

**JUnit tests.** In Eclipse: File > Open Projects from File System, choose this folder, then right-click `src/model` > Run As > JUnit Test. The project uses JUnit 5. Expected: 44 tests, 0 failures (17 in `LinkedQueueTest`, 27 in `OrderBookTest`). Both test classes create only model objects.

### The thin view

```
javac -d bin -sourcepath src src/controller/OrderController.java
java -cp bin controller.OrderController
```

Type a trader ID, then commands:

| Command | What it does |
|---|---|
| `buy` or `sell` | Asks for a quantity and a price in whole dollars, sends the order as the current trader, prints its order ID, the book and the trade log. |
| `cancel <order ID>` | Cancels one of the current trader's waiting orders, prints how many shares were left unfilled, then the book and the trade log. |
| `mine` | Prints only the current trader's waiting orders. |
| `switch <id>` | Changes the current trader, so one person can type both sides of a trade. |
| `quit` | Ends the program. |

Three scripted sessions are in `demo/`. `mvp-session.txt` walks the MVP path; `bad-input-session.txt` types every bad-input case from our scope and ends without `quit`; `cancel-session.txt` cancels a partly filled order and tries every bad cancel.

```
java -cp bin controller.OrderController < demo/mvp-session.txt
java -cp bin controller.OrderController < demo/bad-input-session.txt
java -cp bin controller.OrderController < demo/cancel-session.txt
```

In PowerShell, `<` is not available; use `Get-Content demo\mvp-session.txt | java -cp bin controller.OrderController`. Typed input is not echoed when it comes from a file, so several prompts appear on one line.

### Where things are

| Path | Role | Contents |
|---|---|---|
| `src/model` | Model | `LinkedQueue`, `Side`, `Order`, `Trade`, `OrderBook`, and the two test classes |
| `src/view` | View | `ConsoleView` |
| `src/controller` | Controller | `OrderController`, which holds `main` |
| `src/driver` | Driver | `ModelDriver` |
| `demo` | | The three scripted sessions |
| `Preliminary Spec.md` | | The spec, revised to match this build and to plan Deliverable 4 |

## Reflection

"The spec" below means the Deliverable 2 text, which is in git at commit `e55516d`.

### What changed from the specification

| The spec said | We built | Why | What prompted it |
|---|---|---|---|
| A trader is a name: `submit(String trader, ...)`, `String getTrader()`, `String getBuyer()`, `String getSeller()`. | A trader is a positive `int` ID in all four places. | Cancellation has to check that the trader asking owns the order. Two `int`s compare with `==`; two names can differ by case or spacing and still mean the same person. | Planning the Deliverable 4 change feature. Marcos proposed the switch before any code existed, so it cost spec edits and no rework. |
| Orders had no ID. `submit` returned `void`. | `OrderBook` numbers each accepted order from one counter, `nextOrderId`. `Order` has `getId()`. `submit` returns the ID. | A user needs a way to name the order to cancel. | Planning the change feature, as in the row above. The spec's stretch row already mentioned `cancel(int orderId)` but gave orders no ID to match it. |
| `submit` rejected a blank trader name. | `submit` rejects a trader ID below 1. `run` also rejects it when it is typed. | A number cannot be blank. Text typed as an ID is caught in the controller by `Integer.parseInt`, where the spec already caught text typed as a quantity or price. | Follows from the first row. |
| `run` asked for trader, side, quantity and price for every order, and ended on `quit` at the trader prompt. | `run` asks for a trader ID once and keeps it in a new field, `currentTrader`. It then reads commands: `buy`, `sell`, `cancel <id>`, `mine`, `switch <id>`, `quit`. | Knowing who is typing lets `submit` block self-trades and lets `cancel` refuse to cancel another trader's order. | Marcos debating with Claude how cancellation should work: who is sending the cancel, and whose order it cancels. That is why the program asks for a trader ID before any trading. |
| `ask` returns the line the user types. | `ask` returns `null` when the input has ended, and `run` treats `null` as `quit`. | Without it the program crashed. | Running the prototype with input piped from a file that did not end in `quit` threw `NoSuchElementException` from `Scanner.nextLine`, at `ConsoleView.ask`. |
| Seven classes in three packages. | An eighth class, `driver.ModelDriver`, in a fourth package. | The model has to be runnable from a driver with no view. Keeping the driver outside `model` keeps every print statement out of the model package. | The Deliverable 3 rubric. |
| The test plan named traders alice, bob, carol and dave. | The same cases with trader IDs 1, 2, 3 and 4, each also checking the order ID. | Follows from the first two rows. | Follows from the first two rows. |
| The test plan had one normal and one bad-input case per method. | Seven more cases: enqueue after the queue was emptied, changing the list `toList` returned, a small order partly filling a waiting one, the same trader waiting on both sides at prices that cannot trade, a waiting order priced past the new order's limit, the maximum quantity and price, and order numbering with a rejected order in between. | The planned cases did not reach these branches: `dequeue` resetting `tail`, the self-trade scan stopping at the first price that does not match, the fill loop stopping at the limit price, and the upper edge of each range check. | Writing those branches and seeing that no planned test would fail if they were wrong. |

Unchanged from the spec: `LinkedQueue`'s five methods and its private `Node`, the two TreeMaps of queues, the matching rules, trading at the waiting order's price, the 10,000 maximums, `getBids`, `getAsks` and `getTrades` returning copies, `ConsoleView`'s four method names, and which class catches each bad input.

### Did the model stand alone?

Yes. Three checks anyone can repeat:

- The five model source files import only `java.util` classes. `grep "^import" src/model/LinkedQueue.java src/model/Side.java src/model/Order.java src/model/Trade.java src/model/OrderBook.java` shows it.
- The driver command under "How to run" compiles and runs the model with `view` and `controller` absent from the command line.
- `LinkedQueueTest` and `OrderBookTest` pass and create only model objects.

Nothing had to move across the model/view line to make it run. The one piece of new state that could have gone on either side, which trader is typing, went in the controller. `OrderBook` takes the trader ID as a parameter and never stores it, so the tests can act as several traders with no setup.

The view knows three model classes, as the spec planned: it imports `Order` and `Trade` and reads their getters, and it prints the `Side` that `Order.getSide()` returns. It holds no `OrderBook`.

One thing in the model is written for the screen. The messages inside `submit`'s `IllegalArgumentException`s, such as "Quantity must be from 1 to 10000, got 0.", reach the user word for word: `run` passes `e.getMessage()` to `showError`. The model does not call or import the view to do this, but the wording a user reads is decided in `OrderBook`. A GUI that wanted different wording would have to map the exceptions itself.

### What happened to our unknowns

| Unknown from the scope | Status |
|---|---|
| How do we cancel an order in the middle of a singly linked queue without changing the others? | Figured out and built. `LinkedQueue.remove(T item)` walks from `head` keeping a reference to the previous node, links the previous node to the one after the match, and moves `head` or `tail` when the match is first or last. Its six tests pass, including removing the tail and then enqueueing, which fails if `tail` is left on the removed node. |
| Should we use a Swing or JavaFX UI? | Settled: we dropped the GUI stretch goal, so we no longer need to choose. The prototype uses the console. See Stretch goals. |
| How do we plan a git workflow for all three team members? | Figured out. The spec plans it: one branch per piece (`model`, `view`, `controller`), merged into `main` by its owner. We worked on separate branches and merged them all into `main`: the spec on `preliminary-spec`, with Raghul's edits merged in at commit `f147ad4`, and this prototype on `prototype-1`. |

### What doesn't work yet

- The whole trade log is printed after every order and every cancel, so a long session scrolls.

## Next Steps

### What is left to finish the MVP

All five MVP steps in the scope run today. What remains is two fixes, in this order:

1. Report an out-of-range quantity before asking for the price: `run` compares the quantity with `OrderBook.MAX_QUANTITY` before the price prompt. `submit` keeps its own check.
2. Decide whether `showTrades` should print only the newest trades.

### Our change feature: order cancellation

A trader can cancel one of their own waiting orders by its ID, and can list their own waiting orders. It was stretch goal 1 in our scope and is not in the MVP.

It qualifies as a change feature on both counts in the rubric. It changes model logic and state: `OrderBook` gains a way to take an order out of the book, with three checks, and `LinkedQueue` gains its first operation that removes from the middle. A user reaches it through the controller and view with two new commands, `cancel <orderId>` and `mine`.

**Prediction: what it will touch**

| Class | Role | Methods we expect to add or change |
|---|---|---|
| `LinkedQueue` | Model | Add `boolean remove(T item)`. |
| `OrderBook` | Model | Add `Order cancel(int traderId, int orderId)`, `ArrayList<Order> getBids(int traderId)` and `ArrayList<Order> getAsks(int traderId)`. |
| `OrderController` | Controller | Change `run` and its private helper `handle` to read `cancel <orderId>` and `mine`, and to call `showMessage` after `buy`, `sell` and `cancel`. |
| `ConsoleView` | View | Add `void showMessage(String message)`. |

**Prediction: what should not need to change**

| Class or method | Why our design says it stays |
|---|---|
| `OrderBook.submit`, `getBids()`, `getAsks()`, `getTrades()` | A cancelled order leaves its queue at once, so every order these methods meet is still waiting. |
| `LinkedQueue.enqueue`, `dequeue`, `peek`, `isEmpty`, `toList` | `remove` is a new method beside them. |
| `ConsoleView.ask`, `showBook`, `showTrades`, `showError` | `mine` hands filtered lists to the existing `showBook`. |
| `Order`, `Trade`, `Side` | `Order` already has `getId()` and `getTrader()`. A cancel makes no trade. |
| `ModelDriver` | It calls only `submit`, `getBids()`, `getAsks()` and `getTrades()`. |

The feature touches classes in all three roles, and each edit stays inside its own role: the model decides whether a cancel is allowed and removes the order, the controller reads the command, and the view prints one line of text it is handed. The view does not learn how the book stores orders, and the model does not print.
The full plan, with the steps of `cancel`, its validation and its tests, is in the spec under "Deliverable 4 change feature: order cancellation".

### Built early: prediction against the result

We built cancellation after writing the prediction above and without changing it. Outside the two test classes, every edit is in the four classes listed below.

| Predicted | What happened |
|---|---|
| `LinkedQueue` adds `remove(T item)` | As predicted. |
| `OrderBook` adds `cancel`, `getBids(int)`, `getAsks(int)` | As predicted, plus two private helpers: `findWaiting`, which scans both sides for an order ID, and `onlyTrader`, which filters a list by trader. Neither is visible outside the class. |
| `OrderController` changes `run` and `handle` | As predicted. One more change: `OrderController` now imports `model.Order`, because it reads the shares cancelled from the `Order` that `cancel` returns. Before this the controller used only `OrderBook` and `Side`. Both are model classes, so the controller still talks only to the model and the view. |
| `ConsoleView` adds `showMessage` | As predicted. |
| No change to `submit`, `getBids()`, `getAsks()`, `getTrades()`, the five other queue methods, `ask`, `showBook`, `showTrades`, `showError`, `Order`, `Trade`, `Side`, `ModelDriver` | Held for every row. |

The tests also go beyond the plan in two places. `cancelClearsSelfTradeBlock` checks that a cancelled order no longer blocks its trader as a self-trade; this is the bug lazy removal would have caused. `removeMissingItem` also calls `remove` on an empty queue.

### Stretch goals

| Stretch goal from the scope | Decision | Reason |
|---|---|---|
| 1. Order cancellation | Committing. It is the change feature. | The spec already places its validation, and it answers our first unknown. |
| 2. Swing or JavaFX GUI | Dropping. | The model would not change: a GUI replaces `ConsoleView`. Two things would. `OrderController` holds a `ConsoleView` field, so it needs a view interface or a second controller, and its `run` loop blocks on `ask`, which a GUI's button events cannot drive. |
| 3. Stats: volume traded, average wait before a fill | Dropping. | Average wait needs a timestamp on every `Order`. Volume and spread would add methods to `OrderBook` and `ConsoleView`; we are keeping Deliverable 4 to cancellation and the MVP fixes above. |
| 4. Market-making model | Dropping. | It needs other traders to trade against, which the program does not have. |

One idea is not in the scope: randomly generated orders, so a single user has a market to trade against, with a setting for a busy or a thin market. We are dropping it.

### New tests we will need

The quantity check in step 1 of "What is left to finish the MVP" must reject 0, -5 and 10,001 before the price prompt; we will add these to `demo/bad-input-session.txt`.

### Who does what

| Item | Owner |
|---|---|
| Quantity check before the price prompt | Raghul Jayaprakash |
| The trade log decision | Riya Shah |
| Review `LinkedQueue.remove`, `OrderBook.cancel`, `getBids(int)`, `getAsks(int)`, and their tests | Marcos Salas |

## Attribution

| Class or test file | Who | How |
|---|---|---|
| `model/LinkedQueue.java` | Marcos Salas, Riya Shah, Raghul Jayaprakash | Marcos: GenAI-assisted; Claude wrote the first draft from the spec at Marcos's direction. Riya and Raghul: reviewed it against the spec. |
| `model/Side.java` | Marcos Salas, Riya Shah, Raghul Jayaprakash | Marcos: GenAI-assisted, as for `LinkedQueue`. Riya and Raghul: reviewed it against the spec. |
| `model/Order.java` | Marcos Salas, Riya Shah, Raghul Jayaprakash | Marcos: GenAI-assisted, as for `LinkedQueue`; Marcos decided on the `int` trader ID and the order ID. Riya and Raghul: reviewed it against the spec. |
| `model/Trade.java` | Marcos Salas, Riya Shah, Raghul Jayaprakash | Marcos: GenAI-assisted, as for `LinkedQueue`. Riya and Raghul: reviewed it against the spec. |
| `model/OrderBook.java` | Marcos Salas, Riya Shah, Raghul Jayaprakash | Marcos: GenAI-assisted, as for `LinkedQueue`; Marcos decided on the self-trade rule in the spec and the ID counter. Riya and Raghul: reviewed it against the spec. |
| `model/LinkedQueueTest.java` | Marcos Salas, Riya Shah, Raghul Jayaprakash | Marcos: GenAI-assisted; Claude wrote the tests from the spec's test plan at Marcos's direction and ran them. Riya and Raghul: reviewed the tests against the spec's test plan. |
| `model/OrderBookTest.java` | Marcos Salas, Riya Shah, Raghul Jayaprakash | Marcos: GenAI-assisted, as for `LinkedQueueTest`. Riya and Raghul: reviewed the tests against the spec's test plan. |
| `driver/ModelDriver.java` | Marcos Salas, Riya Shah, Raghul Jayaprakash | Marcos: GenAI-assisted; Claude wrote and ran it at Marcos's direction. Riya and Raghul: reviewed it. |
| `view/ConsoleView.java` | Riya Shah (owner), Marcos Salas, Raghul Jayaprakash | Marcos: GenAI-assisted; Claude wrote the first draft, prompted by Marcos. Riya: reviewed it against the spec as its owner. Raghul: reviewed it against the spec. |
| `controller/OrderController.java` | Raghul Jayaprakash (owner), Marcos Salas, Riya Shah | Marcos: GenAI-assisted; Claude wrote the first draft, prompted by Marcos. Raghul: reviewed it against the spec as its owner. Riya: reviewed it against the spec. |

### GenAI note

**Choosing the change feature.** Following the rubric's tip, Marcos debated with Claude which of the stretch goals in our spec would make the best change feature, and which classes each one would force us to edit. For cancellation Claude listed `LinkedQueue`, `OrderBook`, `OrderController` and `ConsoleView`, which crosses our model/view line. Marcos also used Claude to clarify what the model/view boundary means: a feature can have a model part and a view part as long as neither does the other's job. On that reading we kept both the feature and the boundary, since every predicted edit stays inside its own role.

**Designing it.** Marcos proposed integer trader IDs, order IDs, a trader ID set before the first command, and a rule that you can cancel only your own order. We kept all four. He also proposed a cancel as its own kind of order held in a queue, and one ID counter per side. On Claude's recommendation we changed both: a cancel is a method call, and there is one counter. Claude suggested the `switch` command, returning the order ID from `submit`, and removing a cancelled order from its queue at once; Marcos accepted them.

**Writing the prototype.** Claude wrote the preliminary draft of the codebase, including the tests. Running a session that ended without `quit` exposed the crash described in the Reflection, which Claude fixed. Claude also applied the agreed changes to the spec. We used Claude on this README for formatting, fact-checking and grammar.
