package ru.musintimur.eventmanager.service

interface AclService {
    fun grantOwnerPermissions(
        domainObject: Any,
        ownerUsername: String,
    )

    fun deleteAcl(domainObject: Any)

    fun grantManagerWritePermission(domainObject: Any)
}
