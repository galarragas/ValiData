package uk.co.pragmasoft.validate

import scalaz._
import Scalaz._


trait ValidationPrimitives {
  def validationSuccess[T](value: T): ValidationResult[T] = value.successNel[String]

  def failWithMessage[T](errorMsg: String): ValidationResult[T] = errorMsg.failureNel[T]

  implicit class PumpedString(val str: String) extends AnyRef {
    def asValidationFailure = failWithMessage(str)
  }

  def NoValidation[T]  = (input: T) => validationSuccess[T](input)

  def requiresAll[T](validations: DataValidationFunction[T]*): DataValidationFunction[T] = (input: T) => {
    val _validationResult: ValidationResult[T] =
      validations map {
        _.self(input)
      } reduce { (v1, v2) => (v1 |@| v2) { (_, _) => input } }

    _validationResult
  }

}

object ValidationPrimitives extends ValidationPrimitives