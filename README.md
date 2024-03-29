# ItemEconomy

A minecraft server ecoomy plugin working on the current 1.18 paper api and hooks to Vault to link with other economy plugins.
Unlike typical curreency plugins, the currency in this plugin is a physical item or items.

The current Config is set to DIAMOND and DIAMOND_BLOCK.

The way the physical currency works is that any player invetory and chest vault acts as a 'bank account' which the currency can be withdrawn from. You can create a chest vault
by placing a sign on a chest with "[Vault]" as the first line.

Item Vault creation sign:
  - Line 1: "[Vault]"
  - Line 2 (optional): Name of account holder
  - Line 3 (optional, defaults to regular): Vault type: "[Withdraw]" or "[Deposit]" 
    - for withdraw or deposit only vaults

[Video Demo](https://www.youtube.com/watch?v=MuiKJ1oVDqU)
[Portfolio Project Description](https://yel-wishahy.github.io/#modalMCPlugin)

```
name: ItemEconomy
version: ${project.version}
main: shallowcraft.itemeconomy.ItemEconomyPlugin
api-version: 1.18
authors: [ BlackShadow2941 ]
description: An item based economy for minecraft with vault support and quickshop addon
depend: [Vault]
loadbefore: [QuickShop]

commands:
  ItemEconomy:
    aliases: [ie, IE, itemeconomy, money, eco, economy]
    usage: |
      /ItemEconomy balance
      /ItemEconomy baltop
      /ItemEconomy create_account <name>
      /ItemEconomy list_accounts
      /ItemEconomy create_account_all
      /ItemEconomy reload
      /ItemEconomy load
      /ItemEconomy save
      /ItemEconomy statsupdate
      /ItemEconomy remove_account <name>
      /ItemEconomy admindeposit <name> <amount>
      /ItemEconomy adminwithdraw <name> <amount>
      /ItemEconomy withdraw <amount>
      /ItemEconomy deposit <amount>
      /ItemEconomy transfer <vault tpye> <vault type> <amount>
      /ItemEconomy admintransfer <name> <vault tpye> <vault type> <amount>
      /ItemEconomy debug
      /ItemEconomy createconfig
  Tax:
    aliases: [tax, ietax, ieTax, taxes, t]
    usage: |
      /Tax tax <name> <[optional] tax name>
      /Tax add <name> <tax name> <rate>
      /Tax remove <name> <tax name>
      /Tax info <name> < [optional] tax name>
      /Tax taxall
      /Tax clear <name>
      /Tax edit <name> <tax name> <args>
      /Tax redistribute
      /Tax taxprofits
  SmartShop:
    aliases: [smartShop, s, ss ]
    usage: |
      /SmartShop info < [optional] name>
      /SmartShop accept <ordername> (or accept all)
      /SmartShop decline <ordername> (or decline all)
      /SmartShop remove <name> <order name> (or remove all)
      /SmartShop generate
      /SmartShop log <name>
permissions:
  itemeconomy.admin:
    default: op
  itemeconomy.message:
    default: true
  itemeconomy.player:
    default: true
  itemeconomy.remotewithdraw:
    default: op
  itemeconomy.remotedeposit:
    default: op

```
