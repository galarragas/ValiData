package uk.co.pragmasoft.validate.lenses

import monocle.macros.Lenses
import org.scalatest.{FlatSpec, Matchers}
import uk.co.pragmasoft.validate._
import scala.language.higherKinds

object MonocleLensesSpec {

  @Lenses("_")
  case class TestClass(val attribute1: String, val attribute2: Int, val attribute3: Option[String], val attribute4: List[Int] = List.empty)

  import uk.co.pragmasoft.validate.lenses.MonocleLenses._

  implicit object testClassValidator extends TypeValidator[TestClass] with BaseValidations {

    import TestClass._

    override def validations = requiresAll(
      "Attribute 1" definedBy _attribute1 must beNotEmpty,
      "Attribute 2" definedBy _attribute2 must beNonNegative,
      "Attribute 3" definedBy _attribute3 must { beDefined[String] and content(beValidAsRegex) },
      "Attribute 4" definedBy _attribute4 must { beEmptyIterable[List[Int]] or all( beNonNegative[Int] ) }
    )
  }

}

import MonocleLensesSpec._

class MonocleLensesSpec extends FlatSpec with Matchers {

  import BaseValidations._

  behavior of "Monocle Lenses Support"

  it should "allow to use a monocle lens to create a validator" in {

    TestClass("attrib1", 1, Some(".+")).validated should equal (validationSuccess(TestClass("attrib1", 1, Some(".+"))))
    TestClass("attrib1", -1, Some(".+")).validated should equal (failWithMessage("Attribute 2 must be not negative"))
    TestClass("attrib1", 1, None).validated should equal (failWithMessage("Attribute 3 must be defined"))
    TestClass("attrib1", 1, Some("[")).validated.isFailure should be(true)

  }


}
