package models

import play.api.data.Form
import play.api.data.Forms._

object ClientForm {

    case class ClientData(firstName: String, age: Int, profession: String)

    val form = Form(
        mapping(
            "firstName" -> text,
            "age" -> number,
            "profession" -> text
        )(ClientData.apply)(ClientData.unapply)
    )
}
