# Glossary

*Book: Glossary, pp. 553–558*

The book ends with a short glossary. Its author notes that the entries are deliberately brief: they capture the core idea of each term, not every subtlety, and point back to the main text for the details. The table below covers **every** glossary entry, rewritten in plain English, with links to the chapter notes where the term is explained properly. (The book's Index is not reproduced here.)

Chapter links: [1](../01-reliable-scalable-maintainable/README.md) · [2](../02-data-models-and-query-languages/README.md) · [3](../03-storage-and-retrieval/README.md) · [4](../04-encoding-and-evolution/README.md) · [5](../05-replication/README.md) · [6](../06-partitioning/README.md) · [7](../07-transactions/README.md) · [8](../08-trouble-with-distributed-systems/README.md) · [9](../09-consistency-and-consensus/README.md) · [10](../10-batch-processing/README.md) · [11](../11-stream-processing/README.md) · [12](../12-future-of-data-systems/README.md)

> "Part II intro" is summarised at the start of the Chapter 5 notes, and "Part III intro" at the start of the Chapter 10 notes.

> **New to a word?** The book's own glossary (first table) is short. Every *other* term, acronym, product and tool name used anywhere in these notes — from *API*, *JSON* and *SSD* to *MVCC*, *Raft* and *write skew* — has a one-line plain-English definition in [Every other term used in these notes](#every-other-term-used-in-these-notes-az) below. Networking and infrastructure basics (latency, caching, load balancers, queues, TCP, DNS) are explained in more depth in [System Design Fundamentals](../00a-system-design-fundamentals/README.md).

| Term | Plain-English meaning | Where it's explained (chapter) |
|---|---|---|
| **asynchronous** | Carrying on without waiting for something to finish (for example, a message being sent to another node over the network), and making no assumption about how long that thing will take. | [Ch 5](../05-replication/README.md) (synchronous vs asynchronous replication), [Ch 8](../08-trouble-with-distributed-systems/README.md) (synchronous vs asynchronous networks; system models) |
| **atomic** | Has two meanings. (1) For concurrent operations: the operation seems to happen at one instant, so no other concurrent process can ever catch it half-done (compare *isolation*). (2) For transactions: a group of writes that is either committed as a whole or rolled back as a whole, even when faults happen. | [Ch 7](../07-transactions/README.md) (Atomicity), [Ch 9](../09-consistency-and-consensus/README.md) (atomic commit and 2PC) |
| **backpressure** | Making a data producer slow down because the receiver can't keep up. Another name for it is *flow control*. | [Ch 11](../11-stream-processing/README.md) (Messaging Systems) |
| **batch process** | A job that reads a fixed, usually large, input dataset and writes some other dataset as its output, leaving the input unchanged. | [Ch 10](../10-batch-processing/README.md) |
| **bounded** | Having a known maximum limit or size. Used, for instance, about network delays and about datasets. | [Ch 8](../08-trouble-with-distributed-systems/README.md) (timeouts and unbounded delays), [Ch 11](../11-stream-processing/README.md) (introduction) |
| **Byzantine fault** | A node misbehaving in an arbitrary way — for example, sending other nodes messages that contradict each other or are deliberately malicious. | [Ch 8](../08-trouble-with-distributed-systems/README.md) (Byzantine Faults) |
| **cache** | A component that keeps recently used data so that later reads of the same data are quicker. It normally holds only part of the data, so anything missing must be fetched from a slower underlying store that has the full copy. | General term (the book cites no single section); see e.g. [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **CAP theorem** | A theoretical result that is widely misunderstood and, in the book's view, not useful in practice. | [Ch 9](../09-consistency-and-consensus/README.md) (The CAP theorem) |
| **causality** | The dependency that exists when one event "happens before" another: a later event replies to, builds on, or can only be understood in light of an earlier one. | [Ch 5](../05-replication/README.md) (happens-before and concurrency), [Ch 9](../09-consistency-and-consensus/README.md) (Ordering and Causality) |
| **consensus** | The core distributed-computing problem of getting several nodes to agree on something, such as which node leads a database cluster. It is much harder than it first appears. | [Ch 9](../09-consistency-and-consensus/README.md) (Fault-Tolerant Consensus) |
| **data warehouse** | A database that combines data from several OLTP systems and prepares it for analytics. | [Ch 3](../03-storage-and-retrieval/README.md) (Data Warehousing) |
| **declarative** | Stating which properties the result should have, rather than the exact steps for producing it. For queries, a query optimizer receives the declarative query and chooses the best way to run it. | [Ch 2](../02-data-models-and-query-languages/README.md) (Query Languages for Data) |
| **denormalize** | Deliberately adding some redundancy or duplication to a normalized dataset — usually as a cache or index — so reads are faster. A denormalized value is effectively a precomputed query result, much like a materialized view. | [Ch 7](../07-transactions/README.md) (single- and multi-object operations), [Ch 11](../11-stream-processing/README.md) (deriving several views from one event log) |
| **derived data** | A dataset produced from other data by a repeatable process that you could rerun if needed. It usually exists to make some specific kind of read fast. Indexes, caches and materialized views are examples. | Part III intro — [Ch 10](../10-batch-processing/README.md) overview; also [Ch 12](../12-future-of-data-systems/README.md) |
| **deterministic** | Describes a function that, given the same input, always returns the same output. It therefore can't rely on random numbers, the current time, network calls, or anything else unpredictable. | General term (no section cited); used in [Ch 10](../10-batch-processing/README.md), [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **distributed** | Running on several nodes that talk over a network. The hallmark is partial failure: one part may be broken while others work, and the software often can't tell exactly what has broken. | [Ch 8](../08-trouble-with-distributed-systems/README.md) (Faults and Partial Failures) |
| **durable** | Stored in a way that you trust will survive various faults without being lost. | [Ch 7](../07-transactions/README.md) (Durability) |
| **ETL** | Extract–Transform–Load: pulling data out of a source database, reshaping it to suit analytic queries, and loading it into a data warehouse or batch processing system. | [Ch 3](../03-storage-and-retrieval/README.md) (Data Warehousing) |
| **failover** | In a single-leader system, handing the leader role over from one node to another. | [Ch 5](../05-replication/README.md) (Handling Node Outages) |
| **fault-tolerant** | Able to recover by itself when something breaks, such as a machine crashing or a network link going down. | [Ch 1](../01-reliable-scalable-maintainable/README.md) (Reliability) |
| **flow control** | Same thing as *backpressure*. | [Ch 11](../11-stream-processing/README.md) |
| **follower** | A replica that never takes writes directly from clients; it only applies the data changes it receives from a leader. Also called a secondary, slave, read replica or hot standby. | [Ch 5](../05-replication/README.md) (Leaders and Followers) |
| **full-text search** | Searching text for arbitrary keywords, often with extras like matching words that are spelled similarly, or synonyms. A full-text index is a type of secondary index that supports such searches. | [Ch 3](../03-storage-and-retrieval/README.md) (full-text search and fuzzy indexes) |
| **graph** | A data structure made of vertices (things you can refer to; also called nodes or entities) and edges (links from one vertex to another; also called relationships or arcs). | [Ch 2](../02-data-models-and-query-languages/README.md) (Graph-Like Data Models) |
| **hash** | A function that converts an input into a number that looks random. The same input always yields the same number. Different inputs almost always yield different numbers, but occasionally two inputs give the same output — a *collision*. | [Ch 6](../06-partitioning/README.md) (Partitioning by Hash of Key) |
| **idempotent** | An operation that is safe to retry: running it several times has the same effect as running it once. | [Ch 11](../11-stream-processing/README.md) (Idempotence) |
| **index** | A data structure that lets you quickly find all records having a given value in a given field. | [Ch 3](../03-storage-and-retrieval/README.md) (Data Structures That Power Your Database) |
| **isolation** | For transactions: how much transactions running at the same time can interfere with one another. Serializable isolation is the strongest guarantee, but weaker levels are also used. | [Ch 7](../07-transactions/README.md) (Isolation) |
| **join** | Combining records that share something. Most often, one record refers to another (via a foreign key, a document reference, or a graph edge) and the query must fetch the record being referred to. | [Ch 2](../02-data-models-and-query-languages/README.md) (many-to-one and many-to-many relationships), [Ch 10](../10-batch-processing/README.md) (reduce-side joins and grouping) |
| **leader** | When data or a service is replicated over several nodes, the one replica designated as allowed to make changes. It may be chosen by an election protocol or appointed by an administrator. Also called primary or master. | [Ch 5](../05-replication/README.md) (Leaders and Followers) |
| **linearizable** | Acting as though the system held just one copy of the data, changed only by atomic operations. | [Ch 9](../09-consistency-and-consensus/README.md) (Linearizability) |
| **locality** | A performance technique: storing pieces of data together when they are often needed together. | [Ch 2](../02-data-models-and-query-languages/README.md) (data locality for queries) |
| **lock** | A mechanism guaranteeing that only one thread, node or transaction can use something at a time; anyone else wanting it has to wait until it is released. | [Ch 7](../07-transactions/README.md) (Two-Phase Locking), [Ch 8](../08-trouble-with-distributed-systems/README.md) (the leader and the lock) |
| **log** | A file you can only append to, used to store data. Examples: a write-ahead log makes a storage engine crash-safe; a log-structured storage engine uses logs as its main storage format; a replication log copies writes from leader to followers; an event log can represent a data stream. | [Ch 3](../03-storage-and-retrieval/README.md) (B-tree reliability; SSTables and LSM-Trees), [Ch 5](../05-replication/README.md) (Leaders and Followers), [Ch 11](../11-stream-processing/README.md) (Partitioned Logs) |
| **materialize** | To compute something eagerly and store the result, instead of calculating it only when someone asks. | [Ch 3](../03-storage-and-retrieval/README.md) (data cubes and materialized views), [Ch 10](../10-batch-processing/README.md) (materialization of intermediate state) |
| **node** | One running instance of some software on a computer, which communicates with other nodes over a network to get a job done. | General term (no section cited); used throughout Part II ([Ch 5](../05-replication/README.md)–[Ch 9](../09-consistency-and-consensus/README.md)) |
| **normalized** | Organised so that nothing is stored redundantly. When a fact changes in a normalized database, you update it in exactly one place instead of in many copies. | [Ch 2](../02-data-models-and-query-languages/README.md) (many-to-one and many-to-many relationships) |
| **OLAP** | Online analytic processing: a workload dominated by aggregations (count, sum, average, etc.) across very many records. | [Ch 3](../03-storage-and-retrieval/README.md) (Transaction Processing or Analytics?) |
| **OLTP** | Online transaction processing: a workload of quick queries that each read or write a few records, typically looked up by key. | [Ch 3](../03-storage-and-retrieval/README.md) (Transaction Processing or Analytics?) |
| **partitioning** | Breaking a dataset or computation that is too big for one machine into smaller pieces spread across many machines. Also called sharding. | [Ch 6](../06-partitioning/README.md) |
| **percentile** | A way to describe how values are distributed by counting how many fall above or below a threshold. E.g., the 95th percentile response time over some period is the time *t* such that 95% of requests in that period finished faster than *t* and 5% took longer. | [Ch 1](../01-reliable-scalable-maintainable/README.md) (Describing Performance) |
| **primary key** | A value (usually a number or string) that uniquely identifies one record. In many applications the system generates it when the record is created (e.g., sequentially or at random), rather than a user choosing it. Contrast with *secondary index*. | General term (no section cited); see [Ch 3](../03-storage-and-retrieval/README.md) |
| **quorum** | The minimum number of nodes that must vote for an operation before it counts as successful. | [Ch 5](../05-replication/README.md) (quorums for reading and writing) |
| **rebalance** | Moving data or services between nodes so the load is spread evenly. | [Ch 6](../06-partitioning/README.md) (Rebalancing Partitions) |
| **replication** | Keeping copies of the same data on several nodes (replicas) so it is still reachable when a node can't be reached. | [Ch 5](../05-replication/README.md) |
| **schema** | A description of how some data is structured, including its fields and their types. Data can be checked against a schema at different points in its life, and a schema may change over time. | [Ch 2](../02-data-models-and-query-languages/README.md) (schema flexibility in the document model), [Ch 4](../04-encoding-and-evolution/README.md) |
| **secondary index** | An extra data structure maintained next to the main data store that lets you efficiently find records matching some condition. | [Ch 3](../03-storage-and-retrieval/README.md) (Other Indexing Structures), [Ch 6](../06-partitioning/README.md) (Partitioning and Secondary Indexes) |
| **serializable** | A guarantee that transactions running concurrently produce the same outcome as if they had run one after another in some order. | [Ch 7](../07-transactions/README.md) (Serializability) |
| **shared-nothing** | An architecture of independent nodes, each with its own CPU, memory and disks, linked by an ordinary network — as opposed to shared-memory or shared-disk designs. | Part II intro — [Ch 5](../05-replication/README.md) overview |
| **skew** | Two meanings. (1) Uneven load across partitions: some get lots of data or requests while others get little; also called hot spots. (2) A timing anomaly that makes events appear in an unexpected, out-of-sequence order — see read skew, write skew and clock skew. | (1) [Ch 6](../06-partitioning/README.md) (skewed workloads and hot spots), [Ch 10](../10-batch-processing/README.md) (handling skew); (2) [Ch 7](../07-transactions/README.md) (read skew in snapshot isolation; write skew and phantoms), [Ch 8](../08-trouble-with-distributed-systems/README.md) (clock skew in timestamps for ordering events) |
| **split brain** | Two nodes both believing, at the same moment, that they are the leader — which can break the system's guarantees. | [Ch 5](../05-replication/README.md) (Handling Node Outages), [Ch 8](../08-trouble-with-distributed-systems/README.md) (the truth is defined by the majority) |
| **stored procedure** | Packaging a transaction's logic so it runs entirely on the database server, with no back-and-forth to the client during the transaction. | [Ch 7](../07-transactions/README.md) (Actual Serial Execution) |
| **stream process** | A computation that keeps running forever, reading an endless stream of events and producing some output from them. | [Ch 11](../11-stream-processing/README.md) |
| **synchronous** | The opposite of *asynchronous*: waiting for the thing to complete. | [Ch 5](../05-replication/README.md), [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **system of record** | The system that holds the main, authoritative version of some data — the "source of truth". Changes go here first, and other datasets can be derived from it. | Part III intro — [Ch 10](../10-batch-processing/README.md) overview; also [Ch 12](../12-future-of-data-systems/README.md) |
| **timeout** | One of the simplest ways to detect a fault: noticing that no response arrived within some time. You can't tell, though, whether the remote node failed or the network had a problem. | [Ch 8](../08-trouble-with-distributed-systems/README.md) (Timeouts and Unbounded Delays) |
| **total order** | An ordering of items (e.g., timestamps) in which every pair can be compared, so you can always tell which of the two comes first. If some pairs can't be compared, it is called a *partial order*. | [Ch 9](../09-consistency-and-consensus/README.md) (the causal order is not a total order) |
| **transaction** | Bundling several reads and writes into one logical unit so that error handling and concurrency become simpler to deal with. | [Ch 7](../07-transactions/README.md) |
| **two-phase commit (2PC)** | An algorithm that makes sure several database nodes either all commit a transaction or all abort it. | [Ch 9](../09-consistency-and-consensus/README.md) (Atomic Commit and Two-Phase Commit) |
| **two-phase locking (2PL)** | An algorithm for serializable isolation in which a transaction takes a lock on every piece of data it reads or writes and keeps those locks until it finishes. | [Ch 7](../07-transactions/README.md) (Two-Phase Locking) |
| **unbounded** | Having no known maximum limit or size; the opposite of *bounded*. | [Ch 8](../08-trouble-with-distributed-systems/README.md), [Ch 11](../11-stream-processing/README.md) |

## Every other term used in these notes (A–Z)

The table above reproduces only the book's own glossary. This section covers the rest: every technical term, acronym, algorithm, product and tool that appears in the Preface and Chapters 1–12 of these notes, in alphabetical order, so a newcomer never meets an unexplained word. Each entry gives a short plain-English meaning and links to the chapter(s) where the term is used most. Terms already in the table above (such as *quorum* or *two-phase commit*) are not repeated here.

### 0–9

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **2PC** | Short for two-phase commit (see the table above). | [Ch 9](../09-consistency-and-consensus/README.md) |
| **3PC (three-phase commit)** | A variant of two-phase commit designed to avoid blocking, but only correct if the network has bounded delays and failures can be detected perfectly — assumptions real networks don't meet. | [Ch 9](../09-consistency-and-consensus/README.md) |

### A

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **Abbreviated keys** | Storing only enough of each key in interior B-tree pages to tell ranges apart, so more keys fit per page. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Abort / rollback** | Cancelling a transaction so that none of its writes take effect. | [Ch 7](../07-transactions/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Abstraction** | A simpler interface that hides complicated details underneath, like SQL hiding how data is laid out on disk. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **A/B test** | Showing two versions of a feature to different groups of users and comparing which one performs better. | [Ch 10](../10-batch-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Access path** | The route a query takes to reach the data (which indexes, which order of lookups); chosen by hand in old databases, by the query optimizer in relational ones. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Accidental complexity** | Complexity caused by how something was built rather than by the problem itself; good abstractions remove it. | [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **ACID** | The classic transaction guarantees: Atomicity, Consistency, Isolation, Durability. | [Ch 7](../07-transactions/README.md) |
| **Acknowledgment (ack)** | A message from a consumer telling the broker "I have processed this", so the broker can delete it instead of redelivering it. | [Ch 9](../09-consistency-and-consensus/README.md), [Ch 11](../11-stream-processing/README.md) |
| **ActiveMQ / RabbitMQ / HornetQ** | Traditional open-source message brokers (JMS/AMQP style) that delete messages once consumers acknowledge them. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Activity events / clickstream** | Logs of what users did (pages viewed, buttons clicked), often joined with user data in batch jobs. | [Ch 10](../10-batch-processing/README.md) |
| **Actor model** | A concurrency model where independent "actors" each keep private state and communicate only by sending each other asynchronous messages. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Actual serial execution** | Running transactions literally one at a time on a single thread, which is simple and serializable if each transaction is short. | [Ch 7](../07-transactions/README.md) |
| **Admission control** | Limiting how much traffic is let into a network or system so that what is admitted gets guaranteed capacity. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Aggregation** | Combining many values into one summary, such as a count, sum, average or maximum. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Aggregation pipeline** | MongoDB's JSON-based query language that passes documents through a series of stages (filter, group, sort...). | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Agile / DevOps** | Agile: working in short iterations and adapting to change. DevOps: developers and operations working together, with heavy automation. | [Ch 10](../10-batch-processing/README.md) |
| **Ajax** | A technique where browser JavaScript calls the server in the background to update part of a page without reloading it. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Akka** | A framework for the actor model on the Java virtual machine, which can run actors across several machines. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Algorithmic prison** | Being systematically shut out of jobs, loans, travel and so on because an algorithm labelled you risky. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Amazon EBS** | Amazon's network-attached virtual disks for EC2 machines; reads and writes go over the network. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Amazon S3** | Amazon's cloud object storage service for files of any size. | [Ch 10](../10-batch-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Amazon Web Services (AWS)** | Amazon's cloud computing platform (EC2 virtual machines, S3 storage, EBS disks and more). | [Preface](../00-preface/README.md), [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **AMQP** | Advanced Message Queuing Protocol: an open standard for talking to message brokers such as RabbitMQ. | [Ch 11](../11-stream-processing/README.md) |
| **Anti-caching** | An in-memory database design that moves rarely used records out to disk and back when needed, so the dataset can exceed memory. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 7](../07-transactions/README.md) |
| **Anti-entropy** | A background process that compares replicas and copies over any data that one of them is missing. | [Ch 5](../05-replication/README.md) |
| **Apache Beam / Google Cloud Dataflow** | Beam is a programming model for batch and stream jobs that can run on several engines; Cloud Dataflow is Google's hosted engine for it. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Apache BookKeeper** | A replicated log storage service built on ZooKeeper. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Apache Curator** | A library of ready-made recipes (leader election, locks) on top of ZooKeeper. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Apache Jena** | An open-source Java framework for RDF data and SPARQL queries. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **API** | Application programming interface: the operations one piece of software offers for other software to call. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **API key** | A secret token a client sends with each request to identify and authorise itself. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **API versioning** | Running several versions of a service interface side by side (e.g., /v1 and /v2) so old clients keep working. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Append-only** | Data that is only ever added to the end, never changed in place. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Archival storage** | Long-term copies of data (backups, warehouse dumps) usually written with the latest schema in one go. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **ASCII** | The basic 7-bit character set for English letters, digits and punctuation. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 10](../10-batch-processing/README.md) |
| **ASN.1 / DER** | An old, very complex standard for defining data structures and encoding them in binary; still used for TLS/SSL certificates. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Asymmetric fault** | A failure where a node can receive messages but its outgoing messages are lost or delayed, so others wrongly think it's dead. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Asynchronous packet network** | A network (like the internet) that gives no guarantee when, or whether, a packet will arrive. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **ATM (Asynchronous Transfer Mode)** | An older networking technology that reserved bandwidth like telephone circuits. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Atomic (caesium) clock** | An extremely precise clock based on atomic vibrations; used with GPS in Google's TrueTime. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Atomic commit** | Ensuring a transaction spread across several nodes either commits on all of them or aborts on all of them. | [Ch 9](../09-consistency-and-consensus/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Atomicity (ACID)** | All of a transaction's writes happen, or none do; on failure, partial writes are undone. | [Ch 7](../07-transactions/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Atomic write operation** | A database operation, like UPDATE x = x + 1, that reads and changes a value in one step so concurrent updates aren't lost. | [Ch 7](../07-transactions/README.md) |
| **Auditing** | Actively checking that data is still correct and complete, rather than just trusting the system. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Authentication / access control** | Checking who someone is (authentication) and what they are allowed to do (access control). | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Availability** | The fraction of time a system is up and answering requests. | [Ch 5](../05-replication/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Avro** | A binary encoding format from Hadoop that uses a schema; reader and writer schemas can differ and are matched by field name. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Azure** | Microsoft's cloud computing platform. | [Ch 5](../05-replication/README.md), [Ch 11](../11-stream-processing/README.md) |

### B

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **Backup** | A copy of the data kept so it can be restored after loss or corruption. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 7](../07-transactions/README.md) |
| **Backward compatibility** | Newer code can read data written by older code. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **BASE** | Basically Available, Soft state, Eventual consistency: a loose label for "not ACID". | [Ch 7](../07-transactions/README.md) |
| **Base64** | A way of writing binary data using only 64 printable text characters, at about 33% size overhead. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Bash / shell** | Bash is the most common Unix shell: the command-line program that runs commands and pipes them together. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Big ball of mud** | A system so tangled that it's hard to understand or change. | [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **Big Data** | A loose label for datasets too large or fast-moving for one ordinary machine. | [Preface](../00-preface/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Big-O notation (O(n), O(log n))** | A way to say how cost grows with data size: O(n) grows in step with n; O(log n) grows very slowly. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **BigQuery / Dremel** | Google's columnar analytic query engine (Dremel) and the cloud service built on it (BigQuery). | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Bigtable** | Google's distributed wide-column storage system, which inspired HBase and Cassandra's data model. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 6](../06-partitioning/README.md) |
| **Binary encoding** | Storing data in compact bytes rather than human-readable text. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Binary string** | A sequence of raw bytes (not necessarily text). | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Bitcask** | Riak's default storage engine: an append-only log plus an in-memory hash map from each key to its file offset. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Bitcoin / blockchain** | A cryptocurrency whose replicas keep a shared, append-only chain of blocks of transactions secured with hashes and proof of work. | [Ch 8](../08-trouble-with-distributed-systems/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Bitmap encoding** | In column storage, representing each distinct value as a string of bits (1 = this row has the value), which compresses very well. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Blocking atomic commit** | The flaw of 2PC: if the coordinator crashes, participants may have to wait indefinitely for its decision. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Bloom filter** | A small memory structure that can say "definitely not present" or "maybe present", used to skip pointless disk lookups. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Bottled Water / Maxwell / Debezium** | Open-source CDC tools that read a database's replication log (PostgreSQL, MySQL) and publish the changes to Kafka. | [Ch 11](../11-stream-processing/README.md) |
| **Bounded delay** | A guarantee that every message arrives within some known maximum time; not provided by ordinary networks. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Branching factor** | How many child pages each page of a B-tree points to (often several hundred). | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Branch misprediction** | When the CPU guesses wrongly which way an if-statement will go and has to discard work; vectorized code avoids many. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Broadcast hash join** | A map-side join where the smaller dataset is loaded into an in-memory hash table on every mapper. | [Ch 10](../10-batch-processing/README.md) |
| **Brokerless messaging** | Producers sending messages straight to consumers with no intermediary server (e.g., ZeroMQ, UDP multicast). | [Ch 11](../11-stream-processing/README.md) |
| **BSON** | MongoDB's binary encoding of JSON documents. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 4](../04-encoding-and-evolution/README.md) |
| **B-tree** | The most common index structure: a tree of fixed-size pages kept sorted by key, updated in place on disk. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **B+ tree** | A B-tree variant that keeps all values in the leaf pages; most "B-trees" in practice are B+ trees. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Bulk loading** | Building a database's files directly in a batch job and swapping them in at once, instead of writing records one by one. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Bulk synchronous parallel (BSP) / Pregel** | A model for graph processing in rounds: each vertex processes its incoming messages, then sends messages along edges for the next round. | [Ch 10](../10-batch-processing/README.md) |
| **Bursty traffic** | Network traffic that comes in sudden spikes rather than at an even rate. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Business intelligence (BI)** | Using data analysis to support business decisions, typically via reports and dashboards over a data warehouse. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Byte offset** | The position of a record in a file, counted in bytes from the start. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Byzantine fault-tolerant** | Still working correctly even if some nodes misbehave arbitrarily or maliciously. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Byzantine Generals Problem** | The classic puzzle of reaching agreement when some participants may be traitors sending conflicting messages. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |

### C

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **Cache hit / cache miss** | A hit means the requested data was found in the cache; a miss means it had to be fetched from the slower store. | [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **Cache invalidation** | Removing or updating cached copies when the underlying data changes so stale data isn't served. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 7](../07-transactions/README.md) |
| **Capacity planning** | Estimating future load and making sure enough hardware is available in time. | [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **Cascading** | A Java library that builds MapReduce workflows from higher-level operators such as joins and groupings. | [Ch 10](../10-batch-processing/README.md) |
| **Cascading failure** | One component's failure overloads others, which then fail too, and so on. | [Ch 6](../06-partitioning/README.md), [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Cassandra** | An open-source, Dynamo-style leaderless database with a wide-column data model, designed for many writes. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 6](../06-partitioning/README.md) |
| **Catch-up recovery** | A restarted follower reading the leader's log from where it stopped until it has caught up. | [Ch 5](../05-replication/README.md) |
| **Caught up** | A follower that has applied every change the leader has made so far. | [Ch 5](../05-replication/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Causal context** | Riak's name for the version vector a client sends back with a write so the database can tell what it already saw. | [Ch 5](../05-replication/README.md) |
| **Causal dependency** | Event B depends on event A (B happened because of, or after seeing, A), so B must not be processed as if A hadn't happened. | [Ch 5](../05-replication/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Causally consistent** | A consistency model guaranteeing everyone sees cause-and-effect events in the right order; concurrent events may appear in any order. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **CDC (change data capture)** | Reading a database's stream of changes so other systems can apply exactly the same changes. | [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **CDN (content delivery network)** | A worldwide network of cache servers that serves static content from a location near each user. | [Ch 6](../06-partitioning/README.md) |
| **CEP engines (Esper, StreamBase, SQLstream, Apama)** | Products that run continuous pattern-matching queries over event streams. | [Ch 11](../11-stream-processing/README.md) |
| **Certificate transparency** | A public, Merkle-tree-based log used to check that TLS/SSL certificates were issued legitimately. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Chain replication** | Replication where writes flow along a chain of nodes and reads are served by the tail, giving strong consistency. | [Ch 5](../05-replication/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Changelog** | A log of every change made to some state; replaying it rebuilds the state. | [Ch 11](../11-stream-processing/README.md) |
| **Change stream / change feed** | An API that lets clients subscribe to every change in a database as it happens. | [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Chaos Monkey** | A Netflix tool that deliberately kills machines in production to make sure the system tolerates faults. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Checkpoint** | A saved snapshot of a computation's state and progress so it can resume from there after a crash instead of restarting. | [Ch 10](../10-batch-processing/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Checksum** | A short value computed from data; if the data changes or is corrupted, the checksum no longer matches. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Chubby** | Google's internal lock and coordination service, which inspired ZooKeeper. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Circuit switching** | Reserving a fixed amount of network bandwidth for a whole call, as old telephone networks did; gives bounded delay. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Client / server** | A client sends requests; a server answers them. A server may itself be a client of other services. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 5](../05-replication/README.md) |
| **Clock drift** | A clock running slightly faster or slower than real time, so it slowly wanders off. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Clock skew** | The difference between what two machines' clocks say at the same moment. | [Ch 5](../05-replication/README.md), [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Clos topology** | A network layout with many redundant paths between switches, common in datacenters. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Cloud computing** | Renting computing resources (machines, storage, managed services) over the internet on demand. | [Ch 8](../08-trouble-with-distributed-systems/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Cluster** | A group of machines working together as one system. | [Ch 6](../06-partitioning/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Clustered index** | An index that stores the whole row inside the index itself, rather than pointing elsewhere. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **COBOL** | An old business programming language, used with early hierarchical and network databases. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **CODASYL / network model** | An old (1970s) database model where records link to each other in a graph that programs navigate by following pointers. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Code generation** | Producing source code classes automatically from a schema, as Thrift and Protocol Buffers do. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Column-oriented storage** | Storing all values of each column together instead of each row together; fast for analytics that read a few columns of many rows. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Command** | In event sourcing, a user's request that may still be rejected; once validated it becomes an immutable event (a fact). | [Ch 11](../11-stream-processing/README.md) |
| **Commit** | Finishing a transaction so that all its writes become permanent and visible. | [Ch 7](../07-transactions/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Commit point** | The moment in 2PC when the coordinator writes its commit decision to disk; after that, the transaction must commit. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Commodity machines** | Ordinary, inexpensive servers, as opposed to specialised supercomputer hardware. | [Ch 8](../08-trouble-with-distributed-systems/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Commutative operation** | An operation where order doesn't matter (adding 3 then 5 equals adding 5 then 3), making conflicts easy to merge. | [Ch 7](../07-transactions/README.md) |
| **Compaction** | Throwing away overwritten or deleted values in log files, keeping only the latest value per key. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Compare-and-set (CAS)** | An atomic operation: "set this value to X only if it is currently Y", else fail. | [Ch 7](../07-transactions/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Compensating transaction** | A later action that corrects an earlier mistake (refund, apology, rebooking) instead of preventing it up front. | [Ch 9](../09-consistency-and-consensus/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Complex event processing (CEP)** | Searching a stream for patterns of events described by queries that run continuously. | [Ch 11](../11-stream-processing/README.md) |
| **Compound primary key** | A key made of several columns, e.g., (user_id, timestamp): the first part picks the partition, the rest sorts within it. | [Ch 6](../06-partitioning/README.md) |
| **Compute-intensive application** | An application whose bottleneck is raw CPU work rather than data. | [Preface](../00-preface/README.md), [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **Concatenated index** | An index on several fields combined in a fixed order, like a phone book sorted by (last name, first name). | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 6](../06-partitioning/README.md) |
| **Concurrency** | Several operations being in progress at overlapping times. | [Ch 5](../05-replication/README.md), [Ch 7](../07-transactions/README.md) |
| **Concurrency control** | Mechanisms (locks, versioning, validation) that stop concurrent transactions from corrupting data. | [Ch 7](../07-transactions/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Concurrent operations** | Two operations where neither knew about the other when it started — neither "happened before" the other. | [Ch 5](../05-replication/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Confidence interval (clocks)** | A range of time a clock reading is known to fall within, such as Spanner's TrueTime [earliest, latest]. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Conflict avoidance** | Routing all writes for a given record to the same leader so write conflicts can't happen. | [Ch 5](../05-replication/README.md) |
| **Conflict resolution** | Deciding what the final value should be when two concurrent writes changed the same data. | [Ch 5](../05-replication/README.md), [Ch 7](../07-transactions/README.md) |
| **Consensus properties (uniform agreement, integrity, validity, termination)** | The rules consensus must satisfy: no two nodes decide differently, nobody decides twice, the decision was proposed by someone, and every live node eventually decides. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Consistency (ACID)** | The application's rules (invariants) about the data hold after every transaction; really a property of the application. | [Ch 7](../07-transactions/README.md) |
| **Consistency model** | A precise statement of which results reads may return when there are concurrent writes and replicas. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Consistent hashing** | A way of mapping keys to nodes so that adding or removing a node moves only a small share of keys. | [Ch 6](../06-partitioning/README.md) |
| **Consistent prefix reads** | A guarantee that if writes happened in some order, readers see them in that same order (never an answer before its question). | [Ch 5](../05-replication/README.md) |
| **Consul** | HashiCorp's tool for service discovery and configuration, built on Raft. | [Ch 9](../09-consistency-and-consensus/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Consumer** | A program that reads and processes messages or events from a stream or queue. | [Ch 11](../11-stream-processing/README.md) |
| **Consumer group** | A set of consumers sharing the work of reading a topic, each taking some partitions. | [Ch 11](../11-stream-processing/README.md) |
| **Consumer offset** | The position in a log partition up to which a consumer has processed messages. | [Ch 11](../11-stream-processing/README.md) |
| **Contention** | Many transactions or threads competing for the same data or lock at once. | [Ch 7](../07-transactions/README.md) |
| **Context switch** | The CPU switching from running one thread to another, saving and restoring their state. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Controller node** | A node responsible for deciding which node is leader (or owns which partition). | [Ch 5](../05-replication/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Convergent conflict resolution** | Every replica resolves a conflict the same way, so all end up with the same final value. | [Ch 5](../05-replication/README.md) |
| **Coordination-avoiding data system** | A system that keeps data integrity without synchronous cross-node coordination. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Coordinator node (leaderless)** | In leaderless replication, the node that receives a client's request and forwards it to the replicas (without enforcing an order). | [Ch 5](../05-replication/README.md) |
| **Coordinator (transaction manager)** | The component that runs two-phase commit, asking participants to prepare and then telling them to commit or abort. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Copy-on-write** | Instead of changing data in place, write a modified copy and switch pointers to it, leaving the old version intact. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 7](../07-transactions/README.md) |
| **CORBA / DCOM / RMI / EJB** | Older RPC frameworks for calling objects on remote machines; mostly abandoned because they hid network problems. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Cost-based query optimizer** | An optimizer that estimates the cost of each possible execution plan and picks the cheapest. | [Ch 10](../10-batch-processing/README.md) |
| **Couchbase** | A distributed document/key-value database descended from Memcached and CouchDB. | [Ch 6](../06-partitioning/README.md) |
| **CouchDB** | An open-source document database that stores JSON and supports multi-leader replication. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 5](../05-replication/README.md) |
| **Covering index** | An index that stores some extra columns so certain queries can be answered from the index alone. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **CPU** | Central processing unit: the chip that executes program instructions. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **CQRS** | Command Query Responsibility Segregation: keeping the write model separate from (possibly many) read models. | [Ch 11](../11-stream-processing/README.md) |
| **Crash-recovery fault** | A fault model where nodes may crash and later restart, keeping what was on disk. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Crash-stop fault** | A fault model where a crashed node is gone forever. | [Ch 8](../08-trouble-with-distributed-systems/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **CRDT** | Conflict-free replicated data type: a data structure (counter, set, list...) that merges concurrent edits automatically and sensibly. | [Ch 5](../05-replication/README.md) |
| **Cross-device read-after-write** | Read-your-writes across a user's devices: an update made on a laptop must be visible on their phone. | [Ch 5](../05-replication/README.md) |
| **Cross-partition transaction** | A transaction touching data on several partitions, which needs coordination between them. | [Ch 7](../07-transactions/README.md) |
| **C-Store** | A research column-store database (the ancestor of Vertica) that kept data sorted in several orders. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **CSV** | Comma-separated values: a simple text format for tables, ambiguous about types and escaping. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 4](../04-encoding-and-evolution/README.md) |
| **curl** | A command-line tool for making HTTP requests. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Cursor stability** | Locking a row while it is being read and modified, to prevent lost updates. | [Ch 7](../07-transactions/README.md) |
| **Cypher** | The declarative query language of the Neo4j graph database. | [Ch 2](../02-data-models-and-query-languages/README.md) |

### D

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **Daemon** | A background program that runs continuously on a server (e.g., the HDFS process on each machine). | [Ch 6](../06-partitioning/README.md), [Ch 10](../10-batch-processing/README.md) |
| **DAG (directed acyclic graph)** | A graph of steps with arrows and no cycles; dataflow engines run jobs as DAGs of operators. | [Ch 10](../10-batch-processing/README.md) |
| **Database inside-out** | Another name for the unbundling / dataflow approach to building systems from logs and derived views. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Database-internal distributed transaction** | A distributed transaction where all participants run the same database software, so it can be optimised. | [Ch 9](../09-consistency-and-consensus/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Data broker** | A company that buys, combines, infers and resells personal data. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Databus / Wormhole / Sherpa** | CDC systems built at LinkedIn (Databus), Facebook (Wormhole) and Yahoo! (Sherpa). | [Ch 11](../11-stream-processing/README.md) |
| **Datacenter** | A building full of servers, networking and power equipment. | [Ch 5](../05-replication/README.md), [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Data cube / OLAP cube** | A precomputed grid of aggregates over several dimensions, so totals can be read instantly. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Data exhaust** | A label for behavioural data produced as a side effect of using a service. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Dataflow** | The path data takes as it is passed and transformed between processes or systems. | [Ch 10](../10-batch-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Dataflow engine** | A batch processor like Spark, Tez or Flink that runs a whole workflow as one job without writing every intermediate step to disk. | [Ch 10](../10-batch-processing/README.md) |
| **Data integration** | Making sure the same data ends up, in the right shape, in every system that needs it. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Data-intensive application** | An application whose main challenge is the amount, complexity or speed of change of its data, not CPU power. | [Preface](../00-preface/README.md), [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **Data lake** | A big store (often HDFS or object storage) where raw data of any format is dumped first and interpreted later. | [Ch 10](../10-batch-processing/README.md) |
| **Data locality** | Running computation on the machine that already stores the input, to avoid copying it over the network. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Datalog** | An old logic query language (a subset of Prolog) that builds queries from small rules. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Data model** | How data is organised and described to the programmer: tables, documents, graphs and so on. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Data outlives code** | Code is replaced in minutes, but stored data may stay years old, so new code must keep reading old data. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Data system** | The book's umbrella term for databases, caches, queues and other tools that store and process data. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Datomic** | A database that keeps all history as immutable facts and supports Datalog queries. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 7](../07-transactions/README.md) |
| **DB2** | IBM's commercial relational database. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 7](../07-transactions/README.md) |
| **Deadlock** | Two transactions each waiting for a lock the other holds, so neither can proceed; the database aborts one. | [Ch 7](../07-transactions/README.md) |
| **Declarative query language** | A language (like SQL) where you say what result you want and the system decides how to compute it. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Decoding (deserialization, parsing)** | Turning bytes back into in-memory objects. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Denial of service** | An attack (or accident) that floods a system so it can't serve legitimate users. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Derivation function** | The code that turns source data into a derived dataset (e.g., "extract the field and sort it" for an index). | [Ch 12](../12-future-of-data-systems/README.md) |
| **Dimension table** | In a star schema, a table describing the who/what/where of events (products, stores, dates). | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Dirty read** | Seeing another transaction's writes before it has committed. | [Ch 7](../07-transactions/README.md) |
| **Dirty write** | Overwriting another transaction's uncommitted write. | [Ch 7](../07-transactions/README.md) |
| **Distributed actor framework** | A system (Akka, Orleans, Erlang/OTP) that runs actors across several machines with the same message-passing model. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Distributed filesystem** | A filesystem whose files are spread across many machines (e.g., HDFS). | [Ch 10](../10-batch-processing/README.md) |
| **Distributed RPC** | Storm's feature for sending a query through a stream topology and gathering results from many partitions. | [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Django / ActiveRecord / Hibernate** | Popular ORM frameworks for Python (Django), Ruby on Rails (ActiveRecord) and Java (Hibernate). | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 7](../07-transactions/README.md) |
| **DNS** | Domain Name System: translates names like example.com into IP addresses. | [Ch 6](../06-partitioning/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Docker / Mesos** | Tools for packaging programs into containers (Docker) and scheduling them across a cluster (Mesos). | [Ch 10](../10-batch-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Document database** | A database storing self-contained records as JSON-like documents (e.g., MongoDB). | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Document-partitioned (local) index** | Each partition keeps a secondary index of only its own records; reads must ask all partitions. | [Ch 6](../06-partitioning/README.md) |
| **Document reference** | A field in one document holding the ID of another, the document-model equivalent of a foreign key. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 10](../10-batch-processing/README.md) |
| **DOM** | Document Object Model: the browser's tree of page elements that JavaScript can change. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Domain-driven design (DDD)** | A software design approach centred on modelling the business domain; event sourcing is popular in it. | [Ch 11](../11-stream-processing/README.md) |
| **Dotted version vector** | A refined version vector used by Riak to track causality precisely. | [Ch 5](../05-replication/README.md) |
| **Double-spending** | Spending the same money or credit twice due to a concurrency bug. | [Ch 7](../07-transactions/README.md) |
| **Drill-down / slicing and dicing** | Exploring analytic data by zooming into detail or viewing it by different dimensions. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Druid** | An open-source analytic database designed for fast queries over event data. | [Ch 11](../11-stream-processing/README.md) |
| **Dryad / Nephele** | Research dataflow engines that inspired Spark, Tez and Flink. | [Ch 10](../10-batch-processing/README.md) |
| **Dual writes** | Application code writing the same change to two systems itself; it can leave them permanently inconsistent. | [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Duration** | An amount of elapsed time (e.g., how long a request took), as opposed to a point in time. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Dynamic membership** | Adding or removing nodes from a consensus group while it keeps running. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Dynamic partitioning** | Splitting a partition when it grows too big and merging small ones, so the number of partitions follows data volume. | [Ch 6](../06-partitioning/README.md) |
| **Dynamo** | Amazon's in-house leaderless key-value store, whose paper inspired Riak, Cassandra and Voldemort ("Dynamo-style"). | [Ch 5](../05-replication/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **DynamoDB** | Amazon's hosted cloud key-value/document database (different from the original Dynamo design). | [Ch 5](../05-replication/README.md), [Ch 6](../06-partitioning/README.md) |

### E

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **Edge** | A connection between two vertices in a graph. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Edit distance** | How many single-letter insertions, deletions or substitutions turn one word into another; used for fuzzy search. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Elasticsearch** | A popular open-source distributed search engine built on Lucene. | [Ch 6](../06-partitioning/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Elastic system** | A system that can automatically add or remove machines as load changes. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 6](../06-partitioning/README.md) |
| **Elm / React / Redux** | Front-end tools that model the user interface as a function of a stream of events and state changes. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Encapsulation** | Hiding a component's internal details behind a well-defined interface. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Encoding (serialization, marshalling)** | Turning in-memory objects into bytes for storage or sending over a network. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Encryption** | Scrambling data so only holders of the right key can read it. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **End-to-end argument** | Some guarantees (dedup, integrity, security) can only be fully provided by the endpoints of a communication, not by lower layers alone. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Ephemeral node** | A ZooKeeper entry that is deleted automatically when the client session that created it dies. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Epoch number (ballot / view / term)** | A counter that increases with each leader election, so messages from an old leader can be recognised and ignored. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Epoch (time)** | The fixed starting point clocks count from, usually midnight UTC on 1 January 1970. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Equi-join** | A join where records are matched on equal key values. | [Ch 10](../10-batch-processing/README.md) |
| **Erasure coding** | Storing data with extra computed parity pieces so lost parts can be rebuilt, using less space than full copies. | [Ch 10](../10-batch-processing/README.md) |
| **Erlang / OTP** | A programming language and framework built around lightweight processes (actors) and fault tolerance. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Error-correcting codes** | Extra bits stored or sent with data so that small errors can be detected and fixed automatically. | [Ch 8](../08-trouble-with-distributed-systems/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Espresso** | LinkedIn's distributed document database. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 6](../06-partitioning/README.md) |
| **Etcd** | An open-source coordination service and key-value store using the Raft consensus algorithm (used by Kubernetes). | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Ethernet** | The standard technology for wired local networks. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Etherpad / Google Docs** | Real-time collaborative editors where several people type into the same document at once. | [Ch 5](../05-replication/README.md) |
| **Event** | A small, immutable record that something happened at a certain time. | [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Event sourcing** | Storing every change as an immutable event in a log and deriving current state from those events. | [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Event time** | When an event actually happened, according to the device that produced it. | [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Eventual consistency** | If writes stop, all replicas will eventually hold the same data; until then reads may be stale. | [Ch 5](../05-replication/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Evolvability** | How easily a system can be changed to meet new requirements. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 4](../04-encoding-and-evolution/README.md) |
| **Exactly-once (effectively-once) semantics** | The end result is as if each message were processed once, even though retries may have happened. | [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Excision / shunning** | Actually deleting data from an immutable event history, e.g., for legal reasons. | [Ch 11](../11-stream-processing/README.md) |
| **Explicit locking** | The application locking rows itself (e.g., SELECT ... FOR UPDATE) to prevent lost updates. | [Ch 7](../07-transactions/README.md) |
| **Exponential backoff** | Waiting longer and longer (e.g., doubling) between retries so an overloaded system can recover. | [Ch 7](../07-transactions/README.md) |

### F

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **Fact table** | The huge central table of a star schema, one row per event (such as each sale). | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Failure** | The whole system stops providing its service to users (as opposed to a fault in one part). | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Fan-out** | One input causing many outputs: e.g., one tweet delivered to every follower's timeline, or one message sent to every consumer. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Fault** | One component deviating from its specification (a disk dying, a bug); a failure is when the whole system stops serving users. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Federated database / polystore** | One query interface that reads from many different underlying stores ("unifying reads"). | [Ch 12](../12-future-of-data-systems/README.md) |
| **Feedback loop** | A prediction changes the world in a way that reinforces the prediction. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Fencing / STONITH** | Forcibly cutting off or shutting down a node that may wrongly believe it is still leader ("Shoot The Other Node In The Head"). | [Ch 5](../05-replication/README.md) |
| **Fencing token** | A number that increases each time a lock or lease is granted; storage rejects writes carrying an older number. | [Ch 8](../08-trouble-with-distributed-systems/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Fibre Channel / SAN / NAS** | Specialised shared-storage hardware: a storage area network (SAN) or network-attached storage (NAS) reached over dedicated links. | [Ch 10](../10-batch-processing/README.md) |
| **Field tag** | A number that identifies a field in Thrift or Protocol Buffers encodings, instead of writing the field name. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Finite state automaton** | A small machine of states and transitions that accepts or rejects strings; Lucene uses one for fuzzy search. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Firebase / Meteor** | Application platforms that let clients subscribe to live database changes. | [Ch 11](../11-stream-processing/README.md) |
| **Firewall** | A network filter that blocks traffic not explicitly allowed. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Fixed number of partitions** | Creating many more partitions than nodes up front, then moving whole partitions between nodes when rebalancing. | [Ch 6](../06-partitioning/README.md) |
| **Flink** | Apache Flink: an open-source stream processor that also does batch jobs. | [Ch 10](../10-batch-processing/README.md), [Ch 11](../11-stream-processing/README.md) |
| **FLP result** | A proof that consensus can't always be reached in a purely asynchronous system if a node may crash; timeouts get round it in practice. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **FlumeJava / Crunch** | Libraries for writing batch pipelines as chains of high-level operations that compile down to MapReduce or Spark jobs. | [Ch 10](../10-batch-processing/README.md) |
| **Foreign key** | A column that refers to the primary key of a row in another table. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 7](../07-transactions/README.md) |
| **Forward compatibility** | Older code can read data written by newer code (ignoring fields it doesn't know). | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Forwarding pointer** | A note left in a heap file when a row moves, pointing to the row's new location. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **FoundationDB** | A distributed key-value store offering serializable ACID transactions. | [Ch 7](../07-transactions/README.md) |
| **Fractal tree** | A B-tree variant that buffers writes in internal pages to reduce disk I/O. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Fragmentation** | Free space scattered in small unusable gaps across disk pages. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **fsync** | The system call that forces data buffered in memory onto disk. | [Ch 7](../07-transactions/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Full table scan** | Reading every row of a table, without using an index. | [Ch 10](../10-batch-processing/README.md) |
| **Functional vs nonfunctional requirements** | Functional: what the system must do. Nonfunctional: qualities like security, reliability, scalability and compliance. | [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **Futures / promises** | Objects representing a result that will be available later, used for asynchronous calls. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 12](../12-future-of-data-systems/README.md) |

### G

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **Garbage collection (GC)** | Automatically freeing memory (or old data versions) that is no longer used; can cause program pauses. | [Ch 7](../07-transactions/README.md), [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **GFS (Google File System)** | Google's distributed file system, which inspired HDFS. | [Ch 10](../10-batch-processing/README.md) |
| **Giraph / GraphChi / GraphX** | Graph processing frameworks implementing the Pregel model or single-machine variants. | [Ch 10](../10-batch-processing/README.md) |
| **Git** | The most widely used version control system; it tracks history as branches and merges. | [Ch 5](../05-replication/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **GlusterFS / QFS** | Other distributed filesystems, alternatives to HDFS. | [Ch 10](../10-batch-processing/README.md) |
| **GoldenGate** | Oracle's tool for log-based change data capture and replication. | [Ch 5](../05-replication/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Google Cloud Pub/Sub** | Google's hosted publish/subscribe messaging service. | [Ch 11](../11-stream-processing/README.md) |
| **Gossip protocol** | Nodes randomly exchanging state with each other so information spreads through the whole cluster. | [Ch 6](../06-partitioning/README.md) |
| **GPS** | Global Positioning System: satellites whose signals give location and very accurate time. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Graph database** | A database built around vertices and edges, good for highly connected data (e.g., Neo4j). | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Gremlin** | A graph traversal query language (from Apache TinkerPop). | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **gRPC** | Google's open-source RPC framework using Protocol Buffers over HTTP/2. | [Ch 4](../04-encoding-and-evolution/README.md) |

### H

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **Hadoop** | The open-source ecosystem (HDFS, MapReduce, YARN...) for storing and processing huge datasets on clusters. | [Ch 10](../10-batch-processing/README.md) |
| **Happens-before** | Operation A happened before B if B knew about A, depended on it, or built on it. | [Ch 5](../05-replication/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Hard real-time system** | A system that must respond within a strict deadline or it has failed (e.g., airbag controllers). | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Hash function** | A function that turns a key into a number that looks random, used to spread keys evenly. | [Ch 6](../06-partitioning/README.md) |
| **Hash index** | An in-memory hash map from each key to the location of its value on disk. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Hash mod N** | Choosing a partition with hash(key) % N; a bad idea because changing N moves almost every key. | [Ch 6](../06-partitioning/README.md) |
| **Hash partitioning** | Assigning records to partitions based on a hash of their key, which spreads load evenly but loses key ordering. | [Ch 6](../06-partitioning/README.md) |
| **HATEOAS** | A strict REST principle where responses include links telling the client what it can do next. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **HBase** | An open-source Bigtable-style wide-column database on top of HDFS. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 6](../06-partitioning/README.md) |
| **HDFS** | Hadoop Distributed File System: stores huge files across many machines, replicating blocks for safety. | [Ch 10](../10-batch-processing/README.md) |
| **Head-of-line blocking** | A slow item at the front of a queue holding up everything behind it. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Heap file** | A file holding table rows in no particular order; indexes point into it. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Heartbeat** | A small periodic "I'm alive" message; missing several in a row suggests the node has failed. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Helix** | LinkedIn's cluster-management framework that uses ZooKeeper to track partition assignments. | [Ch 6](../06-partitioning/README.md) |
| **Heterogeneous distributed transaction** | A transaction spanning different technologies (e.g., two databases from different vendors plus a message broker). | [Ch 9](../09-consistency-and-consensus/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Heuristic decision** | A participant in 2PC unilaterally deciding to commit or abort an in-doubt transaction, breaking atomicity. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Hierarchical model / IMS** | IBM's 1960s database that stored records as a tree, each record with one parent. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **High-frequency trading** | Automated trading that relies on microsecond-precise timing. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **High-performance computing (HPC)** | Supercomputers running large scientific calculations; they usually stop the whole job on any failure. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Hinted handoff** | With a sloppy quorum, a node temporarily stores writes for an unreachable node and passes them on when it returns. | [Ch 5](../05-replication/README.md) |
| **Hive** | A Hadoop tool that runs SQL-like queries as batch jobs over files in HDFS. | [Ch 10](../10-batch-processing/README.md) |
| **Hive metastore / HCatalog** | Catalogs recording which files in HDFS form which tables and how they are partitioned. | [Ch 10](../10-batch-processing/README.md) |
| **Hot key / linchpin object** | A single key with disproportionate traffic (e.g., a celebrity's account), causing skew. | [Ch 6](../06-partitioning/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Hot spot** | A partition that receives far more load than others. | [Ch 6](../06-partitioning/README.md), [Ch 10](../10-batch-processing/README.md) |
| **HTML / CSS** | The markup language (HTML) and style language (CSS) of web pages. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 4](../04-encoding-and-evolution/README.md) |
| **HTTP** | The request/response protocol of the web. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Human fault tolerance** | Being able to recover from buggy code or mistakes, e.g., by rerunning a job over immutable input. | [Ch 10](../10-batch-processing/README.md) |
| **HyperLogLog** | A probabilistic algorithm that estimates how many distinct items a stream contains, using very little memory. | [Ch 11](../11-stream-processing/README.md) |
| **Hypervisor** | The software that runs virtual machines and shares the physical hardware between them. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |

### I

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **IaaS** | Infrastructure as a service: renting virtual machines, storage and networks from a cloud provider. | [Preface](../00-preface/README.md) |
| **ICMP Destination Unreachable** | A network message a router may send when it can't deliver a packet; not always sent or reliable. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **ID** | Identifier: a value (number or string) that uniquely names something. | [Ch 10](../10-batch-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Idempotence** | The property of being idempotent (see the table above): repeating an operation has the same effect as doing it once. | [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **IDL (interface definition language)** | A small language for describing a schema or service interface, from which code can be generated. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Immutable** | Never changed after it is written; new information is added instead. | [Ch 10](../10-batch-processing/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Impala / Presto** | Open-source MPP SQL query engines that read data stored in Hadoop. | [Ch 10](../10-batch-processing/README.md) |
| **Impedance mismatch** | The awkward translation needed between objects in application code and rows in relational tables. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Imperative language** | Code that spells out step by step how to compute something, in a specific order. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Increment-and-get** | An atomic operation that adds one to a counter and returns the new value. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Index-range lock (next-key lock)** | A simplified predicate lock that locks a whole range in an index, used to prevent phantoms. | [Ch 7](../07-transactions/README.md) |
| **In doubt (uncertain)** | The state of a 2PC participant that voted "yes" and must wait for the coordinator's decision. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **InfiniBand** | A very fast network technology used in supercomputers. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Initial snapshot** | A full copy of a database used to start CDC, before following the change stream. | [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **In-memory database** | A database that keeps all its data in RAM (with disk or replication for durability). | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 4](../04-encoding-and-evolution/README.md) |
| **InnoDB** | MySQL's default storage engine, which uses B-trees and supports transactions. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 7](../07-transactions/README.md) |
| **Integrity** | No lost, duplicated, contradictory or false data; violations are permanent until repaired. | [Ch 9](../09-consistency-and-consensus/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Internet of Things** | Everyday devices (sensors, appliances, toys) connected to the internet. | [Ch 6](../06-partitioning/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Invariant** | A rule about the data that must always be true (e.g., credits and debits balance). | [Ch 7](../07-transactions/README.md) |
| **Inversion of control** | Letting a framework call your code and wire inputs to outputs, rather than your code wiring everything itself. | [Ch 10](../10-batch-processing/README.md) |
| **Inverted index** | A map from each word to the list of documents containing it; the core of full-text search. | [Ch 12](../12-future-of-data-systems/README.md) |
| **IP** | Internet Protocol: delivers individual packets between machines, with no guarantee of delivery or order. | [Ch 6](../06-partitioning/README.md), [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Isolation level** | A setting choosing how strongly a database isolates concurrent transactions (read committed, snapshot isolation, serializable...). | [Ch 7](../07-transactions/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Iterator** | An object that hands out items one at a time, such as the values a reducer receives for one key. | [Ch 10](../10-batch-processing/README.md) |

### J

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **Java** | A widely used programming language that runs on the Java Virtual Machine (JVM). | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Java EE** | Java Enterprise Edition: a set of standards (JTA, JMS, EJB...) for business applications on Java. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **JavaScript** | The programming language that runs in web browsers (and on servers via Node.js). | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 4](../04-encoding-and-evolution/README.md) |
| **Jepsen** | Kyle Kingsbury's test harness that injects faults into distributed databases to check their claimed guarantees. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Jitter** | Random variation in network delay. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **JMS** | Java Message Service: the standard Java API for message brokers. | [Ch 11](../11-stream-processing/README.md) |
| **Job scheduler** | Software that decides which machines run which tasks (e.g., YARN). | [Ch 9](../09-consistency-and-consensus/README.md) |
| **JSON** | JavaScript Object Notation: a popular text format of nested objects, arrays, strings and numbers. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 4](../04-encoding-and-evolution/README.md) |
| **JTA / JDBC** | Java standards: JTA coordinates distributed (XA) transactions; JDBC is the standard API for connecting to SQL databases. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **JVM** | Java Virtual Machine: the runtime that executes Java (and Scala, Kotlin...) programs; it has garbage-collection pauses. | [Ch 8](../08-trouble-with-distributed-systems/README.md), [Ch 10](../10-batch-processing/README.md) |

### K

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **Kafka** | Apache Kafka: a durable, partitioned, replicated log used as a message broker and event store. | [Ch 11](../11-stream-processing/README.md) |
| **Kafka Connect** | A framework for streaming data between Kafka and other databases and systems. | [Ch 11](../11-stream-processing/README.md) |
| **Kafka Streams** | A stream processing library built into the Kafka project. | [Ch 11](../11-stream-processing/README.md) |
| **Key-range partitioning** | Giving each partition a continuous range of sorted keys, which makes range scans easy but risks hot spots. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 6](../06-partitioning/README.md) |
| **Key-value store** | A database that stores values looked up by a unique key, like a big persistent hash map. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Kinesis** | Amazon's hosted log-based streaming service, similar to Kafka. | [Ch 11](../11-stream-processing/README.md) |
| **k-nearest neighbors** | Finding the k items most similar to a given one, e.g., for recommendations. | [Ch 10](../10-batch-processing/README.md) |
| **Kryo / pickle** | Language-specific serialization libraries (Kryo for Java, pickle for Python). | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Kubernetes** | An open-source system for deploying and running containerised applications across a cluster. | [Ch 10](../10-batch-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |

### L

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **Lambda architecture** | Running a batch system and a stream system side by side over the same events, then merging their outputs. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Lamport timestamp** | A pair (counter, node ID) that gives a total order of events consistent with causality, without synchronised clocks. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Language-specific format** | An encoding built into one programming language (Java serialization, Python pickle); risky and not portable. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Last write wins (LWW)** | Resolving conflicts by keeping the write with the latest timestamp and silently discarding the others. | [Ch 5](../05-replication/README.md), [Ch 7](../07-transactions/README.md) |
| **Latch** | A lightweight lock protecting an in-memory data structure, such as a B-tree page during an update. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Latency** | Time spent waiting: e.g., the delay before a request is handled, or the network delay of a message. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Leader-based replication** | One replica (the leader) takes all writes and streams them to the followers. | [Ch 5](../05-replication/README.md) |
| **Leader election** | Choosing which node is the leader, normally via a consensus algorithm. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Leaderless replication** | Any replica accepts writes from clients directly; reads and writes use quorums (Dynamo-style). | [Ch 5](../05-replication/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Leaf page** | The bottom level of a B-tree, which holds the actual values or row references. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Leap second** | An extra second occasionally added to keep clocks in step with Earth's rotation; it has crashed systems. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Lease** | A lock that expires automatically after a time unless renewed. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Ledger** | An append-only record of financial transactions, a natural fit for event sourcing. | [Ch 7](../07-transactions/README.md), [Ch 11](../11-stream-processing/README.md) |
| **LevelDB** | Google's open-source LSM-tree key-value storage library. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Levenshtein automaton** | A data structure that efficiently finds words within a given edit distance of a search term. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Limping node** | A node that is alive but extremely slow, which can be worse than a dead one. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Linux / Unix** | Families of operating systems built around small programs, files and pipes; Linux is the most widely used. | [Ch 10](../10-batch-processing/README.md) |
| **Live migration** | Moving a running virtual machine to another host, briefly pausing it. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Liveness property** | "Something good eventually happens" (e.g., a request eventually gets a response). | [Ch 8](../08-trouble-with-distributed-systems/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **LLVM** | A compiler toolkit that some query engines use to turn queries into fast machine code. | [Ch 10](../10-batch-processing/README.md) |
| **LMDB / RocksDB** | Embedded key-value storage libraries: LMDB uses a copy-on-write B-tree, RocksDB an LSM-tree. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Load balancer** | A component that spreads incoming requests across several servers. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 6](../06-partitioning/README.md) |
| **Load balancing (messaging)** | Each message delivered to one of several consumers, to share the work. | [Ch 6](../06-partitioning/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Load parameters** | Numbers describing load, such as requests per second or read/write ratio. | [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **Location transparency** | Making a remote call look exactly like a local one; misleading because networks behave differently. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Lock-free data structure** | A concurrent data structure that uses atomic CPU instructions instead of locks. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Lock (shared / exclusive)** | A shared lock lets many readers in at once; an exclusive lock admits only one holder (typically a writer). | [Ch 7](../07-transactions/README.md) |
| **Log-based message broker** | A broker (like Kafka) that stores messages in an append-only log that consumers read by offset. | [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Log compaction** | Keeping only the latest record per key in a log, so it doesn't grow forever. | [Ch 11](../11-stream-processing/README.md) |
| **Logical clock** | A counter-based clock that orders events by causality rather than by physical time. | [Ch 8](../08-trouble-with-distributed-systems/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Logical (row-based) log** | A replication log describing row changes independently of the storage engine's internals. | [Ch 5](../05-replication/README.md) |
| **Logical timestamp** | A counter-based timestamp (e.g., Lamport) that orders events without relying on physical clocks. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Log segment** | One file of a log that has been split into several pieces. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Log sequence number (LSN)** | The position of an entry in a database's replication log. | [Ch 5](../05-replication/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Loose coupling** | Components that know little about each other, so each can change or fail without breaking the others. | [Ch 10](../10-batch-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Lost update** | Two read-modify-write cycles run concurrently and one overwrites the other's change. | [Ch 7](../07-transactions/README.md) |
| **LRU (least recently used)** | An eviction rule that drops the item that hasn't been used for the longest time. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **LSM-tree** | Log-Structured Merge-Tree: storage that buffers writes in memory, flushes sorted files, and merges them in the background. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Lucene** | The open-source full-text indexing library behind Elasticsearch and Solr. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 10](../10-batch-processing/README.md) |

### M

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **Machine learning (ML)** | Software that learns patterns from data to make predictions or decisions. | [Ch 10](../10-batch-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Mahout / MADlib** | Libraries of machine-learning algorithms that run on Hadoop (Mahout) or inside an MPP database (MADlib). | [Ch 10](../10-batch-processing/README.md) |
| **Mainframe** | A large, centralised business computer of the kind that dominated before networks of commodity servers. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Maintainability** | How easily people can operate, understand and change a system over its lifetime. | [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **Many-to-one / many-to-many relationship** | Many records referring to one shared record (many people living in one city), or many records relating to many others (people and the organisations they worked for). | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Mapper** | The function in MapReduce that runs once per input record and emits key–value pairs. | [Ch 10](../10-batch-processing/README.md) |
| **MapReduce** | A batch programming model: a map function emits key–value pairs, they are sorted and grouped by key, and a reduce function processes each group. | [Ch 10](../10-batch-processing/README.md) |
| **Map-side join** | A join done in the mappers, possible when inputs are small or already partitioned and sorted compatibly. | [Ch 10](../10-batch-processing/README.md) |
| **Map-side merge join** | A map-side join when both inputs are partitioned and sorted the same way, so they can be merged directly. | [Ch 10](../10-batch-processing/README.md) |
| **Materialized view** | A query result stored on disk and kept updated, rather than recomputed every time. | [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Materializing conflicts** | Creating rows that exist only so there is something to lock, turning a phantom into a lock conflict. | [Ch 7](../07-transactions/README.md) |
| **Maven / npm / RubyGems** | Package managers that download libraries (dependencies) for Java, JavaScript and Ruby projects. | [Ch 10](../10-batch-processing/README.md) |
| **Median** | The middle value: half of the measurements are below it, half above (the 50th percentile). | [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **Membership service** | A service (often ZooKeeper-based) that tracks which nodes are currently alive and part of the cluster. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Memcached** | A simple, widely used in-memory key-value cache. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 3](../03-storage-and-retrieval/README.md) |
| **Memory barrier / fence** | A CPU instruction that forces memory reads and writes to be ordered and visible across cores. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **MemSQL** | An in-memory relational database (now called SingleStore). | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 7](../07-transactions/README.md) |
| **Memtable** | The in-memory sorted structure where an LSM-tree collects new writes before flushing them to disk. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Mergeable persistent data structure** | A data structure that tracks history like Git and merges concurrent changes with a three-way merge. | [Ch 5](../05-replication/README.md) |
| **Merge (segments)** | Combining several sorted files into one, keeping the newest value per key. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 5](../05-replication/README.md) |
| **Merkle tree** | A tree of hashes that lets you cheaply prove a record is part of a dataset. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Message broker (message queue)** | A server that receives messages from producers and delivers them to consumers, buffering them in between. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 11](../11-stream-processing/README.md) |
| **MessagePack** | A binary encoding of JSON-like data. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Messaging system** | Software that delivers messages (events) from producers to consumers, directly or via a broker. | [Ch 11](../11-stream-processing/README.md) |
| **Microbatching** | Processing a stream as a series of tiny batch jobs, each covering a second or so. | [Ch 11](../11-stream-processing/README.md) |
| **Microservices** | An architecture where an application is split into many small services, each deployable independently. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Middleware** | Software sitting between applications and the operating system or network, e.g., message brokers. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Migrating data** | Rewriting existing data into a new schema, which is expensive on large datasets. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Minimizing irreversibility** | Designing so that mistakes can be undone, e.g., by keeping old outputs and immutable inputs. | [Ch 10](../10-batch-processing/README.md) |
| **MongoDB** | A popular open-source document database storing JSON-like documents. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 6](../06-partitioning/README.md) |
| **MongoDB oplog** | MongoDB's replication log, which CDC tools can read. | [Ch 11](../11-stream-processing/README.md) |
| **Monotonically increasing** | Always going up (never decreasing or repeating), like a sequence of fencing tokens. | [Ch 8](../08-trouble-with-distributed-systems/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Monotonic clock** | A clock that only moves forward; good for measuring durations, meaningless across machines. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Monotonic reads** | A guarantee that a user never sees data go backward in time across successive reads. | [Ch 5](../05-replication/README.md) |
| **MPP (massively parallel processing)** | Databases that split each analytic SQL query across many machines. | [Ch 10](../10-batch-processing/README.md) |
| **MTTF (mean time to failure)** | The average time a component runs before it fails; with thousands of disks, some fail every day. | [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **Multi-dimensional index** | An index that can search on several columns at once, like latitude and longitude (e.g., R-trees). | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Multi-leader replication** | More than one node accepts writes, each passing changes to the others; conflicts are possible. | [Ch 5](../05-replication/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Multi-object transaction** | A transaction that reads or writes several records together. | [Ch 7](../07-transactions/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Multi-Paxos** | The version of Paxos that agrees on a sequence of values (a log) rather than a single value. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Multi-tenant** | Many customers sharing the same hardware or service. | [Ch 8](../08-trouble-with-distributed-systems/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Mutex / semaphore** | Synchronisation tools within one machine: a mutex lets one thread at a time into a section; a semaphore lets up to N. | [Ch 7](../07-transactions/README.md), [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **MVCC (multi-version concurrency control)** | Keeping several versions of each row so each transaction can read a consistent snapshot without blocking writers. | [Ch 7](../07-transactions/README.md) |
| **MySQL** | A popular open-source relational database. | [Ch 5](../05-replication/README.md), [Ch 7](../07-transactions/README.md) |

### N

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **NameNode / DataNode** | In HDFS, the NameNode tracks which machine holds each file block; DataNodes store the blocks. | [Ch 10](../10-batch-processing/README.md) |
| **Neo4j** | A popular graph database that uses the Cypher query language. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Network congestion** | Too much traffic for a network link, causing queues and packet loss. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Network partition / netsplit** | Some nodes can't talk to others because of a network fault, though both groups are still running. | [Ch 8](../08-trouble-with-distributed-systems/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Noisy neighbor** | Another customer's workload on shared hardware slowing your system down. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Nondeterministic** | Can give different results each time (e.g., reading the clock or a random number). | [Ch 5](../05-replication/README.md), [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Nondeterministic function** | A function such as NOW() or RAND() that gives a different result each time, which breaks statement-based replication. | [Ch 5](../05-replication/README.md) |
| **NoSQL** | Databases that don't use the classic relational/SQL model (document, key-value, wide-column, graph stores). | [Preface](../00-preface/README.md), [Ch 2](../02-data-models-and-query-languages/README.md) |
| **NTP** | Network Time Protocol: adjusts a computer's clock using time servers over the network. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **NUMA** | Non-uniform memory access: on big servers, memory near a CPU is faster to reach than memory near another CPU. | [Ch 5](../05-replication/README.md) |
| **NVM (non-volatile memory)** | Memory that keeps its contents without power while being nearly as fast as RAM. | [Ch 3](../03-storage-and-retrieval/README.md) |

### O

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **OAuth** | A standard that lets a user grant an application limited access to their account on another service without sharing their password. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Object container file** | An Avro file holding many records, with the writer's schema stored once at the start. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Object database / XML database** | 1980s–2000s alternatives to relational databases that stored objects or XML documents; they faded away. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Object storage** | Cloud storage of whole files ("objects") by name, such as Amazon S3. | [Ch 10](../10-batch-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Observer pattern** | Code subscribes to a value and is notified when it changes, instead of repeatedly checking. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Offline-first application** | An app that works mainly against a local on-device database and syncs with servers in the background. | [Ch 5](../05-replication/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Offset** | A position number: the byte position in a file, or a message's sequence number within a log partition. | [Ch 11](../11-stream-processing/README.md) |
| **One-to-many relationship** | One record owning a list of others, like a résumé with many job entries; a natural fit for a tree or document. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Online system (service)** | A system that waits for requests and must reply quickly; response time is its key metric. | [Ch 10](../10-batch-processing/README.md) |
| **OpenAPI (Swagger)** | A standard format for describing RESTful APIs. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **OpenStack** | Open-source software for running a private cloud. | [Ch 9](../09-consistency-and-consensus/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Operability** | How easy it is for operations teams to keep a system running smoothly. | [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **Operational transformation** | The conflict-resolution technique behind collaborative editors like Google Docs. | [Ch 5](../05-replication/README.md) |
| **Operation identifier / request ID** | A unique ID generated by the client for one logical action, used to detect and drop duplicates everywhere downstream. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Operator** | One processing step in a dataflow or stream job (map, filter, join...). | [Ch 10](../10-batch-processing/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Optimistic concurrency control** | Let transactions proceed without blocking and check for conflicts at commit time, aborting if necessary. | [Ch 7](../07-transactions/README.md) |
| **Oracle** | Oracle Database: a widely used commercial relational database. | [Ch 5](../05-replication/README.md), [Ch 7](../07-transactions/README.md) |
| **Orleans / Finagle / Rest.li** | RPC and actor frameworks from Microsoft (Orleans), Twitter (Finagle) and LinkedIn (Rest.li). | [Ch 4](../04-encoding-and-evolution/README.md) |
| **ORM (object-relational mapping)** | A library that maps program objects to database rows to reduce boilerplate. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 7](../07-transactions/README.md) |
| **Orphaned transaction** | An in-doubt transaction whose coordinator lost its decision, so it holds locks until someone intervenes manually. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **OS (operating system)** | The software that manages a computer's hardware and runs programs (Linux, Windows...). | [Ch 8](../08-trouble-with-distributed-systems/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Overcommitted resources** | Promising more CPU or memory to tasks than physically exists, betting they won't all use it at once. | [Ch 10](../10-batch-processing/README.md) |

### P

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **p50 / p95 / p99 / p999** | p50, p95, p99, p999 are percentiles: e.g., p99 is the response time 99% of requests beat. | [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **Packet** | A small chunk of data sent across a network on its own. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Packet switching** | Networks that send data as independent packets sharing links dynamically; efficient but delays are unbounded. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Page** | A fixed-size block (often 4 KB) that disks and B-trees read and write as a unit. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Page cache** | Memory the operating system uses to keep recently read disk pages, making repeated reads fast. | [Ch 10](../10-batch-processing/README.md) |
| **Page fault / swapping** | The OS moving memory to and from disk because RAM is full, which can pause a program unexpectedly. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **PageRank** | Google's algorithm ranking web pages by how many important pages link to them; a classic graph computation. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Parquet** | A popular open-source columnar file format for analytics. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Partial failure** | Some parts of a system broken while others work, often with no way to tell which. | [Ch 7](../07-transactions/README.md), [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Partial order** | An ordering where some pairs of items can't be compared (e.g., concurrent events). | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Participant** | A database node taking part in a two-phase commit. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Partition-aware client** | A client that knows which node holds which partition and connects directly to the right one. | [Ch 6](../06-partitioning/README.md) |
| **Partition boundaries** | The key values where one partition's range ends and the next begins. | [Ch 6](../06-partitioning/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Partitioned hash join** | A map-side join when both inputs are partitioned the same way, so each mapper joins one matching pair of partitions. | [Ch 10](../10-batch-processing/README.md) |
| **Partitioned log** | A log split into several partitions on different machines to scale throughput; order is kept only within each partition. | [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Partitioning proportionally to nodes** | Giving each node a fixed number of partitions, so the partition count grows with the cluster. | [Ch 6](../06-partitioning/README.md) |
| **Partition (shard, region, tablet, vnode, vBucket)** | One piece of a dataset that has been split across machines; the names differ by product. | [Ch 6](../06-partitioning/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Paxos** | The classic fault-tolerant consensus algorithm. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Peer-to-peer network** | A network where participants talk directly to each other with no central server. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Percolator** | Elasticsearch's feature for storing queries and matching each new document against them (stream search). | [Ch 11](../11-stream-processing/README.md) |
| **Perfect failure detector** | An imaginary detector that always correctly knows whether a node has crashed; it can't exist on real networks. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Pessimistic concurrency control** | Blocking (locking) whenever a conflict might happen, as two-phase locking does. | [Ch 7](../07-transactions/README.md) |
| **Phantom** | A write in one transaction changing the result of a search query in another (e.g., inserting a row that matches its WHERE clause). | [Ch 7](../07-transactions/README.md) |
| **Phi Accrual failure detector** | A failure detector that adapts its timeout to observed network delays and reports a suspicion level. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Physical clock** | A clock that measures real elapsed time (seconds), as opposed to a logical counter. | [Ch 8](../08-trouble-with-distributed-systems/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Pig** | A Hadoop tool with a scripting language for building MapReduce workflows. | [Ch 10](../10-batch-processing/README.md) |
| **Pipelined execution** | Passing output from one operator to the next as soon as it's ready, rather than waiting for the whole stage. | [Ch 10](../10-batch-processing/README.md) |
| **Point in time** | A specific date and time, as opposed to a duration. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Polling** | Repeatedly asking "anything new?" instead of being notified. | [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Polyglot persistence** | Using several different kinds of database in one application, each for what it's good at. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **PostGIS** | A PostgreSQL extension for geographic data, using R-tree-like indexes. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **PostgreSQL** | A popular, feature-rich open-source relational database. | [Ch 5](../05-replication/README.md), [Ch 7](../07-transactions/README.md) |
| **Postings list** | For one word, the list of IDs of documents that contain it. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 3](../03-storage-and-retrieval/README.md) |
| **ppm (parts per million)** | A unit for clock drift: 200 ppm means up to 17 seconds off per day. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Predicate lock** | A lock on all records matching a condition, including ones that don't exist yet. | [Ch 7](../07-transactions/README.md) |
| **Predictive analytics** | Using data to forecast things, including about individual people (credit risk, reoffending). | [Ch 12](../12-future-of-data-systems/README.md) |
| **Preemption** | Stopping a running task (or thread) to give its resources to a higher-priority one. | [Ch 8](../08-trouble-with-distributed-systems/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Premise** | A fact a transaction read and relied on when deciding what to write; if it changes before commit, SSI aborts. | [Ch 7](../07-transactions/README.md) |
| **Pre-splitting** | Creating initial partitions in advance to avoid all writes hitting one partition at first. | [Ch 6](../06-partitioning/README.md) |
| **Priority queue** | A queue that always hands out the most important item next instead of the oldest. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 7](../07-transactions/README.md) |
| **Probabilistic algorithm** | An algorithm that gives approximate answers using much less memory (e.g., Bloom filters, HyperLogLog). | [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Procedural SQL (PL/SQL, T-SQL, PL/pgSQL)** | Each database vendor's programming language for writing stored procedures. | [Ch 7](../07-transactions/README.md) |
| **Processing lag** | How far a stream processor's work is behind the newest events. | [Ch 11](../11-stream-processing/README.md) |
| **Processing time** | When an event is processed by the stream processor, which can be much later than when it happened. | [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Producer** | A program that writes messages or events into a stream or queue. | [Ch 11](../11-stream-processing/README.md) |
| **Property graph** | A graph model where vertices and edges each have a label and a set of key–value properties. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Protocol Buffers (Protobuf)** | Google's binary encoding format, using a schema and numeric field tags. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **PTP (Precision Time Protocol)** | A protocol for very accurate clock synchronisation over a local network. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Publish/subscribe** | Producers publish messages to a topic, and everyone subscribed to it receives them. | [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Pure function** | A function whose output depends only on its input and that has no side effects. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Push notification** | A message the server sends to a device without the device asking first. | [Ch 11](../11-stream-processing/README.md) |

### Q

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **Quality of service (QoS)** | Network features that reserve capacity or prioritise some traffic. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Quartz crystal oscillator** | The vibrating crystal that keeps time in a computer's hardware clock; it drifts with temperature. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Query optimizer** | The part of a database that chooses how to execute a query (index use, join order...). | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Queueing delay** | Time a request or packet spends waiting in a queue before being processed. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Queue / topic** | A named channel in a message broker that producers write to and consumers read from. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Quorum read** | Reading from a majority of replicas so the result reflects the latest committed write. | [Ch 5](../05-replication/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |

### R

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **Race condition** | A bug where the result depends on the exact timing of concurrent operations. | [Ch 7](../07-transactions/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Raft** | A widely used consensus algorithm designed to be easier to understand than Paxos (used by etcd). | [Ch 9](../09-consistency-and-consensus/README.md) |
| **RAID** | Combining several disks so data survives the failure of one of them. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 10](../10-batch-processing/README.md) |
| **RAM** | A computer's main memory: fast, but its contents are lost on power loss. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Range scan / range query** | Fetching all records whose keys lie between two values. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 6](../06-partitioning/README.md) |
| **RDBMS** | Relational database management system, such as PostgreSQL, MySQL or Oracle. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **RDD** | Resilient Distributed Dataset: Spark's abstraction that tracks how each piece of data was computed so it can be recomputed after failure. | [Ch 10](../10-batch-processing/README.md) |
| **RDF** | Resource Description Framework: a graph model storing data as (subject, predicate, object) triples. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **RDMA** | Remote direct memory access: reading another machine's memory over the network with very low latency. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Read-after-write (read-your-writes) consistency** | A guarantee that users always see their own submitted updates. | [Ch 5](../05-replication/README.md) |
| **Read committed** | An isolation level preventing dirty reads and dirty writes. | [Ch 7](../07-transactions/README.md) |
| **Reader's schema / writer's schema** | In Avro, the schema data was written with versus the one the reading code expects; they are reconciled when decoding. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Read-modify-write cycle** | Reading a value, changing it in application code and writing it back; prone to lost updates. | [Ch 7](../07-transactions/README.md) |
| **Read path** | Work done lazily, only when someone asks a question. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Read repair** | A client reading from several replicas notices a stale one and writes the newer value back to it. | [Ch 5](../05-replication/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Read-scaling architecture** | Adding many followers to serve more read traffic. | [Ch 5](../05-replication/README.md) |
| **Read skew / nonrepeatable read** | Seeing different parts of the database at different points in time within one transaction. | [Ch 7](../07-transactions/README.md) |
| **Read uncommitted** | A weak isolation level that prevents dirty writes but allows dirty reads. | [Ch 7](../07-transactions/README.md) |
| **Rebalancing** | Moving partitions between nodes so load stays even as the cluster changes. | [Ch 6](../06-partitioning/README.md) |
| **Recency guarantee** | A promise that a read returns the most recent value written; the essence of linearizability. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Recursive common table expression** | SQL's WITH RECURSIVE syntax, which can express graph traversals. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Red-black tree / AVL tree** | Self-balancing in-memory tree structures that keep keys sorted with O(log n) inserts and lookups; used for memtables. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Redelivery** | A broker sending a message again because the consumer didn't acknowledge it. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Redis** | A popular in-memory key-value store with data structures such as lists and sets. | [Ch 5](../05-replication/README.md), [Ch 7](../07-transactions/README.md) |
| **Redshift / Vertica / Teradata** | Commercial column-oriented MPP data warehouse products. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Reducer** | The function in MapReduce that receives all values for one key and produces output. | [Ch 10](../10-batch-processing/README.md) |
| **Reduce-side join** | A join done in the reducers, after the shuffle brings records with the same key together. | [Ch 10](../10-batch-processing/README.md) |
| **Refactoring** | Restructuring code to make it cleaner without changing what it does. | [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **Register** | In consistency theory, a single variable that can be read and written. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Regular expression** | A compact pattern language for matching text. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 4](../04-encoding-and-evolution/README.md) |
| **Regular register** | A register where a read concurrent with a write may return either the old or the new value (weaker than linearizable). | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Relational algebra** | The mathematical foundation of SQL: operations like select, project and join on tables. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Relational model** | Data organised as tables (relations) of rows, queried with SQL. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Reliability** | A system keeps working correctly (right function, acceptable performance) even when hardware, software or people make mistakes. | [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **Remote code execution** | A security hole that lets an attacker run their own code on your machine (e.g., via unsafe deserialization). | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Repeatable read** | An isolation level name with inconsistent meanings across databases; often it really means snapshot isolation. | [Ch 7](../07-transactions/README.md) |
| **Replay** | Re-reading old messages from a log to reprocess them. | [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Replica** | A node holding a copy of the data. | [Ch 5](../05-replication/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Replication lag** | The delay between a write on the leader and it showing up on a follower. | [Ch 5](../05-replication/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Replication log** | The stream of changes a leader sends to its followers. | [Ch 5](../05-replication/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Replication topology** | The pattern of which leaders send writes to which (circular, star, all-to-all). | [Ch 5](../05-replication/README.md), [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Reprocessing** | Re-running a (new) transformation over all old data to build a new derived view. | [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Request routing** | Getting each request to the node that holds the relevant partition. | [Preface](../00-preface/README.md), [Ch 6](../06-partitioning/README.md) |
| **Response time** | What the client sees: total time from sending a request to receiving the answer. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **REST / RESTful** | A style of web API built on HTTP, URLs and standard methods like GET and POST. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **RethinkDB** | An open-source document database that supports subscribing to query changes. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 6](../06-partitioning/README.md) |
| **Retraction / correction (streams)** | Publishing an updated result for a window after late events arrive, replacing the earlier output. | [Ch 9](../09-consistency-and-consensus/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Riak** | An open-source Dynamo-style leaderless key-value store. | [Ch 5](../05-replication/README.md), [Ch 6](../06-partitioning/README.md) |
| **Ring buffer (circular buffer)** | A fixed-size buffer where new data overwrites the oldest once it is full. | [Ch 11](../11-stream-processing/README.md) |
| **Rolling average** | An average over a moving window, such as the last five minutes. | [Ch 11](../11-stream-processing/README.md) |
| **Rolling upgrade** | Updating servers one or a few at a time so the service stays up throughout. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 4](../04-encoding-and-evolution/README.md) |
| **Round-robin load balancer** | A load balancer that sends each new request to the next server in turn. | [Ch 6](../06-partitioning/README.md) |
| **Routing tier** | A layer of servers that forward each request to the node owning the relevant partition. | [Ch 6](../06-partitioning/README.md) |
| **Rowhammer** | A hardware effect where repeatedly accessing memory rows flips bits in neighbouring rows. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Row-level lock** | A lock on a single row rather than on a whole table. | [Ch 7](../07-transactions/README.md) |
| **Row-oriented storage** | Storing all values of each row together; good for transactional workloads. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **RPC (remote procedure call)** | Calling a function on another machine over the network as if it were local. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **RST / FIN packet** | TCP packets that end a connection; their arrival can signal that a remote process has died. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **RTOS** | Real-time operating system: one that guarantees CPU time to processes within deadlines. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **R-tree** | An index for multi-dimensional data such as geographic locations. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Rule (Datalog)** | In Datalog, a named definition that derives new facts from existing facts (and from other rules, even recursively). | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Run-length encoding** | Compressing repeated values by storing "value × count" instead. | [Ch 3](../03-storage-and-retrieval/README.md) |

### S

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **Safety guarantees** | Promises a system makes about bad things never happening (e.g., no lost writes). | [Ch 7](../07-transactions/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Safety property** | "Nothing bad happens" (e.g., no two nodes hold the same lock); once violated it can't be undone. | [Ch 8](../08-trouble-with-distributed-systems/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Samza** | Apache Samza: a stream processing framework closely tied to Kafka. | [Ch 11](../11-stream-processing/README.md) |
| **SAP HANA** | SAP's in-memory, column-oriented database. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Scalability** | A system's ability to cope with increased load, usually by adding resources. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Scaling up / scaling out** | Scaling up (vertical) means a bigger machine; scaling out (horizontal) means more machines. | [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **Scatter/gather** | Sending a query to all partitions and combining the results. | [Ch 6](../06-partitioning/README.md) |
| **Schema evolution** | Changing a schema over time while keeping old and new data readable. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Schemaless** | Databases that don't enforce a schema on write; really schema-on-read. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 4](../04-encoding-and-evolution/README.md) |
| **Schema migration** | Changing a database's schema (and possibly rewriting existing data). | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Schema-on-read / schema-on-write** | Schema-on-read: structure is interpreted only when reading ("schemaless"). Schema-on-write: data is checked against a schema when stored. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 4](../04-encoding-and-evolution/README.md) |
| **Secondary sort** | Sorting the values within each reducer group in a particular order. | [Ch 10](../10-batch-processing/README.md) |
| **Self-describing file** | A file that includes its own schema, so any reader can decode it. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Semantic web** | The idea of websites publishing machine-readable data (as RDF) alongside human-readable pages. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Semi-synchronous replication** | One follower is synchronous and the rest asynchronous, so at least two nodes have every committed write. | [Ch 5](../05-replication/README.md) |
| **Sequence number** | A counter attached to each operation to order them. | [Ch 5](../05-replication/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Sequential consistency** | All nodes see operations in the same order, consistent with each client's program order, but not necessarily in real time. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Serializable snapshot isolation (SSI)** | An optimistic algorithm giving full serializability on top of snapshot isolation by detecting dangerous conflicts at commit. | [Ch 7](../07-transactions/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Server-sent events / WebSockets** | Protocols letting a server push messages to a browser over a long-lived connection. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Service** | A program that offers an API over the network for other programs to use. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Service discovery** | Finding which IP address and port a service is currently running on. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Service time** | The time actually spent processing a request, excluding waiting in queues. | [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **Sessionization** | Grouping a user's events into separate visits (sessions). | [Ch 10](../10-batch-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Session timeout** | How long ZooKeeper waits without heartbeats before declaring a client's session dead. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Sharding** | Another name for partitioning (see the table above): splitting data across machines. | [Preface](../00-preface/README.md), [Ch 6](../06-partitioning/README.md) |
| **Shared-disk / shared-memory** | Scaling architectures where machines share one set of disks or one memory, as opposed to shared-nothing. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 5](../05-replication/README.md) |
| **Shredding** | Splitting a document-like structure into several relational tables. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Shuffle** | The MapReduce step that sorts mapper output and sends all values for each key to the same reducer. | [Ch 10](../10-batch-processing/README.md) |
| **Sibling pointers** | Links from each B-tree leaf page to its neighbours, so range scans can move along without going back to the root. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Siblings** | Several concurrent values kept for one key until the application merges them. | [Ch 5](../05-replication/README.md) |
| **SIGSTOP / SIGCONT** | Unix signals that pause and resume a process; a paused process doesn't know time has passed. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **SIMD** | Single instruction, multiple data: CPU instructions that process several values at once. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Simplicity** | Making a system easy to understand by removing accidental complexity. | [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **Single-object operation** | An atomic change to one record (e.g., increment or compare-and-set). | [Ch 7](../07-transactions/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Size-tiered / leveled compaction** | Two strategies for scheduling LSM-tree merges: merge similar-sized files, or keep data in levels of fixed size. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Slewing / smearing** | Adjusting a clock gradually (slightly faster or slower) instead of jumping, e.g., to spread a leap second over a day. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Sloppy quorum** | Accepting writes on nodes outside a key's usual set when the usual ones are unreachable. | [Ch 5](../05-replication/README.md) |
| **SLO / SLA** | Service level objective / agreement: targets or contracts for performance and availability (e.g., p99 under 1 s). | [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **Slowly changing dimension (SCD)** | Keeping old versions of dimension data (e.g., a tax rate) with version IDs so joins stay reproducible. | [Ch 11](../11-stream-processing/README.md) |
| **Snapshot** | A consistent copy of the data as it was at one point in time. | [Ch 7](../07-transactions/README.md) |
| **Snapshot isolation** | Each transaction reads from a consistent snapshot of the database as of when it started. | [Ch 7](../07-transactions/README.md) |
| **Snowflake schema** | A star schema where dimension tables are further split into sub-dimension tables. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **SOA** | Service-oriented architecture: building an application as a set of services; microservices are its modern form. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **SOAP** | An XML-based protocol for web service requests, described with WSDL. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Solr** | An open-source search server built on Lucene. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Sort key** | The column(s) a column store sorts its rows by, which speeds up range queries and compression. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Sort-merge join** | Joining two datasets by sorting both by key and walking through them together. | [Ch 10](../10-batch-processing/README.md) |
| **Space-filling curve** | A way to map two-dimensional locations to one-dimensional keys so a normal index can be used. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Spanner** | Google's globally distributed SQL database, which uses TrueTime clocks to order transactions. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Spark** | Apache Spark: a popular distributed engine for batch (and microbatch streaming) processing. | [Ch 10](../10-batch-processing/README.md) |
| **SPARQL** | A query language for RDF triple-stores. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Speculative execution** | Starting a duplicate copy of a slow task on another machine and using whichever finishes first. | [Ch 10](../10-batch-processing/README.md) |
| **SQL** | Structured Query Language: the standard declarative language for relational databases. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 10](../10-batch-processing/README.md) |
| **SQL injection / cross-site scripting** | Common web attacks where untrusted input is run as database commands or as script in other users' browsers. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **SQL Server** | Microsoft's commercial relational database. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 7](../07-transactions/README.md) |
| **SSD** | Solid-state drive: flash-memory storage, much faster than spinning hard disks. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 7](../07-transactions/README.md) |
| **SSTable** | Sorted String Table: an immutable file of key–value pairs sorted by key. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Stable storage** | Storage that survives crashes (e.g., disk), assumed by the crash-recovery model. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Star schema** | A data warehouse layout with one big fact table referencing smaller dimension tables. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Stateless / stateful service** | A stateless server forgets everything between requests (state lives in a database); a stateful one keeps data itself. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **State machine replication** | Every replica applies the same deterministic commands in the same order, so they all end in the same state. | [Ch 9](../09-consistency-and-consensus/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Statement-based replication** | Replicating by sending each SQL statement to followers; breaks with nondeterministic functions like NOW(). | [Ch 5](../05-replication/README.md) |
| **Static vs dynamic type checking** | Checking types before the program runs (static) versus while it runs (dynamic); compared to schema-on-write versus schema-on-read. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 4](../04-encoding-and-evolution/README.md) |
| **StatsD / Brubeck** | Metrics daemons that receive counters over UDP and aggregate them. | [Ch 11](../11-stream-processing/README.md) |
| **stdin / stdout** | A program's standard input and output streams, which Unix pipes connect together. | [Ch 10](../10-batch-processing/README.md) |
| **Steal time** | CPU time a virtual machine loses to other VMs on the same physical host. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Stop-the-world GC pause** | A pause where garbage collection halts all program threads. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Storage engine** | The part of a database that decides how data is laid out on disk and in memory. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 5](../05-replication/README.md) |
| **Store buffer** | A per-CPU-core queue of pending memory writes, one reason multi-core memory isn't linearizable. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Storm** | Apache Storm: an early open-source stream processing framework. | [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Straggler** | A task that runs much slower than the rest, or an event that arrives late. | [Ch 10](../10-batch-processing/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Stream** | Data that arrives gradually over time and never really ends. | [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Stream analytics** | Computing aggregates and statistics over a stream, such as rates or averages per time window. | [Ch 11](../11-stream-processing/README.md) |
| **Stream-stream join (window join)** | Matching events from two streams that occur close together in time. | [Ch 11](../11-stream-processing/README.md) |
| **Stream-table join (enrichment)** | Adding information to each event by looking it up in a table, often a local copy kept up to date via CDC. | [Ch 11](../11-stream-processing/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Strict quorum** | A quorum where reads and writes overlap (w + r > n), so a read normally sees the latest write. | [Ch 5](../05-replication/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Strict serializability** | Serializability plus linearizability: transactions appear in an order consistent with real time. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Supermajority** | More than two-thirds of the nodes; needed by Byzantine fault-tolerant protocols. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Surveillance** | Tracking people mainly to serve someone else's interests (e.g., advertisers). | [Ch 12](../12-future-of-data-systems/README.md) |
| **Sushi principle** | "Raw data is better": store data in its original form and interpret it later. | [Ch 10](../10-batch-processing/README.md) |
| **Synchronous network** | A network with reserved bandwidth and bounded delays, like old telephone circuits. | [Ch 8](../08-trouble-with-distributed-systems/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Systematic error** | A fault that hits many nodes at once (e.g., a software bug triggered by the same input). | [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **System model** | The set of assumptions about what can go wrong (crashes, delays, clock errors) that an algorithm is designed for. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **System R** | IBM's 1970s research database that pioneered SQL and many transaction ideas. | [Ch 7](../07-transactions/README.md) |
| **Systems thinking** | Considering the whole system, including the humans in it, to foresee consequences. | [Ch 12](../12-future-of-data-systems/README.md) |

### T

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **Tableau** | A popular commercial tool for visual data analysis and dashboards. | [Ch 10](../10-batch-processing/README.md) |
| **Table-table join** | Keeping the join of two changing tables up to date by consuming both change streams. | [Ch 11](../11-stream-processing/README.md) |
| **tail -f** | A Unix command that prints new lines as they are appended to a file — like a consumer following a log. | [Ch 11](../11-stream-processing/README.md) |
| **Tail latency** | The response times of the slowest requests (high percentiles like p99). | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 3](../03-storage-and-retrieval/README.md) |
| **Tail latency amplification** | When one user request needs many backend calls, it's slow if any of them is slow. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 6](../06-partitioning/README.md) |
| **Tandem NonStop SQL** | A 1980s fault-tolerant commercial database, an early example of partitioning with transactions. | [Ch 6](../06-partitioning/README.md), [Ch 10](../10-batch-processing/README.md) |
| **TCP** | Transmission Control Protocol: builds a reliable, ordered byte stream on top of IP, with retransmission and flow control. | [Ch 8](../08-trouble-with-distributed-systems/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **t-digest / forward decay / HdrHistogram** | Algorithms and libraries for computing approximate percentiles cheaply over a stream of measurements. | [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **Telemetry** | Detailed monitoring data (metrics, error rates) sent from running systems. | [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **Term dictionary** | In a full-text index, the sorted list of all words, each pointing to its postings list. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Term-partitioned (global) index** | A secondary index split up by the indexed value, so a read hits one partition but a write may touch several. | [Ch 6](../06-partitioning/README.md) |
| **Test-driven development (TDD)** | Writing automated tests before the code they test. | [Ch 1](../01-reliable-scalable-maintainable/README.md) |
| **Textual format** | Human-readable encodings such as JSON, XML and CSV. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Tez** | A dataflow engine for Hadoop that runs workflows without writing every step to HDFS. | [Ch 10](../10-batch-processing/README.md) |
| **Thinking like a vertex** | The Pregel style of writing graph algorithms as code that runs per vertex and talks to neighbours by messages. | [Ch 10](../10-batch-processing/README.md) |
| **Thrashing** | A computer spending most of its time swapping memory to disk, doing little real work. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Three-way merge** | Merging two changed versions by comparing each against their common ancestor, as Git does. | [Ch 5](../05-replication/README.md) |
| **Thrift** | A binary encoding and RPC framework from Facebook, using schemas and numeric field tags. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Throughput** | How much work a system handles per unit of time (e.g., records per second). | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Timeliness** | Users see an up-to-date state; violations are temporary. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Time-of-day clock** | A clock giving the current date and time; it can jump backwards when corrected. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **TLS/SSL** | The encryption protocol that secures HTTPS and many other network connections. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Tombstone** | A special marker recording that a key was deleted, so the deletion survives merging and replication. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 5](../05-replication/README.md) |
| **Top-of-rack switch / router** | Network devices: a top-of-rack switch connects the servers in one rack; a router forwards packets between networks. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Total order broadcast (atomic broadcast)** | Delivering every message reliably to all nodes in the same order; equivalent to consensus. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Transaction ID (txid)** | A number the database gives each transaction, used to tag the row versions it creates. | [Ch 7](../07-transactions/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Transaction processing** | Handling many small reads and writes as business events happen (sales, orders, payments). | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 3](../03-storage-and-retrieval/README.md) |
| **Transitive closure** | Finding everything reachable from a starting point through any number of steps. | [Ch 10](../10-batch-processing/README.md) |
| **Trie** | A tree that stores strings letter by letter, so words with the same prefix share a path. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Trigger** | Code a database runs automatically when data changes. | [Ch 5](../05-replication/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Trigger-based replication** | Using database triggers to copy changes into a separate table that an external process replicates; flexible but slower. | [Ch 5](../05-replication/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Triple-store** | A graph database storing (subject, predicate, object) triples, such as RDF data. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **TrueTime** | Google's clock API in Spanner that returns a time range guaranteed to contain the true time. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Tumbling / hopping / sliding / session window** | Ways to group stream events by time: fixed non-overlapping blocks; fixed overlapping blocks; a moving interval; or bursts of activity separated by gaps. | [Ch 11](../11-stream-processing/README.md) |
| **Tungsten Replicator / BDR / Bucardo** | Add-on tools providing multi-leader replication for MySQL (Tungsten) and PostgreSQL (BDR, Bucardo). | [Ch 5](../05-replication/README.md) |
| **Tuple** | In the relational model, the formal name for a row. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Turtle / N3** | Human-readable text formats for writing RDF triples. | [Ch 2](../02-data-models-and-query-languages/README.md) |
| **Twitter Snowflake** | Twitter's service that hands out unique, roughly time-ordered 64-bit IDs. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Two Generals Problem** | The puzzle showing two parties can never be certain they agree over an unreliable channel. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |

### U

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **UDP** | User Datagram Protocol: sends packets with no retransmission or ordering; used where late data is useless (e.g., voice calls). | [Ch 8](../08-trouble-with-distributed-systems/README.md), [Ch 11](../11-stream-processing/README.md) |
| **UDP multicast** | Sending one UDP packet to many receivers at once; low latency but unreliable. | [Ch 9](../09-consistency-and-consensus/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Unbounded delay** | Network or processing delays with no upper limit, so a timeout can never be sure a node is dead. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Unbundling databases** | Treating the features inside a database (indexes, views, replication) as separate components wired together with logs. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Unicode / UTF-8** | Unicode is the universal character set; UTF-8 is its most common byte encoding. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Unified batch and stream processing** | Doing both historical reprocessing and live processing in one engine with the same code. | [Ch 12](../12-future-of-data-systems/README.md) |
| **Uniform consensus** | Consensus where even nodes that later crash must not have decided differently. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Uniform interface** | Everything speaking the same format (in Unix, a stream of bytes, often lines of text), so any tool can connect to any other. | [Ch 10](../10-batch-processing/README.md) |
| **Union type** | A schema type allowing a field to be one of several types (e.g., null or string) in Avro. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Uniqueness constraint** | A rule that no two records may share a value (a username, a seat, a request ID). | [Ch 9](../09-consistency-and-consensus/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Unix pipe** | Connecting one program's output to the next program's input (the vertical-bar character in a shell command). | [Ch 10](../10-batch-processing/README.md) |
| **URI** | Uniform Resource Identifier: a string that names a resource; URLs are a kind of URI. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **URL** | Uniform Resource Locator: a web address. | [Ch 10](../10-batch-processing/README.md) |
| **UUID** | Universally unique identifier: a 128-bit random ID that is practically guaranteed to be unique. | [Ch 5](../05-replication/README.md), [Ch 12](../12-future-of-data-systems/README.md) |

### V

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **Vacuum** | PostgreSQL's background process that removes old row versions no transaction can see any more. | [Ch 7](../07-transactions/README.md) |
| **Variable-length integer (varint)** | Encoding small numbers in fewer bytes than large ones. | [Ch 4](../04-encoding-and-evolution/README.md) |
| **Vectorized processing** | Processing a batch of column values in a tight loop at once, using CPU caches and SIMD efficiently. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Version vector** | A set of counters, one per replica, used to tell whether two versions are concurrent or one supersedes the other. | [Ch 5](../05-replication/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Vertex** | A node (thing) in a graph, joined to others by edges. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 10](../10-batch-processing/README.md) |
| **Viewstamped Replication (VSR)** | A consensus algorithm from the 1980s, similar in spirit to Raft. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **Virtual machine (VM)** | A simulated computer running on shared physical hardware. | [Ch 1](../01-reliable-scalable-maintainable/README.md), [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Virtual view** | A saved query that is re-run whenever it is read (an ordinary SQL view). | [Ch 3](../03-storage-and-retrieval/README.md) |
| **VoIP** | Voice over IP: phone calls carried over the internet. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Voldemort** | LinkedIn's open-source Dynamo-style key-value store. | [Ch 5](../05-replication/README.md), [Ch 6](../06-partitioning/README.md) |
| **VoltDB** | An in-memory database that runs transactions one at a time on a single thread per partition. | [Ch 7](../07-transactions/README.md), [Ch 11](../11-stream-processing/README.md) |

### W

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **Wall-clock time** | The current date and time of day, as shown by a time-of-day clock. | [Ch 8](../08-trouble-with-distributed-systems/README.md) |
| **Watch / change notification** | A ZooKeeper feature letting a client be notified when some data changes. | [Ch 9](../09-consistency-and-consensus/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Webhook** | A URL that one service calls when an event happens, to notify another service. | [Ch 11](../11-stream-processing/README.md) |
| **Web service** | A service accessed using HTTP. | [Ch 4](../04-encoding-and-evolution/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **Window** | A time interval used to group stream events for aggregation or joining. | [Ch 11](../11-stream-processing/README.md) |
| **Workflow** | A chain of batch jobs where each one's output becomes the next one's input. | [Ch 10](../10-batch-processing/README.md) |
| **Workflow scheduler** | A tool (e.g., Oozie, Azkaban, Airflow) that runs batch jobs in dependency order. | [Ch 10](../10-batch-processing/README.md) |
| **Working set** | The data a computation needs in memory at once. | [Ch 10](../10-batch-processing/README.md), [Ch 11](../11-stream-processing/README.md) |
| **Write-ahead log (WAL)** | An append-only file where every change is written before it's applied, so a crash can be recovered from. | [Ch 3](../03-storage-and-retrieval/README.md), [Ch 5](../05-replication/README.md) |
| **Write amplification** | One logical write causing several physical writes to disk over time. | [Ch 3](../03-storage-and-retrieval/README.md) |
| **Write conflict** | Two concurrent writes changing the same data in incompatible ways. | [Ch 5](../05-replication/README.md), [Ch 12](../12-future-of-data-systems/README.md) |
| **Write path** | Work done eagerly when data arrives (updating indexes, caches and views). | [Ch 12](../12-future-of-data-systems/README.md) |
| **Write skew** | Two transactions read the same data, then update different records, together breaking a rule each one checked. | [Ch 7](../07-transactions/README.md) |
| **WSDL** | Web Services Description Language: an XML format describing a SOAP API. | [Ch 4](../04-encoding-and-evolution/README.md) |

### X

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **XA** | X/Open eXtended Architecture: the standard API for two-phase commit across heterogeneous systems. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **XML** | Extensible Markup Language: a verbose text format with tags, used for documents and data. | [Ch 2](../02-data-models-and-query-languages/README.md), [Ch 4](../04-encoding-and-evolution/README.md) |
| **XPath / XSL** | Languages for selecting parts of an XML document (XPath) and transforming them (XSL). | [Ch 2](../02-data-models-and-query-languages/README.md) |

### Y

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **YARN** | Hadoop's cluster resource manager, which schedules jobs onto machines. | [Ch 9](../09-consistency-and-consensus/README.md), [Ch 10](../10-batch-processing/README.md) |

### Z

| Term | Plain-English meaning | Where it's used |
|---|---|---|
| **Zab** | ZooKeeper's consensus (total order broadcast) protocol. | [Ch 9](../09-consistency-and-consensus/README.md) |
| **ZeroMQ / nanomsg** | Libraries for sending messages directly between programs without a broker. | [Ch 11](../11-stream-processing/README.md) |
| **ZooKeeper** | An open-source coordination service for leader election, locks, configuration and service discovery. | [Ch 6](../06-partitioning/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
| **zxid** | ZooKeeper's transaction ID, which increases with each change and can serve as a fencing token. | [Ch 8](../08-trouble-with-distributed-systems/README.md), [Ch 9](../09-consistency-and-consensus/README.md) |
