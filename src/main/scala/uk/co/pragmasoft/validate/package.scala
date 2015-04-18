package uk.co.pragmasoft

import scala.annotation.implicitNotFound
import scalaz._
import Scalaz._

package object validate {
  type ValidationResult = ValidationNel[String, Unit]
  type DataValidationFunc[T] = T => ValidationResult

  @implicitNotFound(msg = "Unable to find Validation for data type {T}")
  implicit class DataValidation[-T]( val self: DataValidationFunc[T] ) extends AnyVal {
    private def concatValidationResults( v1: => ValidationResult,  v2: => ValidationResult): ValidationResult = (v1 |@| v2) { (u1, u2) => () }

    def and[K <: T]( other: DataValidation[K] ): DataValidation[K] =  (input: K) => {
      concatValidationResults(self(input), other.self(input))
    }

    def or[K <: T]( other: DataValidation[K] ): DataValidation[K] =  (input: K) => {
      val myValidation = self(input)

      if( myValidation.isSuccess ) {
        myValidation
      } else {
        val otherValidation = other(input)

        if(otherValidation.isSuccess) {
          otherValidation
        } else {
          concatValidationResults(myValidation, otherValidation)
        }
      }
    }

    def withFailFast = QuickFailingDataValidation(self)

    def apply(input: T): ValidationResult = self(input)
  }

  case class QuickFailingDataValidation[-T]( self: DataValidationFunc[T] )  {
    def and[K <: T]( other: DataValidation[K] ): DataValidation[K] =  (input: K) => {
      self(input) match {
        case Success(_) => other(input)
        case quickFailure@Failure(_) => quickFailure
      }
    }

    def apply(input: T): ValidationResult = self(input)
  }

}
