package uk.co.pragmasoft.validate.lenses

import monocle.macros.Lenses
import org.scalatest.{FlatSpec, Matchers}
import uk.co.pragmasoft.validate._
import uk.co.pragmasoft.validate.lenses.MonocleLenses._
import scala.language.higherKinds

class MonocleLensesSpec extends FlatSpec with Matchers {

  import BaseValidations._

  behavior of "Monocle Lenses Support"

  @Lenses("_")
  case class TestClass(val attribute1: String, val attribute2: Int, val attribute3: Option[String])

  it should "allow to use a monocle lens to create a validator" in {

    implicit val validator = new TypeValidator[TestClass]  {
      import TestClass._

      val attribute3 = "Attribute 3" definedBy _attribute3

      override def validations =
        ( "Attribute 1" definedBy _attribute1 must beNotEmpty ) and ( "Attribute 2" definedBy _attribute2 must beNotNegative ) and (
            attribute3 must ( beDefined[String] and content( beValidAsRegex ) )
          )

    }

    TestClass("attrib1", 1, Some(".+")).validated should equal (validationSuccess(TestClass("attrib1", 1, Some(".+"))))
    TestClass("attrib1", -1, Some(".+")).validated should equal (failWithMessage("Attribute 2 must be not negative"))
    TestClass("attrib1", 1, None).validated should equal (failWithMessage("Attribute 3 must be defined"))
    TestClass("attrib1", 1, Some("[")).validated.isFailure should be(true)

  }


}
