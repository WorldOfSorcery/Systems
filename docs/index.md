# WoSSystems Documentation

## Table of Contents

1. [Economy](#Economy)



## Economy
#### Commands

/eco subcommand parameters

| Subcommand                         | Example            | Permissions           | Function                                          |
| ---------------------------------- | ------------------ | --------------------- | ------------------------------------------------- |
| **give** player currency amount    | HektorTM gold 1    | economy.modify.give   | Give a player a currency                          |
| **take** player currency amount    | HektorTM gold 1    | economy.modify.take   | Takes currency from a player                      |
| **set** player currency amount     | HektorTM gold 1    | economy.modify.set    | Set a player's currency                           |
| **reset** player currency          | HektorTM gold      | economy.modify.reset  | reset a players currency                          |
| **random** player currency min max | HektorTM gold 1 30 | economy.modify.random | Give a player a random amount between min and max |
|                                    |                    |                       |                                                   |

---
 /pay player currency amount

| Example          | Function                |
| ---------------- | ----------------------- |
| HektorTM gold 10 | Pay a player a currency |

---
/balance player

| Parameters       | Permissions                                  | Function                            |
| ---------------- | -------------------------------------------- | ----------------------------------- |
| balance HektorTM | economy.balance.self, economy.balance.others | View your own or a players currency |

---
/coinflip amount(in gold) heads/tails

| Parameters | Permissions      | Function                     |
| ---------- | ---------------- | ---------------------------- |
| 10 heads   | economy.coinflip | Creates a coinflip challenge |

## Systems

### Citems

/citem
**Flags:**
- Unusable
- Undroppable
- hide

| Subcommands                         | Permissions          | Function                        |
| ----------------------------------- | -------------------- | ------------------------------- |
| **rename** newName                  | citem.modify.rename  | set name of item                |
| **lore** add/remove/edit            | citem.modify.lore    | edit/add/remove lore            |
| **flag** add/remove                 | citem.modify.flags   | add/remove flag                 |
| **action** right/left interactionID | citem.modify.actions | add actions to right/left click |
| **save** ID                         | citem.save           | save a citem                    |
| **update** ID                       | citem.update         | update a citem                  |
| **tag** tagID                       | citem.tag            | add Tags                        |
| **info**                            | citem.info           | view a Citems information       |
