package org.onlab.onos.store.service;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.onlab.onos.cluster.ControllerNode;

/**
 * Service interface for running administrative tasks on a Database.
 */
public interface DatabaseAdminService {

    /**
     * Creates a new table.
     * Table creation is idempotent. Attempting to create a table
     * that already exists will be a noop.
     * @param name table name.
     * @return true if the table was created by this call, false otherwise.
     */
    public boolean createTable(String name);

    /**
     * Creates a new table where last update time will be used to track and expire old entries.
     * Table creation is idempotent. Attempting to create a table
     * that already exists will be a noop.
     * @param name table name.
     * @param ttlMillis total duration in millis since last update time when entries will be expired.
     * @return true if the table was created by this call, false otherwise.
     */
    public boolean createTable(String name, int ttlMillis);

    /**
     * Lists all the tables in the database.
     * @return set of table names.
     */
    public Set<String> listTables();

    /**
     * Deletes a table from the database.
     * @param name name of the table to delete.
     */
    public void dropTable(String name);

    /**
     * Deletes all tables from the database.
     */
    public void dropAllTables();


    /**
     * Add member to default Tablet.
     *
     * @param node to add
     */
    public void addMember(ControllerNode node);

    /**
     * Remove member from default Tablet.
     *
     * @param node node to remove
     */
    public void removeMember(ControllerNode node);

    /**
     * List members forming default Tablet.
     *
     * @return Copied collection of members forming default Tablet.
     */
    public Collection<ControllerNode> listMembers();

    /**
     * Returns the current Leader of the default Tablet.
     *
     * @return leader node
     */
    public Optional<ControllerNode> leader();
}
