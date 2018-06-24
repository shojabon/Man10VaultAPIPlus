package red.man10.man10vaultapiplus;

import java.util.HashMap;

public class MoneyPoolManager {

    private static HashMap<Long, MoneyPoolObject> poolObjects = new HashMap<>();

    public MoneyPoolManager(){
    }

    public void put(Long id, MoneyPoolObject obj){
        poolObjects.put(id, obj);
    }

    public MoneyPoolObject get(Long id){
        return poolObjects.get(id);
    }


}
