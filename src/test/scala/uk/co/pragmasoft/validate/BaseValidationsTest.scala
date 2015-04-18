package uk.co.pragmasoft.validate

import org.scalatest.{Matchers, WordSpec}

class BaseValidationsTest extends WordSpec with Matchers with TypeValidationSupport with BaseValidations {
  case class StringPropTestClass(stringProp: String)
  case class BooleanPropTestClass(booleanProp: Boolean)
  case class OptionTestClass(optionProp: Option[String])
  case class SeqStringClass(seqStringProp: Seq[String])

  val stringProp = typeProperty[StringPropTestClass]("String Property") extractedWith { _.stringProp }
  val booleanProp = typeProperty("Boolean Property") { (obj: BooleanPropTestClass) => obj.booleanProp }
  val optionProp = typeProperty[OptionTestClass]( "Option Property" ) { (obj: OptionTestClass) => obj.optionProp }
  val seqStringProp = typeProperty("Seq[String] Property") { (obj: SeqStringClass) => obj.seqStringProp }

  "beNotEmpty" should {

    val validation = stringProp must beNotEmpty

    "succeed for non empty strings" in {
      validation(StringPropTestClass("non empty")) should be(validationSuccess(StringPropTestClass("non empty")))
    }

    "fail for empty string properties" in {
      validation(StringPropTestClass("")) should be("String Property must not be empty".asValidationFailure)
    }
  }


  "beNotEmptyIter" should {
    val validation = seqStringProp must beNotEmptyIterable

    "succeed for non empty iterables" in {
      validation(SeqStringClass( Seq("a") )) should be (validationSuccess(SeqStringClass( Seq("a") )))
    }

    "fail for empty iterables" in {
      validation(SeqStringClass( Seq.empty )) should be ("Seq[String] Property must be a non empty iterable".asValidationFailure)
    }
  }


  "matchRegexOnce" should {

    val regex = "[0-9]+BODY".r
    val validation = stringProp must matchRegexOnce(regex)

    "succeed for strings matching the regex exactly once" in {
      val testVal: StringPropTestClass = StringPropTestClass("123BODY")
      validation(testVal) should be(validationSuccess(testVal))
    }

    "fail for string non matching the regex" in {
      validation(StringPropTestClass("bla bla")) should be(s"String Property must match regex $regex exactly once".asValidationFailure)
    }

    "fail for strings matching the regex more than once" in {
      validation(StringPropTestClass("123BODY321BODY")) should be(s"String Property must match regex $regex exactly once".asValidationFailure)
    }
  }


  "allMatchRegexOnce" should {

    val regex = "[0-9]+BODY".r
    val validation = seqStringProp must allMatchRegexOnce(regex)

    "succeed for strings matching the regex exactly once" in {
      validation(SeqStringClass( Seq("123BODY") )) should be(validationSuccess(SeqStringClass( Seq("123BODY") )))
      validation(SeqStringClass( Seq("123BODY", "456BODY") )) should be(validationSuccess(SeqStringClass( Seq("123BODY", "456BODY") )))
    }

    "fail for string non matching the regex" in {
      validation(SeqStringClass( Seq("123BODY", "bla bla") )) should be(s"Seq[String] Property must match regex '$regex' exactly once for all elements".asValidationFailure)
    }

    "fail for strings matching the regex more than once" in {
      validation(SeqStringClass( Seq("123BODY", "123BODY321BODY")) ) should be(s"Seq[String] Property must match regex '$regex' exactly once for all elements".asValidationFailure)
    }
  }

}
