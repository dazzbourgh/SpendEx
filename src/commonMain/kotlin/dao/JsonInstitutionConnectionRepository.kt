package dao

import model.InstitutionConnection

/**
 * JSON-backed institution connection repository.
 */
expect class JsonInstitutionConnectionRepository() : InstitutionConnectionRepository {
    override suspend fun save(connection: InstitutionConnection)

    override suspend fun list(): Iterable<InstitutionConnection>
}
