package red.man10.man10vaultapiplus;

import java.util.HashMap;

public class MoneyPoolManager {

    private static HashMap<Long, MoneyPoolData> poolObjects = new HashMap<>();

    public MoneyPoolManager(){
    }

    public void put(Long id, MoneyPoolData obj){
        poolObjects.put(id, obj);
    }

    public MoneyPoolData get(Long id){
        return poolObjects.get(id);
    }


}
