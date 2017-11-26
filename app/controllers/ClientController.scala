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

    //INIT
    def clientList = Action {
        Ok(views.html.client_list())
    }

    //POST AND GET IMPLEMENTATIONS
    def addNewClientRequest() = Action { implicit request =>
        form.bindFromRequest.fold({ errorForm: Form[ClientData] =>
        }, { successClient: ClientData =>
            databaseAddClient(successClient)
        })
        Ok
    }

    def addPhoneNumberToClient() = Action (parse.json) { implicit request =>
        val id = getIntFromJson(request.body, "id")
        val number = getIntFromJson(request.body, "number")
        databaseAddPhoneNumberToClient(id, number)
        Ok
    }

    def getAllRequest = Action { implicit request =>
        val resultList = databaseGetAllClients()
        val jsonResult = Json.obj("clients" -> resultList)
        Ok(jsonResult)
    }

    def getClientsFromSearchRequest = Action (parse.json) { implicit request =>
        val firstName = getStringFromJson(request.body, "searchFormula")
        val resultList = databaseGetClientsByFirstName(firstName)
        val jsonResult = Json.obj("clients" -> resultList)
        Ok(jsonResult)
    }


    //DATABASE
    //Usage: SQLQUERIES(i).format(arg1,...)
    private final val SQLQUERIES = Array(
        "INSERT INTO clients (firstName, age, profession) VALUES ('%s', '%d', '%s');",
        "SELECT * FROM clients WHERE ID='%d';",
        "SELECT * FROM clients;",
        "SELECT * FROM clients LEFT OUTER JOIN phonenumbers USING(ID);",
        "INSERT INTO phonenumbers VALUES ('%d', '%d');",
        "SELECT * FROM clients LEFT OUTER JOIN phonenumbers USING(ID) WHERE firstName LIKE '%s%%';"
    )

    private def databaseAddClient(newClient: ClientData) = db.withConnection { connection: java.sql.Connection =>
        connection.createStatement.executeUpdate(
            SQLQUERIES(0).format(
                newClient.firstName,
                newClient.age,
                newClient.profession
            )
        )
    }

    private def databaseGetAllClients() : List[Client] = db.withConnection { connection: java.sql.Connection =>
        val clientsResult = connection.createStatement().executeQuery(SQLQUERIES(3))
        if( clientsResult == null ) {
            return null
        }
        val clientsBuffer = new ListBuffer[Client]()
        while( clientsResult.next() ) {
            val id = clientsResult.getInt("ID")
            val phoneNumber = clientsResult.getInt("phoneNumber")

            val c = clientsBuffer.find(_.id == id).orNull
            if( c != null ) {
                c.phoneNumbers = phoneNumber::c.phoneNumbers
            } else {
                val numbers : List[Int] = if(phoneNumber == 0) List() else List(phoneNumber)
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
        return clients
    }

    private def databaseAddPhoneNumberToClient(id: Int, number: Int) = db.withConnection { connection: java.sql.Connection =>
        connection.createStatement.executeUpdate(
            SQLQUERIES(4).format(
                id,
                number
            )
        )
    }

    private def databaseGetClientsByFirstName(firstName: String): List[Client] = db.withConnection { connection: java.sql.Connection =>
        val clientsResult = connection.createStatement().executeQuery(
            SQLQUERIES(5).format(firstName)
        )
        if( clientsResult == null ) {
            return null
        }
        val clientsBuffer = new ListBuffer[Client]()
        while( clientsResult.next() ) {
            val id = clientsResult.getInt("ID")
            val phoneNumber = clientsResult.getInt("phoneNumber")

            val c = clientsBuffer.find(_.id == id).orNull
            if( c != null ) {
                c.phoneNumbers = phoneNumber::c.phoneNumbers
            } else {
                val numbers : List[Int] = if(phoneNumber == 0) List() else List(phoneNumber)
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
        return clients
    }

    //UTILS
    private def getLongFromJson(json: JsValue, field: String): Long = {
        return (json \ field).as[Long]
    }

    private def getIntFromJson(json: JsValue, field: String): Int = {
        return (json \ field).as[Int]
    }

    private def getStringFromJson(json: JsValue, field: String): String = {
        return (json \ field).as[String]
    }

    private final implicit val ClientFormat: OFormat[Client] = Json.format[Client]

}
