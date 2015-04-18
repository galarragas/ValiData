package uk.co.pragmasoft.validate

import java.util.regex.PatternSyntaxException

import scala.util.control.NonFatal
import scala.util.matching.Regex
import scalaz._
import Scalaz._

trait BaseValidations extends ValidationPrimitives {

  def satisfyCriteria[T](failureDescription: String)(criteria: T => Boolean): DataValidation[T] = (value: T) => {
    if(criteria(value))
      validationSuccess(value)
    else
      failWithMessage(failureDescription)
  }

  def beNotEmpty: DataValidation[String] = satisfyCriteria("not be empty") { !_.isEmpty }

  def beNotEmptyIterable[T <: Iterable[_]]: DataValidation[T] = satisfyCriteria("be a non empty iterable") { !_.isEmpty }

  def beDefined: DataValidation[Option[_]] = satisfyCriteria("not be empty")  { _.isDefined }

  def beEmpty: DataValidation[Option[_]] = satisfyCriteria("be empty")  { _.isEmpty }

  def beTrue: DataValidation[Boolean] = satisfyCriteria("be true") { value => value }

  def beNotNegative[T : Numeric]: DataValidation[T] = satisfyCriteria("be not negative") {
    value =>
      val myNumeric = implicitly[Numeric[T]]

      myNumeric.gteq(value, myNumeric.zero)
  }

  def matchRegexOnce(regex: Regex): DataValidation[String] =  satisfyCriteria(s"match regex $regex exactly once") {
    regex.findAllIn(_).length == 1
  }

  def beOfMinimumLength(minLength: Int): DataValidation[String] =  satisfyCriteria(s"Be of a minimum length $minLength") {
    _.length >= minLength
  }

  def haveNoBlankChar: DataValidation[String] = satisfyCriteria(s"Contain no spaces") {
    _.contains(" ") == false
  }

  def all[T]( validation: DataValidation[T] ): DataValidation[Iterable[T]] = (value: Iterable[T]) => {
    value.foldLeft(validationSuccess(value)) {  (validationStatus, elem) =>
      (validationStatus |@| validation(elem)) { (_, _) => value }
    }
  }

  def content[T]( validation: DataValidation[T] ): DataValidation[Option[T]] = (maybeValue: Option[T]) =>  maybeValue match {
    case None => validationSuccess(maybeValue)

    case Some(value) => validation(value) map { result => Some(result) }
  }

  def allMatchRegexOnce[T <: Iterable[String]](regex: Regex): DataValidation[T] = (value: T) => {
    val invalidItemsFound = value find { item =>
      regex.findAllIn(item).length != 1
    }

    if (invalidItemsFound.isDefined)
      failWithMessage(s"match regex '$regex' exactly once for all elements")
    else
      validationSuccess(value)
  }

  def beAValidRegexString(): DataValidation[String] = (value: String) => {
    try {
      value.r
      validationSuccess(value)
    } catch {
      case (e: PatternSyntaxException) =>
        failWithMessage("Your regex does not appear to be valid: " + e.getMessage)  // expected occasionally
      case NonFatal(e) =>
        failWithMessage("Unexpedted issue compiling regex: " + e.getMessage)
    }
  }

  def matchProperties[T](errorMsgNoMatch: String, errorMsgNone : String)(expectedPropertyExtractor: T => String, actualPropertyExtractor: T => Option[String]) : DataValidation[T] = (value : T) => {
    val propertyA: String = expectedPropertyExtractor(value)
    val propertyB: Option[String] = actualPropertyExtractor(value)

    (propertyA, propertyB) match {
      case (_, None) =>
        failWithMessage(errorMsgNone)
      case (valueA, Some(valueB) ) if(valueA != valueB) =>
        failWithMessage(errorMsgNoMatch)
      case _ =>
        validationSuccess(value)
    }
  }

  def beValid[Entity](implicit entityValidation: TypeValidator[Entity]): DataValidation[Entity] = entityValidation
}

object BaseValidations extends BaseValidations