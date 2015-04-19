package uk.co.pragmasoft.validate.lenses

import org.scalatest.{FlatSpec, Matchers}
import uk.co.pragmasoft.validate._
import uk.co.pragmasoft.validate.lenses.ScalazLenses._

import scalaz._

class ScalazLensesSpec extends FlatSpec with Matchers {

  import BaseValidations._

  behavior of "Scalaz Lenses Support"

  case class TestClass(val attribute1: String, val attribute2: Int)

  val attr1Lens = Lens.lensu[TestClass, String]( (obj, attr1) => new TestClass(attr1, obj.attribute2),  obj => obj.attribute1 )
  val attr2Lens = Lens.lensu[TestClass, Int]( (obj, attr2) => new TestClass(obj.attribute1, attr2),  obj => obj.attribute2 )

  it should "allow to use a scalaz lens to create a validator" in {

    implicit val validator = new TypeValidator[TestClass]  {
      override def validations =
        ( "Attribute 1" definedBy attr1Lens must beNotEmpty ) and ( "Attribute2" definedBy attr2Lens must beNotNegative )

    }

    TestClass("attrib1", 1).validated should equal (validationSuccess(TestClass("attrib1", 1)))
    TestClass("attrib1", -1).validated should equal (failWithMessage("Attribute2 must be not negative"))

  }


}
