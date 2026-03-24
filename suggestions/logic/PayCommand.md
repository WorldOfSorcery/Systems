# PayCommand

**Package:** `me.hektortm.woSSystems.systems.economy.cmd`

## Logic Issues

### 1. Args length check is inverted — usage error never fires for correct input

Line 38:
```java
if(args.length > 3) {
```
The command expects exactly three arguments (`<player> <currency> <amount>`). The guard should reject calls with *fewer than* 3 arguments (`args.length < 3`), not *more than* 3. As written, any call with 4+ args sends a usage error, while a call with 0, 1, or 2 args proceeds and throws `ArrayIndexOutOfBoundsException` when `args[0]`, `args[1]`, or `args[2]` is accessed.

### 2. `currency` object accessed before null check

Lines 47–51:
```java
Currency currency = ecoManager.getCurrencies().get(currencyName.toLowerCase());

String color = currency.getColor();   // NPE if currency is null
String icon  = currency.getIcon();
```
If `currencyName` does not match any known currency, `getCurrencies().get(...)` returns `null` and the next line throws `NullPointerException`. There is a `currencyExists` check in other subcommands (e.g. `GiveCommand`) but it is absent here. A null guard or `currencyExists` check must be added before dereferencing `currency`.

### 3. Self-pay is not prevented

The command does not check whether the sender is paying themselves (`p.getUniqueId().equals(target.getUniqueId())`). A player can run `/pay <own name> currency 100` and the code will deduct the amount then add it back, resulting in a net-zero transaction that still fires all success messages and action-bar notifications. This is inconsistent with the coinflip command, which does block self-challenges.
