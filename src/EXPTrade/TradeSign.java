package EXPTrade;

import org.bukkit.Location;

import java.util.UUID;

public class TradeSign {
    private Location signLoc;
    private Location chestLoc;
    private UUID playerUUID;
    private int levelProduct;
    private int diamondCost;
    private long storedXP;

    public TradeSign(UUID playerUUID, int levelProduct, int diamondCost, Location signLoc, Location chestLoc){
        this.signLoc = signLoc;
        this.playerUUID = playerUUID;
        this.levelProduct = levelProduct;
        this.diamondCost = diamondCost;
        this.chestLoc = chestLoc;
        storedXP = 0;
    }

    public Location getSignLoc(){
        return signLoc.clone();
    }

    public UUID getPlayerUUID(){
        return playerUUID;
    }

    public int getLevelProduct(){
        return levelProduct;
    }

    public int getDiamondCost(){
        return diamondCost;
    }

    public void addXP(long xp){
        storedXP += xp;
    }

    public void subractXP(long xp){
        storedXP-= xp;
    }

    public Location getChestLoc(){
        return chestLoc.clone();
    }
}
