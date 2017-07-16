@file:Suppress("SpringKotlinAutowiring", "MemberVisibilityCanPrivate", "ReplaceArrayOfWithLiteral")

package test

import test.DDLAutoGenerationTest.Config
import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.present
import org.hibernate.SessionFactory
import org.hibernate.cfg.Environment.HBM2DDL_AUTO
import org.hibernate.cfg.Environment.SHOW_SQL
import org.hibernate.validator.constraints.*
import org.hibernate.validator.constraints.CompositionType.*
import org.hsqldb.jdbc.JDBCDataSource
import org.junit.Assert.fail
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
import java.io.PrintWriter
import java.io.StringWriter
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
import kotlin.reflect.KClass

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = arrayOf(Config::class))
@Transactional
open class DDLAutoGenerationTest {
    @Autowired lateinit var sessionFactory: SessionFactory
    val session by lazy { sessionFactory.openSession() }

    @Test @Transactional(readOnly = true) open fun `column constraints were added`() {
        assert(!isNullable("id"))
        assert(!isNullable("nonnull"))
        assert(isNullable("nullable"))
    }

    @Test(expected = ConstraintViolationException::class) @Transactional open fun `hibernate validator is enabled`() {
        val transaction = session.beginTransaction()
        assert.that(transaction, present())

        //              can't be empty  ---v
        session.save(Constraints(1, "foo", ""))

        transaction.commit()
    }

    private fun isNullable(columnName: String) = session.doReturningWork { it ->
        it.metaData.getColumns(null, null, "CONSTRAINTS", null).use nullable@ {
            while (it.next()) {
                val column = it.getString("COLUMN_NAME")

                if (column.equals(columnName, true)) {
                    return@nullable it.getInt("NULLABLE") == 1
                }
            }

            fail("COLUMN `$columnName` is not found!")
            return@nullable false
        }
    }

    @Configuration
    @EnableTransactionManagement
    open class Config {

        @Bean open fun sessionFactory(@Autowired dataSource: DataSource) = LocalSessionFactoryBean().apply {
            setAnnotatedClasses(Constraints::class.java)
            hibernateProperties = Properties().apply {
                setProperty(SHOW_SQL, "true")
                setProperty(HBM2DDL_AUTO, "create-drop")
            }
            setDataSource(dataSource)
        }

        @Bean open fun hsqldb(): JDBCDataSource {
            return JDBCDataSource().apply {
                logWriter = PrintWriter(StringWriter())
                setUrl("jdbc:hsqldb:mem:test")
            }
        }

        @Bean open fun transactionManager(@Autowired sessionFactory: SessionFactory) = HibernateTransactionManager(sessionFactory)
    }
}


@Entity
class Constraints(
        @Id val id: Int,
        @field:[NullOrNotBlank Size(max = 50)] @Column(nullable = false) var nonnull: String,
        @field:[NullOrNotBlank Size(max = 50)] @Column(nullable = true) var nullable: String? = null
)

@[ConstraintComposition(OR) Length(min = 1) Null]
@ReportAsSingleViolation
@Constraint(validatedBy = emptyArray())
annotation class NullOrNotBlank(
        val message: String = "{org.hibernate.validator.constraints.test.NullOrNotBlank.message}",
        val groups: Array<KClass<*>> = emptyArray(),
        val payload: Array<KClass<out Payload>> = emptyArray()
)