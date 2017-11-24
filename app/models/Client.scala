package models

case class Client(id: Int, firstName: String, age: Int, profession: String, var phoneNumbers: List[Long])