package uk.co.pragmasoft.validate

import scalaz._
import Scalaz._


trait ValidationPrimitives {
  lazy val VALIDATION_SUCCESS: ValidationResult = ().successNel[String]

  def failWithMessage(errorMsg: String): ValidationResult = errorMsg.failureNel[Unit]

  implicit class PumpedString(val str: String) extends AnyRef {
    def asValidationFailure = failWithMessage(str)
  }

  def NO_VALIDATION: DataValidation[Any] = (value: Any) => {
    VALIDATION_SUCCESS
  }

  def requiresAll[T](validations: DataValidation[T]*): DataValidation[T] = (input: T) => {
    val _validationResult: ValidationResult =
      validations map {
        _.self(input)
      } reduce { (v1, v2) => (v1 |@| v2) { (u1, u2) => () } }

    _validationResult
  }

}

object ValidationPrimitives extends ValidationPrimitives