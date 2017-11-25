package controllers

import com.google.inject.Inject
import models.Client
import play.api.data.Form
import play.api.db.Database
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.collection.mutable.ListBuffer

class ClientController @Inject()(db: Database, cc: ControllerComponents) extends AbstractController(cc) {
    import models.ClientForm._

    def clientList = Action {
        Ok(views.html.client_list())
    }

    //POST AND GET IMPLEMENTATIONS
    def addNewClientRequest() = Action { implicit request =>
        form.bindFromRequest.fold({ errorForm: Form[ClientData] =>
        }, { successClient: ClientData =>
            println(successClient.firstName)
            databaseAddClient(successClient)
        })
        Ok
    }

    def addPhoneNumberToClient() = Action (parse.json) { implicit request =>
        val id = getLongFromJson(request.body, "id").toInt
        val number = getLongFromJson(request.body, "number")
        databaseAddPhoneNumberToClient(id, number)
        Ok
    }

    def getAllRequest = Action { implicit request =>
        val resultList = databaseGetAllClients()
        implicit val clientFormat: OFormat[Client] = Json.format[Client]
        val jsonResult = Json.obj("clients" -> resultList)
        println(jsonResult)
        Ok(jsonResult)
    }


    //DATABASE
    //Usage: SQLQUERIES(i).format(arg1,...)
    private final val SQLQUERIES = Array(
        "INSERT INTO clients (firstName, age, profession) VALUES ('%s', '%d', '%s');",
        "SELECT * FROM clients WHERE ID='%d';",
        "SELECT * FROM clients;",
        "SELECT * FROM clients LEFT OUTER JOIN phonenumbers USING(ID);",
        "INSERT INTO phonenumbers VALUES ('%d', '%d');"
    )

    private def databaseAddClient(newClient: ClientData) = db.withConnection { connection: java.sql.Connection =>
        println(SQLQUERIES(0).format(newClient.firstName, newClient.age, newClient.profession))
        connection.createStatement.executeUpdate(
            SQLQUERIES(0).format(
                newClient.firstName,
                newClient.age,
                newClient.profession
            )
        )
    }

    private def databaseGetClientByID(id: Int): ClientData = db.withConnection { connection: java.sql.Connection =>
        println(SQLQUERIES(1).format(id))
        val result = connection.createStatement.executeQuery(SQLQUERIES(1).format(id))
        if( result.next() ){
            val nClient = form.bind(
                Map("firstName" -> result.getString("firstName"),
                    "age" -> result.getString("age"),
                    "profession" -> result.getString("profession")
                )
            ).get
            println("name: " + nClient.firstName)
            return nClient
        }
        return null
    }

    private def databaseGetAllClients() : List[Client] = db.withConnection { connection: java.sql.Connection =>
        val clientsResult = connection.createStatement().executeQuery(SQLQUERIES(3))

        if( clientsResult == null ) {
            return null
        }

        val clientsBuffer = new ListBuffer[Client]()
        while( clientsResult.next() ) {
            val id = clientsResult.getInt("ID")
            val phoneNumber = clientsResult.getLong("phoneNumber")
            println(phoneNumber)

            val c = clientsBuffer.find(_.id == id).orNull
            if( c != null ) {
                c.phoneNumbers = phoneNumber::c.phoneNumbers
            } else {
                val numbers : List[Long] = if(phoneNumber == 0) List() else List(phoneNumber)
                val newClient = Client(
                    clientsResult.getInt("ID"),
                    clientsResult.getString("firstName"),
                    clientsResult.getInt("age"),
                    clientsResult.getString("profession"),
                    numbers
                )
                clientsBuffer += newClient
            }
        }
        val clients = clientsBuffer.toList.sortBy(_.id)
        clients.foreach(println)
        return clients
    }

    private def databaseAddPhoneNumberToClient(id: Int, number: Long) = db.withConnection { connection: java.sql.Connection =>
        println(SQLQUERIES(4).format(id, number))
        connection.createStatement.executeUpdate(
            SQLQUERIES(4).format(
                id,
                number
            )
        )
    }

    //UTILS
    private def getLongFromJson(json: JsValue, field: String): Long = {
        return (json \ field).as[Long]
    }
}
