# No Cutting!

An order book simulator for one stock. Traders send orders to buy or sell shares at a price. An order fills against the best price on the other side, oldest order first within a price, and any unfilled remainder waits in line.

This is the Deliverable 4 prototype: the console view runs through the controller, order cancellation is the change feature, and JUnit tests cover every public `LinkedQueue` and `OrderBook` method with a normal and a bad-input case, plus every bad input the controller catches.

## How to run

You need JDK 17. Run every command in PowerShell from this folder.

### The program

```
javac -d bin -sourcepath src src\controller\OrderController.java
java -cp bin controller.OrderController
```

Type a trader ID, then commands:

| Command | What it does |
|---|---|
| `buy` or `sell` | Asks for a quantity, rejects it at once if it is not from 1 to 10,000, then asks for a price in whole dollars. Sends the order as the current trader and prints its order ID, the book and the newest five trades. |
| `cancel <order ID>` | Cancels one of the current trader's waiting orders, prints how many shares were left unfilled, then the book and the newest five trades. |
| `mine` | Prints only the current trader's waiting orders. |
| `switch <id>` | Changes the current trader, so one person can type both sides of a trade. |
| `quit` | Ends the program. |

### Scripted sessions

Four scripts are in `demo/`:

| Script | What it shows |
|---|---|
| `demo-session.txt` | The 3-minute demo path under Next Steps. |
| `mvp-session.txt` | The MVP path: an order waits, a bigger one fills it and rests, a third waits. |
| `bad-input-session.txt` | Every bad-input case from our scope. It ends without `quit` to show the program survives the input running out. |
| `cancel-session.txt` | Cancels a partly filled order and tries every bad cancel. |

```
cmd /c "java -cp bin controller.OrderController < demo\demo-session.txt"
```

Use `cmd /c` with `<` as above. Piping with `Get-Content demo\demo-session.txt | java ...` puts a byte-order mark in front of the first line in our PowerShell setup, and the program rejects the first trader ID as `"﻿1"`. Input read from a file is not echoed, so several prompts appear on one line.

### The model, with no view

```
javac -d bin src\model\LinkedQueue.java src\model\Side.java src\model\Order.java src\model\Trade.java src\model\OrderBook.java src\driver\ModelDriver.java
java -cp bin driver.ModelDriver
```

This compiles the five model classes and the driver only; `view` and `controller` are not on the command line. The last lines should show three trades: 50 at $25, 50 at $25, 20 at $26.

### The JUnit tests

