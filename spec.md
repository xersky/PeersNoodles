# Syncing with other nodes

- trying to sync and retreive the state, receipts and database from nodes that are acheiving consensus
- sending four types of messages to these nodes:
    - ping: 
        - to retreive count of transactions or count of receipts
        - and the hash of both the state and database
        - result as json key value
    - transaction (as a master node):
        - awaiting then
        - sends a batch of transactions to random peers/nodes and expect the same value (state root) xor of hashcode of state and database
    - token:
        - it chooses a random peer and sends it a token to act as a master node and stops the current node of being a master
    - masterchange:
        - awaiting then
        - sends id of new master