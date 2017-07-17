@file:Suppress("SpringKotlinAutowiring", "MemberVisibilityCanPrivate", "EXPERIMENTAL_FEATURE_WARNING")

package test

import org.hibernate.SessionFactory
import org.hibernate.cfg.Environment.HBM2DDL_AUTO
import org.hibernate.cfg.Environment.SHOW_SQL
import org.hibernate.validator.constraints.CompositionType.OR
import org.hibernate.validator.constraints.ConstraintComposition
import org.hibernate.validator.constraints.Length
import org.hibernate.validator.constraints.NotBlank
import org.hsqldb.jdbc.JDBCDataSource
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.orm.hibernate5.HibernateTransactionManager
import org.springframework.orm.hibernate5.LocalSessionFactoryBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.sql.DataSource
import javax.validation.Constraint
import javax.validation.ConstraintViolationException
import javax.validation.Payload
import javax.validation.ReportAsSingleViolation
import javax.validation.constraints.Null
import javax.validation.constraints.Size
import kotlin.coroutines.experimental.buildSequence
import kotlin.reflect.KClass

@RunWith(SpringRunner::class)
@ContextConfiguration
open class DDLAutoGenerationTest {
    @Autowired lateinit var sessionFactory: SessionFactory
    val session by lazy { sessionFactory.openSession() }

    @Test open fun `column constraints was changed by hibernate-validator`() {
        val required = tableColumnsNonNullConstraint("constraints")

        assertTrue(required["not_blank"]!!)
    }

    @Test open fun `column constraints were added as its column definition`() {
        val required = tableColumnsNonNullConstraint("constraints")

        assertTrue(required["id"]!!)
        assertFalse(required["not_empty"]!!)
    }

    @Test(expected = ConstraintViolationException::class) @Transactional open fun `reports error when violate column constraints`() {
        val transaction = session.beginTransaction()

        //              can't be empty  ---v
        session.save(Constraints(1, "foo", ""))

        transaction.commit()
    }

    private fun tableColumnsNonNullConstraint(table: String) = session.doReturningWork constraints@ { it ->
        val ALL = null
        return@constraints it.metaData.getColumns(ALL, ALL, table.toUpperCase(), ALL).use {
            buildSequence {
                while (it.next()) yield(it.getString("COLUMN_NAME").toLowerCase() to !it.getBoolean("NULLABLE"))
            }.associateBy({ it.first }, { it.second })
        }
    }

    @Configuration
    @EnableTransactionManagement
    open class Config {

        @Bean open fun sessionFactory(@Autowired dataSource: DataSource) = LocalSessionFactoryBean().apply {
            setAnnotatedClasses(Constraints::class.java)
            setDataSource(dataSource)
            hibernateProperties = Properties().apply {
                setProperty(SHOW_SQL, "true")
                setProperty(HBM2DDL_AUTO, "create-drop")
            }
        }

        @Bean open fun hsqldb() = JDBCDataSource().apply { setUrl("jdbc:hsqldb:mem:test") }

        @Bean open fun transactionManager(@Autowired sessionFactory: SessionFactory) = HibernateTransactionManager(sessionFactory)
    }
}


@Entity
class Constraints(
        @Id val id: Int,
        @field:[NullOrNotBlank Size(max = 50)] @Column(nullable = true) var not_blank: String? = null,
        @field:[NullOrNotEmpty Size(max = 50)] @Column(nullable = true) var not_empty: String? = null
)

@[ConstraintComposition(OR) Length(min = 1) Null]
@ReportAsSingleViolation
@Constraint(validatedBy = emptyArray())
annotation class NullOrNotEmpty(
        val message: String = "{org.hibernate.validator.constraints.test.NullOrNotBlank.message}",
        val groups: Array<KClass<*>> = emptyArray(),
        val payload: Array<KClass<out Payload>> = emptyArray()
)


@[ConstraintComposition(OR) NotBlank Null]
@ReportAsSingleViolation
@Constraint(validatedBy = emptyArray())
annotation class NullOrNotBlank(
        val message: String = "{org.hibernate.validator.constraints.test.NullOrNotBlank.message}",
        val groups: Array<KClass<*>> = emptyArray(),
        val payload: Array<KClass<out Payload>> = emptyArray()
)