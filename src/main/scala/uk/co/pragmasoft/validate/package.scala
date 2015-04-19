package uk.co.pragmasoft

import scala.language.implicitConversions
import scala.annotation.implicitNotFound
import scalaz._
import Scalaz._

package object validate {
  type ValidationResult[T] = ValidationNel[String, T]
  type DataValidationFunction[T] = T => ValidationResult[T]

  protected[validate] def pickFirst[T](first: T, second: T): T = first

  @implicitNotFound(msg = "Unable to find Validation for data type {T}")
  implicit class DataValidation[T]( val self: DataValidationFunction[T] ) extends AnyVal {
    private def concatValidationResults[T]( v1: => ValidationResult[T],  v2: => ValidationResult[T])(composeSuccess: (T, T) => T): ValidationResult[T] =
      (v1 |@| v2) { composeSuccess }

    def and( other: DataValidationFunction[T] ): DataValidationFunction[T] =  (input: T) => {
      concatValidationResults(self(input), other.self(input)) { (_, _) => input }
    }

    def or( other: DataValidationFunction[T] ): DataValidationFunction[T] =  (input: T) => {
      val myValidation = self(input)

      if( myValidation.isSuccess ) {
        myValidation
      } else {
        val otherValidation = other(input)

        if(otherValidation.isSuccess) {
          otherValidation
        } else {
          concatValidationResults(myValidation, otherValidation){ (_, _) => input }
        }
      }
    }

    def withFailFast = QuickFailingDataValidation(self)

    def apply(input: T): ValidationResult[T] = self(input)
  }

  implicit def dataValidationToFunction[T](validation: DataValidation[T]): DataValidationFunction[T] = validation.self

  case class QuickFailingDataValidation[T]( self: DataValidationFunction[T] )  {
    def and( other: DataValidation[T] ): DataValidation[T] =  (input: T) => {
      self(input) match {
        case Success(_) => other(input)
        case quickFailure@Failure(_) => quickFailure
      }
    }

    def apply(input: T): ValidationResult[T] = self(input)
  }

  @implicitNotFound(msg = "Unable to find Validation for data type {T}")
  implicit class ValidatableAny[T : DataValidationFunction ](obj: T) {
    def validated: ValidationResult[T] = {
      val validation = implicitly[DataValidationFunction[T]]
      validation(obj)
    }
  }
}
