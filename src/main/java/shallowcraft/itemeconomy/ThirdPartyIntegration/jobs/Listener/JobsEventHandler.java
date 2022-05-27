package shallowcraft.itemeconomy.ThirdPartyIntegration.jobs.Listener;

import com.gamingmesh.jobs.api.JobsPaymentEvent;
import com.gamingmesh.jobs.container.CurrencyType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.ItemEconomy;

public class JobsEventHandler implements Listener {
    @EventHandler
    public void onJobsPayment(JobsPaymentEvent event) {
        try {
            double amount = event.get(CurrencyType.MONEY);
            Account taxAccount = ItemEconomy.getInstance().getAccounts().get((String)Config.TaxesConfig.get("mainTaxDepositID"));
            taxAccount.transactionBalanceBuffer(-1 * amount);
            taxAccount.convertBalanceBuffer();
            if (ItemEconomy.getInstance().isDebugMode()) {
                ItemEconomy.log.info("[ItemEconomy] JobsReborn API Listener detected job payment event");
                ItemEconomy.log.info("[ItemEconomy] withdrew " + amount + " from " + taxAccount.getID() + " due to jobs payment.");
            }
        } catch (Exception e){
            if(ItemEconomy.getInstance().isDebugMode())
                e.printStackTrace();
        }

    }
}
