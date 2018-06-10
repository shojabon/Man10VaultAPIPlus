package red.man10.man10vaultapiplus;


enum MoneyPoolTerm{
    LONG_TERM,
    MID_TERM,
    SHORT_TERM,
    UNKNOWN
}

enum MoneyPoolType{
    MEMORY,
    WITHDRAWAL,
    GAMBLE_POOL,
    UNKNOWN,
}

enum TransactionCategory{
    UNKNOWN,
    GENERAL,
    SHOP,
    GAMBLE,
    REPOSITORY_NOTE,
    TAX,
    MARKET,
    GAME
}

enum TransactionType{
    MEMORY_TRANSFER,
    UNKNOWN,
    PAY,
    TRADE,
    BUY,
    SELL,
    FEE,
    BET,
    WIN,
    SEND_CHEQUE,
    REDEEM_CHEQUE,
    LEND,
    COLLECT,
    RECIVE,
}

enum TransactionLogType{
    RESULT,
    RAW,
    BOTH
}

