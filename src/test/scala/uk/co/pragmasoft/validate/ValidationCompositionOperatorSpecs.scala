package uk.co.pragmasoft.validate

import org.scalatest.{Matchers, FlatSpec}
import scalaz._
import Scalaz._

class ValidationCompositionOperatorSpecs extends FlatSpec with Matchers with ValidationPrimitives {

  val successValidation: DataValidation[String] = (in: String) => { in.successNel[String]  }
  val failureValidation: DataValidation[String] = (in: String) => { s"$in is failing".failureNel[String]  }

  behavior of "The AND operator on DataValidations"

  it should "Fail if any of the two validations fails" in {
    (successValidation and failureValidation)("input") should be( NonEmptyList( "input is failing" ).failure)
  }

  it should "Succed if all validations succeed" in {
    (successValidation and successValidation)("input") should be(validationSuccess("input"))
  }


  behavior of "The OR operator on DataValidations"

  it should "Fail if both validations fail" in {
    (failureValidation or failureValidation)("input") should be( NonEmptyList( "input is failing", "input is failing" ).failure )
  }

  it should "Succed if any of the validations succeed (T or T)" in {
    (successValidation or successValidation)("input") should be(validationSuccess("input"))
  }

  it should "Succed if any of the validations succeed (T or F)" in {
    (successValidation or failureValidation)("input") should be(validationSuccess("input"))
  }

  it should "Succed if any of the validations succeed (F or T)" in {
    (failureValidation or successValidation)("input") should be(validationSuccess("input"))
  }

  behavior of "withQuickFAil"

  it should "cause the AND operator to fail without executing the second validation if failure" in {
    ( failureValidation.withFailFast and failureValidation)("input") should be( NonEmptyList( "input is failing" ).failure )
  }

  it should "cause the AND operator to execute the second validation if success" in {
    ( successValidation.withFailFast and failureValidation)("input") should be( NonEmptyList( "input is failing" ).failure )
  }

}
