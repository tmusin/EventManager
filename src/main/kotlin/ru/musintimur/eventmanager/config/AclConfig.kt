package ru.musintimur.eventmanager.config

import org.springframework.cache.CacheManager
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.acls.AclPermissionCacheOptimizer
import org.springframework.security.acls.AclPermissionEvaluator
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl
import org.springframework.security.acls.domain.AuditLogger
import org.springframework.security.acls.domain.ConsoleAuditLogger
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy
import org.springframework.security.acls.domain.SpringCacheBasedAclCache
import org.springframework.security.acls.jdbc.BasicLookupStrategy
import org.springframework.security.acls.jdbc.JdbcMutableAclService
import org.springframework.security.acls.jdbc.LookupStrategy
import org.springframework.security.acls.model.AclCache
import org.springframework.security.acls.model.MutableAclService
import org.springframework.security.acls.model.PermissionGrantingStrategy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import javax.sql.DataSource

@Configuration
class AclConfig(
    private val dataSource: DataSource,
) {
    @Bean
    fun cacheManager(): CacheManager = ConcurrentMapCacheManager("aclCache")

    @Bean
    fun auditLogger(): AuditLogger = ConsoleAuditLogger()

    @Bean
    fun permissionGrantingStrategy(): PermissionGrantingStrategy = DefaultPermissionGrantingStrategy(auditLogger())

    @Bean
    fun aclAuthorizationStrategy() = AclAuthorizationStrategyImpl(SimpleGrantedAuthority("ROLE_ADMIN"))

    @Bean
    fun aclCache(): AclCache =
        SpringCacheBasedAclCache(
            cacheManager().getCache("aclCache")!!,
            permissionGrantingStrategy(),
            aclAuthorizationStrategy(),
        )

    @Bean
    fun lookupStrategy(): LookupStrategy =
        BasicLookupStrategy(
            dataSource,
            aclCache(),
            aclAuthorizationStrategy(),
            auditLogger(),
        )

    @Bean
    fun mutableAclService(): MutableAclService =
        JdbcMutableAclService(dataSource, lookupStrategy(), aclCache()).apply {
            setClassIdentityQuery("SELECT currval(pg_get_serial_sequence('acl_class', 'id'))")
            setSidIdentityQuery("SELECT currval(pg_get_serial_sequence('acl_sid', 'id'))")
        }

    @Bean
    fun permissionEvaluator(): AclPermissionEvaluator = AclPermissionEvaluator(mutableAclService())

    @Bean
    fun methodSecurityExpressionHandler(): MethodSecurityExpressionHandler =
        DefaultMethodSecurityExpressionHandler().apply {
            setPermissionEvaluator(permissionEvaluator())
            setPermissionCacheOptimizer(AclPermissionCacheOptimizer(mutableAclService()))
        }
}
