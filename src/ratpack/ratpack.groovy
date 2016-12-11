import com.google.common.reflect.TypeToken
import jooq.tables.Todo
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import ratpack.hikari.HikariModule

import javax.sql.DataSource

//@Grab('io.ratpack:ratpack-groovy:1.4.4')
import static ratpack.groovy.Groovy.ratpack

ratpack {
    bindings {
        module(HikariModule) { config ->
            config.dataSourceClassName = 'org.h2.jdbcx.JdbcDataSource'
            config.addDataSourceProperty('URL', "jdbc:h2:mem:tood;INIT=RUNSCRIPT FROM 'classpath:/init.sql'")
        }
    }

    handlers {
        get {
            render 'Hello world'
        }

        get(':conf') {
            render "Hello ${pathTokens.get('conf')}"
        }

//        get('test') {
//            def testUser = new TestUser(name: 'Kevin')
//            render ratpack.jackson.Jackson.json(testUser)
//        }
        post{
            DataSource ds = get(DataSource)
            DSLContext create = DSL.using(ds, SQLDialect.H2)

            parse(new TypeToken<Map<String, Object>>(){}).map { map ->
                create .newRecord(Todo.TODO, map)
            } .blockingOp {
                it.store()
            }.blockingOp {
                it.refresh()
            }.then { todo ->
                render ratpack.jackson.Jackson.json(todo)
            }
        }
    }
}

class TestUser {
    String name
}
