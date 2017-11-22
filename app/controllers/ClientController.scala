package controllers

import com.google.inject.Inject
import play.api.data.Form
import play.api.db.Database
import play.api.mvc.{AbstractController, ControllerComponents}

class ClientController @Inject()(db: Database, cc: ControllerComponents) extends AbstractController(cc) {
    private final val SQLQUERIES = Array(
        "INSERT INTO clients (firstName, age, profession) VALUES ('%s', '%d', '%s');",
        "bbb"
    )

    import models.ClientForm._

    def addRequest = Action { implicit request =>
        val client = form.bindFromRequest.fold({ errorForm: Form[Client] =>
        }, { successClient: Client =>
            println(successClient.firstName)
            databaseAddClient(successClient)
        })
        Ok
    }

    private def databaseAddClient(newClient: Client) = db.withConnection { connection: java.sql.Connection =>
        println(SQLQUERIES(0).format(newClient.firstName, newClient.age, newClient.profession))
        connection.createStatement.executeUpdate(
            SQLQUERIES(0).format(
                newClient.firstName,
                newClient.age,
                newClient.profession
            )
        )
    }


}
