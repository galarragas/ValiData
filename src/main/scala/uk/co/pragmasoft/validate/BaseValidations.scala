package uk.co.pragmasoft.validate

import java.util.regex.PatternSyntaxException

import scala.util.control.NonFatal
import scala.util.matching.Regex
import scalaz._
import Scalaz._

trait BaseValidations extends ValidationPrimitives {

  def satisfyCriteria[T](failureDescription: String)(criteria: T => Boolean): DataValidationFunction[T] = (value: T) => {
    if(criteria(value))
      validationSuccess(value)
    else
      failWithMessage(failureDescription)
  }

  def beNotEmpty = satisfyCriteria("be a non-empty string") {  value: String => !value.isEmpty }

  def beEmptyString = satisfyCriteria[String]("be empty")  { _.isEmpty }

  def beNotEmptyIterable[T <: Iterable[_]] = satisfyCriteria("be non empty") { value: T => !value.isEmpty }

  def beEmptyIterable[T <: Iterable[_]] = satisfyCriteria("be empty") { value: T => value.isEmpty }

  def beDefined[T] = satisfyCriteria[Option[T]]("be defined")  { _.isDefined }

  def beEmpty[T] = satisfyCriteria[Option[T]]("be empty")  { _.isEmpty }

  def beTrue = satisfyCriteria[Boolean]("be true") { value => value }

  def beNonNegative[T : Numeric] = satisfyCriteria[T]("be not negative") {
    value =>
      val myNumeric = implicitly[Numeric[T]]

      myNumeric.gteq(value, myNumeric.zero)
  }

  def bePositive[T : Numeric] = satisfyCriteria[T]("be positive") {
    value =>
      val myNumeric = implicitly[Numeric[T]]

      myNumeric.gt(value, myNumeric.zero)
  }

  def matchRegexOnce(regex: Regex) =  satisfyCriteria[String](s"match regex $regex exactly once") {
    regex.findAllIn(_).length == 1
  }

  def beOfMinimumLength(minLength: Int) = satisfyCriteria[String](s"Be of a minimum length $minLength") {
    _.length >= minLength
  }

  def haveNoBlankChar = satisfyCriteria[String](s"Contain no spaces") {
    _.contains(" ") == false
  }

  def all[T, Iter <: Iterable[T]]( validation: DataValidationFunction[T] ): DataValidationFunction[Iter] = (value: Iter) => {
    value.foldLeft(validationSuccess(value)) {  (validationStatus, elem) =>
      (validationStatus |@| validation(elem)) { (_, _) => value }
    }
  }

  def content[T]( validation: DataValidationFunction[T] ) = (maybeValue: Option[T]) =>  maybeValue match {
    case None => validationSuccess(maybeValue)

    case Some(value) => validation(value) map { result => Some(result) }
  }

  def allMatchRegexOnce[T <: Iterable[String]](regex: Regex) = (value: T) => {
    val invalidItemsFound = value find { item =>
      regex.findAllIn(item).length != 1
    }

    if (invalidItemsFound.isDefined)
      failWithMessage(s"match regex '$regex' exactly once for all elements")
    else
      validationSuccess(value)
  }

  def beValidAsRegex = (value: String) => {
    try {
      value.r
      validationSuccess(value)
    } catch {
      case (e: PatternSyntaxException) =>
        failWithMessage(s"be a valid regex but regex compilation failed with pattern syntax exception ${e.getMessage}")
      case NonFatal(e) =>
        failWithMessage(s"be a valid regex but regex compilation failed with exception $e")
    }
  }

  def beValid[Entity](implicit entityValidation: DataValidationFunction[Entity]): DataValidationFunction[Entity] = entityValidation
}

object BaseValidations extends BaseValidations