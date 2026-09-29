package ch07.tx;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.RollbackOn;

/**
 * Spring Framework 6.2+: make checked exceptions roll back too, for every @Transactional method.
 * Active only with the "rollback-all" profile so the other demos keep the default rules.
 */
@Configuration
@Profile("rollback-all")
@EnableTransactionManagement(proxyTargetClass = true, rollbackOn = RollbackOn.ALL_EXCEPTIONS)
public class RollbackOnAllExceptionsConfig {
}
