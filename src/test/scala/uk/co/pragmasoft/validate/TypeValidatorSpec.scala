package uk.co.pragmasoft.validate

import org.scalatest.{Matchers, FlatSpec}
import uk.co.pragmasoft.validate.lenses.MonocleLensesSpec.TestClass

object TypeValidatorSpec {

  case class TestClass(val stringAttribute: String, val expectedLength: Int, val optionAttribute: Option[String], val listAttribute: List[Int] = List.empty)

  implicit object testClassValidator extends TypeValidator[TestClass] with BaseValidations {
    override def validations = requiresAll(
      "Attribute 1" definedBy { _.stringAttribute } must beNotEmpty,
      "Attribute 2" definedBy { _.expectedLength } must beNonNegative,
      "Attribute 3" definedBy { _.optionAttribute } must { beDefined[String] and content(beValidAsRegex) },
      "Attribute 4" definedBy { _.listAttribute } must { beEmptyIterable[List[Int]] or all( beNonNegative[Int] ) },

      // Can place assertions on several attributes
      "Complex Attribute" definedBy { obj => (obj.expectedLength, obj.stringAttribute) } must {
        satisfyCriteria[(Int, String)]("String should be as long as its declared len") { case (len, string) => string.length == len }
      },

      // Can place assertions on the full object too
      it must {
        satisfyCriteria[TestClass]("Some assertion on the full class") { obj => if(obj.stringAttribute.isEmpty) obj.optionAttribute.isDefined else true }
      }
    )
  }

}

import TypeValidatorSpec._

class TypeValidatorSpec extends FlatSpec with Matchers {

  import BaseValidations._

  behavior of "Type Validator"

  it should "Be implicitly invoked with .validated" in {

    TestClass("attrib1", 1, Some(".+")).validated should equal (validationSuccess(TestClass("attrib1", 1, Some(".+"))))
    TestClass("attrib1", -1, Some(".+")).validated should equal (failWithMessage("Attribute 2 must be not negative"))
    TestClass("attrib1", 1, None).validated should equal (failWithMessage("Attribute 3 must be defined"))
    TestClass("attrib1", 1, Some("[")).validated.isFailure should be(true)

  }


}