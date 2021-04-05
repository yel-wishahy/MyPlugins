# ItemEconomy

A minecraft server ecoomy plugin developed for the current 1.16 PaperMC api and hooks to Vault to link with other economy plugins.
Unlike typical curreency plugins, the currency in this plugin is a physical item or items.

The current Config is set to DIAMOND and DIAMOND_BLOCK.

The way the physical currency works is that any player invetory and chest vault acts as a 'bank account' which the currency can be withdrawn from. You can create a chest vault
by placing a sign on a chest with "[Vault]" as the first line.

Item Vault creation sign:
  - Line 1: "[Vault]"
  - Line 2 (optional): Name of account holder
  - Line 3 (optional, defaults to regular): Vault type: "[Withdraw]" or "[Deposit]" 
    - for withdraw or deposit only vaults

  
name: ItemEconomy
version: ${project.version}
main: shallowcraft.itemeconomy.ItemEconomy
api-version: 1.16
authors: [ BlackShadow2941 ]
description: An item based economy for minecraft with potential for vault support
depend: [Vault]
commands:
  ItemEconomy:
    aliases: [ie, IE, itemeconomy, money, eco, economy]
    usage: |
      /ItemEconomy balance
      /ItemEconomy baltop
      /ItemEconomy create_account
      /ItemEconomy list_accounts
      /ItemEconomy create_account_all
      /ItemEconomy reload
      /ItemEconomy save
      /ItemEconomy remove_account
      /ItemEconomy deposit
      /ItemEconomy withdraw
permissions:
  itemeconomy.admin:
    default: op
