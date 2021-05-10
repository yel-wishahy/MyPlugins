package shallowcraft.itemeconomy.Data;

import shallowcraft.itemeconomy.Accounts.Account;

import java.util.Map;

public interface Serializable<T> {
    public Map<String, String> getSerializableData();
    //public T loadFromData(Map<String, String> inputData, String ID);
//    public T getObject(String[] data);
}
