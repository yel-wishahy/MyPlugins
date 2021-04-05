package shallowcraft.itemeconomy.Shop;

public class HyperbolicShop {
    private static double getPrice(int stockAmount, double value){
        int stock = stockAmount;
        if(stock < 1)
            stock = 1;

        return value/stock;
    }
}
