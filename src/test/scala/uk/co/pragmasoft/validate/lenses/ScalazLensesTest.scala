package uk.co.pragmasoft.validate.lenses

import org.scalatest.{FlatSpec, Matchers}

import uk.co.pragmasoft.validate._
import ScalazLenses._

import scalaz._
import Scalaz._

class ScalazLensesTest extends FlatSpec with Matchers {

  import BaseValidations._

  behavior of "Scalaz Lenses Converter"

  class TestClass(val attribute1: String, val attribute2: Int)

  val attr1Lens = Lens.lensu[TestClass, String]( (obj, attr1) => new TestClass(attr1, obj.attribute2),  obj => obj.attribute1 )
  val attr2Lens = Lens.lensu[TestClass, Int]( (obj, attr2) => new TestClass(obj.attribute1, attr2),  obj => obj.attribute2 )

  it should "allow to use a scalaz lens to create a validator" in {

    implicit val validator = new TypeValidator[TestClass]  {
      override def validations: DataValidation[TestClass] =
        ( property("Attribute 1") extractedWith attr1Lens must beNotEmpty ) and ( property("Attribute2") extractedWith attr2Lens must beNotNegative )

    }

    val validObj = new TestClass("attrib1", 1)
    val notValidObj = new TestClass("attrib1", -1)

    validator(validObj) should be(validationSuccess(validObj))
    validator(notValidObj) should be(failWithMessage("Attribute2 must be not negative"))

  }


}
