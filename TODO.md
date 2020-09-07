##TODO:
- Database Migration
    1. https://docs.realm.io/sync/v/3.x/using-synced-realms/migrating-your-data
    2. https://github.com/nitrite/nitrite-java/issues/213
    3. https://realm.io/docs/dotnet/latest/api/reference/Realms.Migration.html
    4. https://developer.android.com/training/data-storage/room/migrating-db-versions#java
- LuceneIndexer, test with testIssue174()
- Ensure every error event in replication

DataGate Server:

https://stackoverflow.com/questions/42644779/how-to-secure-a-websocket-endpoint-in-java-ee
https://github.com/ls1intum/jReto

https://github.com/eranyanay/1m-go-websockets
https://www.freecodecamp.org/news/million-websockets-and-go-cc58418460bb/

IPFS:

https://www.freecodecamp.org/news/how-to-build-mongodb-like-datastore-using-interplanetary-linked-data-in-5-minutes/


Queue message and run a timer task to pick message and
send over websocket


## title DataGate Message Communication

    Replica (R1)->Server: Connect Message
    note left of Server: Validates Auth token in message
    
    note over Replica (R1)
        Somehow need to bar Replica from 
        sending DataGateFeed message to
        Server before receiving ConnectAck
    end note
    
    alt Valid token
        Server->Server: Stores replica id
        Server->Replica (R1): ConnectAck Message
        note left of Replica (R1):
            Replica can now receive DataGateFeed
        end note
    else Validation failed
        Server->Replica (R1): Error Message
        note left of Replica (R1): 
            Closes connection to server
            and will not receive any further
            communications from server
            until next successful Connect
        end note
    end
    
    Replica (R1)->Replica (R1): Find last sync time
    Replica (R1)->Replica (R1): Find changes since last sync
    
    Replica (R1)->Server: BatchChangeStart Message
    note right of Replica (R1)
        In replica keep the list of NitriteIds of
        the Documents sending to servers. Server
        would send back BatchChangeAck message with
        list of NitriteIds accepted by Server. Replica
        will remove those ids from it's list. Finally
        after exchange is completed this list size should
        be 0.
    end note
    
    note right of Replica (R1): 
        If changes exists sends changes 
        in chunks, size is sets in replication
        config
    end note
    
    Server->Replica (R1): BatchAck
    note left of Server
        BatchChangeAck will contain list of
        NitriteIds accepted by Server.
    end note
    
    Replica (R1)->Server: BatchChangeContinue Message
    Server->Replica (R1): BatchAck
    note left of Server:
        Server broadcast the BatchChangeContinue 
        to all connected peers
    end note
    
    Replica (R1)->Server: BatchChangeContinue Message
    Server->Replica (R1): BatchAck
    
    Server->Replica (R1): DataGateFeed
    Replica (R1)->Server: FeedAck
    
    note right of Replica (R1): 
        When there is no more changes to send
        send BatchChangeEnd with last sync time
    end note
    
    Replica (R1)->Server: BatchChangeEnd Message
    note right of Replica (R1): 
        BatchChangeEnd message will contain replicaId,
        batchSize and last sync time
    end note
    Server->Replica (R1): BatchEndAck
    Replica (R1)->Replica (R1): Check from journal for failed entries
    Replica (R1)->Server: Retry failed entries in DataGateFeed
    Server->Replica (R1): FeedAck
    
    Server->Replica (R1): BatchChangeStart Message
    Replica (R1)->Server: BatchAck
    note left of Server:
        Server should ensure all message sent to replica
        by keeping track of replicaId and nitriteId map
    end note
    
    Server->Replica (R1): BatchChangeContinue Message
    Replica (R1)->Server: BatchAck
    Server->Replica (R1): BatchChangeEnd Message
    Replica (R1)->Replica (R1): Save message header time as last sync time
    Replica (R1)->Server: BatchEndAck
    
    note right of Replica (R1):
        From now on Replica will store the
        timestamp of any ack or feed message
        as last sync time
    end note
    
    Replica (R1)->Server: DataGateFeed
    Server->Replica (R1): FeedAck
    Replica (R1)->Replica (R1): Save message header time as last sync time
    Replica (R1)->Replica (R1): Check from journal for failed entries
    Replica (R1)-->Server: Retry failed entries in DataGateFeed
    Server-->Replica (R1): FeedAck
    
    Server->Replica (R1): DataGateFeed
    Replica (R1)->Server: FeedAck
    Replica (R1)->Replica (R1): Save message header time as last sync time
    
    
    Replica (R1)->Server: Disconnect Message
    Server->Server: Removes replica id
    Replica (R1)->Replica (R1): Closes connection




https://blog.yugabyte.com/how-we-built-a-high-performance-document-store-on-rocksdb/

https://docs.yugabyte.com/latest/architecture/docdb/persistence/

https://blog.yugabyte.com/enhancing-rocksdb-for-speed-scale/

## Fix

1. repository package


```c++
Options.max_background_flushes: 2
Options.max_background_compactions: 8
Options.avoid_flush_during_shutdown: 1
Options.compaction_readahead_size: 16384
ColumnFamilyOptions.comparator: leveldb.BytewiseComparator
ColumnFamilyOptions.table_factory: BlockBasedTable
BlockBasedTableOptions.checksum: kxxHash
BlockBasedTableOptions.block_size: 16384
BlockBasedTableOptions.filter_policy: rocksdb.BuiltinBloomFilter
BlockBasedTableOptions.whole_key_filtering: 0
BlockBasedTableOptions.format_version: 4
LRUCacheOptionsOptions.capacity : 8589934592
ColumnFamilyOptions.write_buffer_size: 134217728
ColumnFamilyOptions.compression[0]: NoCompression
ColumnFamilyOptions.compression[1]: NoCompression
ColumnFamilyOptions.compression[2]: LZ4
ColumnFamilyOptions.prefix_extractor: CustomPrefixExtractor
ColumnFamilyOptions.compression_opts.max_dict_bytes: 32768

```

## Transaction

https://vladmihalcea.com/a-beginners-guide-to-transaction-isolation-levels-in-enterprise-java/