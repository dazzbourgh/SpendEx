package dao

import model.InstitutionConnection

/**
 * Persistence abstraction for linked institution connections.
 */
interface InstitutionConnectionRepository {
    /**
     * Persists a linked institution connection.
     *
     * @param connection Connection to save
     */
    suspend fun save(connection: InstitutionConnection)

    /**
     * Returns all linked institution connections.
     *
     * @return All stored connections
     */
    suspend fun list(): Iterable<InstitutionConnection>
}