Download the JUnit console runner once into `lib\` (it is in `.gitignore`):

```
New-Item -ItemType Directory -Force lib
Invoke-WebRequest https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar -OutFile lib\junit-platform-console-standalone-1.10.2.jar
```

Then compile and run the tests:

```
javac -d bin -cp lib\junit-platform-console-standalone-1.10.2.jar -sourcepath src src\model\LinkedQueueTest.java src\model\OrderBookTest.java src\controller\OrderControllerTest.java
java -jar lib\junit-platform-console-standalone-1.10.2.jar execute --class-path bin --select-package model --select-package controller
```

Expected: 64 tests successful, 0 failed (18 in `LinkedQueueTest`, 29 in `OrderBookTest`, 17 in `OrderControllerTest`).

`LinkedQueueTest` and `OrderBookTest` create only model objects; they never touch `ConsoleView` or `OrderController`. `OrderControllerTest` runs whole command sessions through `OrderController` with a fake view: `FakeView`, a class inside the test file that implements `View`, returns typed lines from a list and records every error, message, book and trade list the controller hands it. Nothing is typed at a keyboard and nothing is printed.

### Where things are

| Path | Role | Contents |
|---|---|---|
| `src/model` | Model | `LinkedQueue`, `Side`, `Order`, `Trade`, `OrderBook`, and the two test classes |
| `src/view` | View | `View`, the interface the controller talks to, and `ConsoleView`, which implements it |
| `src/controller` | Controller | `OrderController`, which holds `main`, and `OrderControllerTest` |
| `src/driver` | Driver | `ModelDriver` |
| `demo` | | The four scripted sessions |
| `Preliminary Spec.md` | | The spec, revised after Deliverable 2 |
| `Cancellation Proposal.md` | | The design of the change feature |

## Reflection

### How our design absorbed the change

The change feature is order cancellation: a trader cancels one of their own waiting orders by its ID with `cancel <id>`, and lists their own waiting orders with `mine`. It touched four classes:

| Class | Role | What the feature added |
|---|---|---|
| `LinkedQueue` | Model | `boolean remove(T item)` |
| `OrderBook` | Model | `Order cancel(int traderId, int orderId)`, `getBids(int traderId)`, `getAsks(int traderId)`, and two private helpers, `findWaiting` and `onlyTrader` |
| `OrderController` | Controller | `cancel <id>` and `mine` in `handle`; confirmation lines through `showMessage` |
| `ConsoleView` | View | `void showMessage(String message)` |

The prediction in our Deliverable 3 Next Steps, against what happened:

| Predicted | What happened |
|---|---|
| `LinkedQueue` adds `remove(T item)` | Matched. |
| `OrderBook` adds `cancel`, `getBids(int)`, `getAsks(int)` | Matched, plus the two private helpers. Neither is visible outside the class. |
| `OrderController` changes `run` and `handle` | Matched. `OrderController` also started importing `model.Order`, because it reads the shares cancelled from the `Order` that `cancel` returns. |
| `ConsoleView` adds `showMessage` | Matched. |
| No change to `submit`, `getBids()`, `getAsks()`, `getTrades()`, the five other queue methods, `ask`, `showBook`, `showTrades`, `showError`, `Order`, `Trade`, `Side`, `ModelDriver` | Held for every row. No class or method the prediction said would stay unchanged had to change. |

What surprised us:

- `remove` takes an item out of the middle of a queue. Our scope said a deque was not needed because "nothing joins the front or leaves the back"; that still holds, but the scope did not foresee removal from the middle. `remove` keeps the arrival order of the items left, so fills stay first in, first out.
- Eager removal kept `submit` untouched. Because `cancel` takes the order out of its queue at once, `submit`'s best-price check, fill loop and self-trade scan never meet a cancelled order. The test `cancelClearsSelfTradeBlock` checks the case lazy removal would have broken: a trader whose cancelled sell would otherwise block their own buy as a self-trade.

The feature and the base prototype are in one commit, `4e78316`, so `git diff` cannot isolate the feature's edits. The table above comes from the methods the feature added, each of which exists only for `cancel` or `mine`.

### Did the change cross the model/view line?

Each edit stayed inside its own role. The model decides whether a cancel is allowed and removes the order; the controller reads the command and picks whose orders to ask for; the view prints what it is handed.

The view learned one thing: how to print a confirmation, `showMessage(String)`. It did not learn how the book stores orders. `mine` passes the filtered lists from `getBids(int)` and `getAsks(int)` to the existing `showBook`, so the view prints one trader's orders with the same code that prints everyone's.

The view keeps no copy of model state. `ConsoleView`'s only field is the `Scanner` it reads with; every list it prints is passed in by the controller for that one call and not stored.

One thing in the model is still written for the screen: the messages inside `OrderBook`'s `IllegalArgumentException`s, such as "Order #3 belongs to another trader.", reach the user word for word through `showError`. The model does not call or import the view, but a GUI that wanted different wording would have to map the exceptions itself. One exception class per rejection, such as a `NotYourOrderException`, would let each view choose its own wording.

### What design change would make the next feature cheaper

We have already made the first one. `OrderController` used to hold a `ConsoleView`, so a GUI or a second screen meant editing the controller. It now holds a `view.View`, an interface with `ConsoleView`'s five public methods, so a GUI view is a new class that implements `View` and the controller does not change. `FakeView` in `OrderControllerTest` is the second class to implement it.

The next change is a `placeOrder` method in `OrderController`. Today, when you type `buy`, the program stops and asks you to type a quantity and a price. A GUI cannot answer those typed questions, because its user fills in boxes and clicks a button instead. `placeOrder` would take the side, quantity and price directly, so nothing gets asked:

```java
placeOrder(Side.BUY, 100, 25);   // the Buy button calls this
placeOrder(Side.SELL, 100, 25);  // the Sell button calls this
```

### What changed from our Deliverable 3 plan

| D3 plan | What we did | Why |
|---|---|---|
| Report an out-of-range quantity before asking for the price. | Done in `OrderController.handle`. A quantity of 0, a negative one, or one above 10,000 is rejected before the price prompt; text was already rejected there. `submit` keeps its own check. | You should not be asked to price an order that cannot be sent. |
| Add the three quantity cases to `demo/bad-input-session.txt`. | The cases were already there; we deleted the price line after each rejected quantity (three lines). | After the fix, the old price line `25` was read as a command and printed "not a command". |
| Decide whether `showTrades` should print only the newest trades. | After each order or cancel, the controller passes the newest five trades to `showTrades` and prints "Showing the newest 5 of N trades." when it cut any. The book still keeps every trade. `ConsoleView`'s code did not change; only the `showTrades` Javadoc was reworded. | The whole log scrolled the book off the screen in long sessions. Cutting the list in the controller keeps the change out of the view; `OrderController` now imports `model.Trade` and `java.util.ArrayList` to do it. |
| New tests: only the quantity cases in the demo script. | Three more model tests: `interleavedEnqueueDequeue`, `getBidsForImpossibleTraderId`, `getAsksForImpossibleTraderId`. A new test class, `OrderControllerTest`, with 17 tests. | `getBids(int)` had only a normal case, and no test dequeued while two or more items were held. The bad inputs the controller catches were checked only by the demo scripts, which no JUnit run executes. |
| Nothing planned for how to test the controller. | `OrderControllerTest` drives `OrderController.run()` through `FakeView`, which supplies typed lines from a list and records what it is told to show. `FakeView` was first a `ConsoleView` subclass, so the tests needed no change to production code; it now implements `View` (next row). | `parseNumber`, `parseSide` and `parseTraderId` are private, and `run()` reads through a `ConsoleView` that wraps `System.in`, so a test could not reach the controller's checks without faking the keyboard. We also considered making the three parse methods package-private and static and testing them one by one. We chose the fake view because it needs no change to production code and it also reaches the checks that live inside `handle` (`cancel 1 2`, `switch 2 3`) and the end-of-input path, which the parse methods do not. |
| Nothing planned: the `View` interface was a final-build idea. | Added `view.View` with `ask`, `showBook`, `showTrades`, `showError` and `showMessage`. `ConsoleView` implements it. `OrderController`'s field and constructor parameter are now typed `View`; `main` still passes a `ConsoleView`. `FakeView` implements `View` in place of extending `ConsoleView`. No method body changed, and the program's output for `demo-session.txt` is identical. | As a subclass, `FakeView` depended on `ConsoleView` staying non-`final` and on its constructor never reading `System.in`. Either change would have stopped the controller tests compiling or left them waiting for keyboard input. It is also the first half of the design change named above. |
| README ran the tests in Eclipse and the scripts with `Get-Content`. | Every command is for PowerShell, the tests run with the JUnit console runner, and the scripts run through `cmd /c`. | We demo in PowerShell. Piping with `Get-Content` put a byte-order mark before the first line, and the program rejected the first trader ID. |
| [Placeholder: the review of `LinkedQueue.remove`, `OrderBook.cancel`, `getBids(int)`, `getAsks(int)` and their tests.] | [Placeholder] | [Placeholder] |

### What doesn't work yet

- Input from a script is not echoed, so several prompts run together on one line. Type the demo live, or read the script alongside the output.

### Bad-input coverage

| Bad input | Caught by | Checked by |
|---|---|---|
| Quantity 0, negative, or above 10,000 | `OrderController.handle`, before the price prompt; `OrderBook.submit` again | `badQuantityRejectedBeforePrice`, `submitRejectsBadValues`; `bad-input-session.txt` |
| Price 0, negative, or above 10,000 | `OrderBook.submit` | `badPriceRejected`, `submitRejectsBadValues`; `bad-input-session.txt` |
| Text, a decimal, or a number too big for `int` typed as a quantity or price | `OrderController.parseNumber` | `badQuantityRejectedBeforePrice`, `badPriceRejected`; `bad-input-session.txt` |
| Trader ID 0 or negative | `OrderController.parseTraderId`; `OrderBook.submit` | `startRejectsBadTraderIds`, `switchRejectsBadInput`, `submitRejectsBadValues`; `bad-input-session.txt` |
| Text or a blank line typed as a trader ID | `OrderController.parseNumber` | `startRejectsBadTraderIds`, `switchRejectsBadInput`; `bad-input-session.txt` |
| A side other than buy or sell, or an unknown command | `OrderController.parseSide` | `unknownCommandsRejected`; `bad-input-session.txt` |
| Self-trade | `OrderBook.submit` | `submitRejectsSelfTrade`, `selfTradeShownAsError`; `bad-input-session.txt`; `demo-session.txt` |
| Cancel an ID never issued | `OrderBook.cancel` | `cancelRejectsIdsNeverIssued`, `cancelShowsBookErrors`; `cancel-session.txt` |
| Cancel a filled or already cancelled order | `OrderBook.cancel` | `cancelRejectsFilledOrder`, `cancelRejectsSecondCancel`; `cancel-session.txt` |
| Cancel another trader's order | `OrderBook.cancel` | `cancelRejectsOtherTrader`, `cancelShowsBookErrors`; `cancel-session.txt`; `demo-session.txt` |
| Text, or the wrong number of words, after `cancel` or `switch` | `OrderController.handle` | `cancelRejectsBadInput`, `switchRejectsBadInput`; `cancel-session.txt`; `bad-input-session.txt` |
| `getBids(int)` or `getAsks(int)` with trader ID 0 or negative | Returns an empty list | `getBidsForImpossibleTraderId`, `getAsksForImpossibleTraderId` |
| Input ends before `quit`, or in the middle of an order | `View.ask` returns `null`; `run` stops, or `parseNumber` rejects the half-typed order | `inputEndsBeforeQuit`, `inputEndsMidOrder`; `bad-input-session.txt` |

Test names in `OrderBookTest` and `LinkedQueueTest` call the model directly. The others are in `OrderControllerTest` and run through `FakeView`.

For the methods that take no input (`getBids()`, `getAsks()`, `getTrades()`, `isEmpty()`, `toList()`), the bad-input case is the call on an empty book or queue: `getBidsReturnsACopy`, `getAsksReturnsACopy`, `getTradesReturnsACopy`, `isEmptyFollowsContents`, `toListEmpty`.

## Next Steps

### What is left for the final build

1. Add volume traded, from stretch goal 3: `OrderBook.getVolume()` sums the shares in `getTrades()`, and a `stats` command in `OrderController.handle` prints it through `showMessage`. It gets a normal test and an empty-book test in `OrderBookTest`. That is one model method and one command, with no change to the view.
2. Fill in the attribution table and GenAI note; each teammate checks their own rows.
3. Merge the final build into `main`.

### Stretch goals

| Stretch goal from the scope | Decision | Reason |
|---|---|---|
| 1. Order cancellation | Done. It is the change feature. | |
| 2. Swing or JavaFX GUI | Dropping. | `OrderController` now talks to a `View` interface, but its `run` loop still blocks on `ask`, so a GUI first needs the `placeOrder` split described under "What design change would make the next feature cheaper". |
| 3. Stats: volume traded, average wait before a fill | Committing to volume traded; dropping average wait. | Volume is one `OrderBook` method over the trades the book already keeps, printed through the existing `showMessage`. Average wait needs a timestamp on every `Order`. |
| 4. Market-making model | Dropping. | It needs other traders to trade against, which the program does not have. |

Randomly generated orders, which are not in the scope, are dropped too.

### The 3-minute demo

Run `cmd /c "java -cp bin controller.OrderController < demo\demo-session.txt"`, or type the same lines live. The test run at the end uses the commands under "How to run", compiled beforehand. We rehearsed the whole path against a timer: it finished within 3 minutes, and each step ran close to the time shown.

| Time | Commands | What it shows |
|---|---|---|
| 0:00–~0:20 | Trader `1`: `sell 100 at 25`, `sell 50 at 26`, `buy 10 at 20` | Orders #1, #2 and #3 wait; asks list the lowest price first. |
| ~0:20–~0:55 | `switch 2`, `buy 120 at 26` | Order #4 fills 100 at $25, then 20 at $26: best price first. #2 keeps 30 shares. The trade log shows both fills. |
| ~0:55–~1:35 | `cancel 3`, `cancel 1`, `buy` with quantity `0`, `buy` with quantity `ten` | Trader 2 cannot cancel trader 1's order, or an order that already filled. A bad quantity is rejected before the price prompt. |
| ~1:35–~2:05 | `buy 10 at 22`, `sell 10 at 22` | The sell is rejected as a self-trade against trader 2's own waiting buy. |
| ~2:05–~2:35 | `switch 1`, `mine`, `cancel 2` | Trader 1 sees only their orders, cancels #2, and is told 30 shares were unfilled. The book redraws without it. |
| ~2:35–~3:00 | The JUnit run | 64 tests, 0 failures. |

### Who does what

| Item | Owner |
|---|---|
| 1. Volume traded and the `stats` command | Raghul Jayaprakash |
| 2. Attribution table and GenAI note | Marcos Salas; each teammate checks their own rows |
| 3. Merge into `main` | Raghul Jayaprakash (controller owner, who did the first join under integration rule 4 in the spec) |

## Attribution

| Class or test file | Who | How |
|---|---|---|
| `model/LinkedQueue.java` | Riya Shah, Raghul Jayaprakash, Marcos Salas | GenAI (Claude) made a preliminary draft; Riya, Raghul and Marcos checked and edited it collectively, with the work split evenly among the three |
| `model/Side.java` | Riya Shah, Raghul Jayaprakash, Marcos Salas | GenAI (Claude) made a preliminary draft; Riya, Raghul and Marcos checked and edited it collectively, with the work split evenly among the three |
| `model/Order.java` | Riya Shah, Raghul Jayaprakash, Marcos Salas | GenAI (Claude) made a preliminary draft; Riya, Raghul and Marcos checked and edited it collectively, with the work split evenly among the three |
| `model/Trade.java` | Riya Shah, Raghul Jayaprakash, Marcos Salas | GenAI (Claude) made a preliminary draft; Riya, Raghul and Marcos checked and edited it collectively, with the work split evenly among the three |
| `model/OrderBook.java` | Riya Shah, Raghul Jayaprakash, Marcos Salas | GenAI (Claude) made a preliminary draft; Riya, Raghul and Marcos checked and edited it collectively, with the work split evenly among the three |
| `model/LinkedQueueTest.java` | Riya Shah, Raghul Jayaprakash, Marcos Salas | GenAI (Claude) made a preliminary draft; Riya, Raghul and Marcos checked and edited it collectively, with the work split evenly among the three |
| `model/OrderBookTest.java` | Riya Shah, Raghul Jayaprakash, Marcos Salas | GenAI (Claude) made a preliminary draft; Riya, Raghul and Marcos checked and edited it collectively, with the work split evenly among the three |
| `driver/ModelDriver.java` | Riya Shah, Raghul Jayaprakash, Marcos Salas | GenAI (Claude) made a preliminary draft; Riya, Raghul and Marcos checked and edited it collectively, with the work split evenly among the three |
| `view/View.java` | Marcos Salas | GenAI-assisted: Claude wrote it at Marcos's direction, along with the matching changes to `ConsoleView`, `OrderController` and `OrderControllerTest` |
| `view/ConsoleView.java` | Riya Shah, Raghul Jayaprakash, Marcos Salas | GenAI (Claude) made a preliminary draft; Riya, Raghul and Marcos checked and edited it collectively, with the work split evenly among the three |
| `controller/OrderController.java` | Riya Shah, Raghul Jayaprakash, Marcos Salas | GenAI (Claude) made a preliminary draft; Riya, Raghul and Marcos checked and edited it collectively, with the work split evenly among the three |
| `controller/OrderControllerTest.java` | Riya Shah, Raghul Jayaprakash, Marcos Salas | GenAI (Claude) made a preliminary draft; Riya, Raghul and Marcos checked and edited it collectively, with the work split evenly among the three |

### GenAI note

We used GenAI (Claude) to propose and draft plans to meet our goals as outlined in the rubric. We also used it for structuring this README so that it looks good, has proper spelling and lacks factual errors, and for updating any stale sentences or inconsistencies. We also used it for tests that go beyond full branch coverage.
