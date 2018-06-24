package red.man10.man10vaultapiplus;

import red.man10.man10vaultapiplus.enums.MoneyPoolTerm;

import java.util.UUID;

class MoneyPoolData {
    double balance;
    long id;
    MoneyPoolTerm term;

    boolean wired;
    UUID wiredUuid;
    String wiredName;

    double value;
    String plugin;
    long pId;
    String memo;
    boolean frozen;

    boolean available = true;
}
