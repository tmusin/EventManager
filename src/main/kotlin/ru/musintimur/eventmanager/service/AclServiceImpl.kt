package ru.musintimur.eventmanager.service

import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.acls.domain.GrantedAuthoritySid
import org.springframework.security.acls.domain.ObjectIdentityImpl
import org.springframework.security.acls.domain.PrincipalSid
import org.springframework.security.acls.model.MutableAclService
import org.springframework.security.acls.model.NotFoundException
import org.springframework.security.acls.model.ObjectIdentity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AclServiceImpl(
    private val mutableAclService: MutableAclService,
) : AclService {
    @Transactional
    override fun grantOwnerPermissions(
        domainObject: Any,
        ownerUsername: String,
    ) {
        val oid = ObjectIdentityImpl(domainObject)
        val ownerSid = PrincipalSid(ownerUsername)

        val acl =
            try {
                mutableAclService.readAclById(oid)
            } catch (e: NotFoundException) {
                mutableAclService.createAcl(oid)
            } as org.springframework.security.acls.domain.AclImpl

        acl.insertAce(acl.entries.size, BasePermission.READ, ownerSid, true)
        acl.insertAce(acl.entries.size, BasePermission.WRITE, ownerSid, true)
        acl.insertAce(acl.entries.size, BasePermission.DELETE, ownerSid, true)

        val managerSid = GrantedAuthoritySid("ROLE_MANAGER")
        val adminSid = GrantedAuthoritySid("ROLE_ADMIN")
        acl.insertAce(acl.entries.size, BasePermission.DELETE, managerSid, true)
        acl.insertAce(acl.entries.size, BasePermission.DELETE, adminSid, true)

        mutableAclService.updateAcl(acl)
    }

    @Transactional
    override fun deleteAcl(domainObject: Any) {
        val oid: ObjectIdentity = ObjectIdentityImpl(domainObject)
        mutableAclService.deleteAcl(oid, true)
    }

    @Transactional
    override fun grantManagerWritePermission(domainObject: Any) {
        val oid = ObjectIdentityImpl(domainObject)
        val managerSid = GrantedAuthoritySid("ROLE_MANAGER")
        val adminSid = GrantedAuthoritySid("ROLE_ADMIN")

        val acl =
            try {
                mutableAclService.readAclById(oid)
            } catch (e: NotFoundException) {
                mutableAclService.createAcl(oid)
            } as org.springframework.security.acls.domain.AclImpl

        acl.insertAce(acl.entries.size, BasePermission.WRITE, managerSid, true)
        acl.insertAce(acl.entries.size, BasePermission.WRITE, adminSid, true)
        acl.insertAce(acl.entries.size, BasePermission.DELETE, adminSid, true)

        mutableAclService.updateAcl(acl)
    }
}
