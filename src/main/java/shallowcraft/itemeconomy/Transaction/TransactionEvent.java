package shallowcraft.itemeconomy.Transaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TransactionEvent extends Event {
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
}
