package uk.co.pragmasoft.validate

import org.scalatest.{Matchers, FlatSpec}

class TypeValidationDSLTest extends FlatSpec with Matchers {

  case class TestClass(property1: String, property2: Boolean)

  case class OtherTestClass(property1: String, property2: Boolean)

  import uk.co.pragmasoft.validate.TypeValidationSupport._

  def stringPropertyNotEmptyAssertion: DataValidationFunc[String] = (value: String) => {
    if (value.isEmpty)
      failWithMessage("not be empty")
    else
      validationSuccess(value)
  }

  def prop1EmptyIfProperty2SaysSo: DataValidationFunc[(String, Boolean)] = (value: (String, Boolean)) => {
    if (value._1.isEmpty != value._2)
      failWithMessage("be empty if specified in the associated flag")
    else
      validationSuccess(value)
  }

  def prop1EmptyIfProperty2SaysSoEntity: DataValidationFunc[TestClass] = (value: TestClass) => {
    if (value.property1.isEmpty != value.property2)
      failWithMessage("be empty if specified in the associated flag")
    else
      validationSuccess(value)
  }

  behavior of "A property validation expression"

  it should "create a validation for given extractor" in {
    val validation = typeProperty[TestClass] ("Property1") { _.property1 } must stringPropertyNotEmptyAssertion

    validation(TestClass("not empty", false)) should be(validationSuccess(TestClass("not empty", false)))
    validation(TestClass("", false)).isFailure should be(true)
  }

  it should "create a validation for given typed extractor" in {
    val validation = typeProperty ("Property1") { obj: TestClass => obj.property1 } must stringPropertyNotEmptyAssertion

    validation(TestClass("not empty", false)) should be(validationSuccess(TestClass("not empty", false)))
    validation(TestClass("", false)).isFailure should be(true)
  }

  it should "create a validation for given extractor with different syntax" in {
    val validation = typeProperty[TestClass] ("Property1") extractedWith {
      _.property1
    } must stringPropertyNotEmptyAssertion

    validation(TestClass("not empty", false)) should be(validationSuccess(TestClass("not empty", false)))
    validation(TestClass("", false)).isFailure should be(true)
  }

  it should "support compact definition" in {
    val validation = typeProperty[TestClass] ("Property1") { _.property1 } must stringPropertyNotEmptyAssertion

    validation(TestClass("not empty", false)).isSuccess should be(true)
    validation(TestClass("", false)).isFailure should be(true)
  }


  it should "infer types with new syntax" in {
    """typeProperty[TestClass] ("Property1") { _.property1 } must stringPropertyNotEmptyAssertion""" should compile
  }


  it should "fail with a description joining the property name and the assertion failure description" in {
    val validation = typeProperty[TestClass] ("Property1") { _.property1 } must stringPropertyNotEmptyAssertion

    validation(TestClass("", false)) should be("Property1 must not be empty".asValidationFailure)
  }

  it should "require a property description" in {
    """val validation = typeProperty[TestClass]  { _.property1 }  must stringPropertyNotEmptyAssertion""" shouldNot compile
  }

  it should "enforce property types when composing with assertion" in {
    """typeProperty[TestClass] ("Property2") { _.property2 } must stringPropertyNotEmptyAssertion""" shouldNot compile
  }

  it should "enforce base object type in validation" in {
    """val validation = typeProperty[TestClass] ("Property1") { _.property1 } must stringPropertyNotEmptyAssertion
      |
      |val wrongValidationApplication = validation( OtherTestClass("not empty", false) )
    """.stripMargin shouldNot compile
  }

  it should "support entity validation" in {
    val validation = entity[TestClass] must prop1EmptyIfProperty2SaysSoEntity

    validation(TestClass("", true)).isSuccess should be (true)
    validation(TestClass("", false)).isFailure should be(true)
  }

  it should "support complex property validation" in {
    val validation = typeProperty("Complex") { obj: TestClass => (obj.property1, obj.property2) } must prop1EmptyIfProperty2SaysSo

    validation(TestClass("", true)).isSuccess should be (true)
    validation(TestClass("", false)).isFailure should be(true)
  }
}
