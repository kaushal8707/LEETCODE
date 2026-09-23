# Read Replica

A read replica is a read-only copy of a database that stays synchronized with a primary (master) database. Its main purpose is to offload read traffic and improve application scalability.

## How It Works

1. Applications write data to the primary database.
2. The primary records changes (using transaction logs, binary logs, etc.).
3. One or more read replicas asynchronously (or sometimes semi-synchronously) receive and apply those changes.
4. Applications send read queries (e.g., `SELECT`) to the replicas and write queries (`INSERT`, `UPDATE`, `DELETE`) to the primary.

```
              Write
Application ---------> Primary Database
                         |
                         | Replication
          -------------------------------
          |                             |
          v                             v
    Read Replica 1                Read Replica 2
          ^                             ^
          |                             |
         Read                          Read
```

## Benefits

- Improves read performance by distributing read queries.
- Scales horizontally by adding more replicas.
- Reduces load on the primary database.
- Can be used for reporting, analytics, and backups without affecting production performance.
- May improve read latency by placing replicas closer to users geographically.

## Limitations

- **Replication lag:** Replicas may be slightly behind the primary, so recently written data might not be immediately visible.
- **Read-only:** Most managed read replicas do not allow writes.
- **Not a backup:** If data is accidentally deleted on the primary, that deletion is replicated to the replicas.
- **Failover:** A read replica is not automatically a high-availability solution unless promoted or managed by a failover mechanism.

## Example Use Case

An e-commerce website:

- Customers browse products (many read operations).
- Customers place orders (fewer write operations).

Instead of sending all traffic to one database:

- **Product searches and catalog browsing** → Read replicas
- **Order creation and inventory updates** → Primary database

## Read Replica vs. Standby Database

| Read Replica | Standby Database |
|---|---|
| Used for scaling reads | Used primarily for high availability/disaster recovery |
| Accepts read queries | Often not used for application reads |
| May have replication lag | Usually optimized for quick failover |
| Multiple replicas can exist | Typically one or a small number of standby instances |

## Popular Database Support

- **MySQL:** Binary log replication
- **PostgreSQL:** Streaming replication
- **Amazon RDS/Aurora:** Managed read replicas
- **Azure SQL Database:** Read scale-out replicas
- **Google Cloud SQL:** Read replicas

## Summary

Read replicas are designed to improve read scalability and performance, while the primary database continues to handle all write operations.
