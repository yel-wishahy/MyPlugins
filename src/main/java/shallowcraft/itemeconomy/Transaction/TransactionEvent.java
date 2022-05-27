package shallowcraft.itemeconomy.Transaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;

public class TransactionEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    @Getter @Setter
    private Transaction transaction;

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public TransactionEvent(Transaction transaction){
        this.transaction = transaction;
    }

    @Override
    public boolean isCancelled() {
        return this.transaction.isCancelled();
    }

    @Override
    public void setCancelled(boolean b) {
        if(b)
            this.transaction.cancel();
    }
}
