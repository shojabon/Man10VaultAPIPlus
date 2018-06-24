package red.man10.man10vaultapiplus;

import java.util.HashMap;
import java.util.Set;

public class MoneyPoolManager {

    public static HashMap<Long, MoneyPoolData> poolObjects = new HashMap<>();
    public static HashMap<Long, Boolean> changesMade = new HashMap<>();

    public void put(Long id, MoneyPoolData obj){
        poolObjects.put(id, obj);
    }

    public MoneyPoolData get(Long id){
        if(!poolObjects.containsKey(id)){
            MoneyPoolObject pool =  new MoneyPoolObject("MoneyPoolManager", id);
            if(pool.isAvailable()){
                MoneyPoolData data = pool.getData();
                poolObjects.put(id, data);
            }else{
                return null;
            }
        }
        return poolObjects.get(id);
    }




}
