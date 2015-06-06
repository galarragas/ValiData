<img src=ValiData.png width=400  />

[![Build Status](https://travis-ci.org/galarragas/ValiData.svg)](http://travis-ci.org/galarragas/ValiData)

Simple Data Validation Framework in Scala
ValiData allows you to write Scalaz based validations using an expressive and flexible DSL.

## How Does It Look Like

You can define a type validator for your class placing assertions on each of its attributes, on a group of its attributes or the whole class 

```scala
  case class TestClass(val stringAttribute: String, val expectedLength: Int, val optionAttribute: Option[String], val listAttribute: List[Int] = List.empty)

  implicit object testClassValidator extends TypeValidator[TestClass] with BaseValidations {
    override def validations = requiresAll(
      "Attribute 1" definedBy { _.stringAttribute } must beNotEmpty,
      "Attribute 2" definedBy { _.expectedLength } must beNonNegative,
      "Attribute 3" definedBy { _.optionAttribute } must { beDefined[String] and content(beValidAsRegex) },
      "Attribute 4" definedBy { _.listAttribute } must { beEmptyIterable[List[Int]] or all( beNonNegative[Int] ) },

      // Can place assertions on several attributes
      "Complex Property" definedBy { obj => (obj.expectedLength, obj.stringAttribute) } must {
        satisfyCriteria[(Int, String)]("String should be as long as its declared len") { case (len, string) => string.length == len }
      },

      // Can place assertions on the full object too
      it must {
        satisfyCriteria[TestClass]("Some assertion on the full class") { obj => if(obj.stringAttribute.isEmpty) obj.optionAttribute.isDefined else true }
      }
    )
  }
```

Once you have a type validator for type `T` in scope you can generate a Scalaz `ValidationNel[String, T]` from any instance of type T simply writing

```scala
    val validation: ValidationNel[String, TestClass] = TestClass("attrib1", 1, Some(".+")).validated
```

That will return you either: the input object wrapped in a Scalaz `Validation.success` or the list of validation errors in a list of `Validation.failureNel` 

To read more about Scalaz Validations please have a look at [the learning ScalaZ section on them](http://eed3si9n.com/learning-scalaz/Validation.html)


### Writing a Validation

To generate a type validator you usually combine a set of validation on the type sub-parts such as type properties.

There is a set of pre-defined basic validations on basic types. The simplest way to write a custom validation is to 
reuse the method `satisfyCriteria` on wich you simply have to provide your validation failure message and the success criteria.

To write more complex validations you just have to provide a function from your input type T to `ValidationNel[String, T]`. 
There are a couple of helper methods to help dealing with Scalaz syntax and generate a success or a failure. The implementation of `satisfyCriteria` should be a good example:

```scala
  def satisfyCriteria[T](failureDescription: String)(criteria: T => Boolean): DataValidationFunction[T] = (value: T) => {
      if(criteria(value))
        validationSuccess(value)
      else
        failWithMessage(failureDescription)
    }
```

### Accessing Base Type Properties

The DSL allows you to define properties and their constraint with a declarative way.
In general a property for type `Type` is defined with the following expression

` "Property Description" definedBy propertyExtractor `

Where `propertyExtractor` is an expression from `Type` to `PropertyType`. 

The simplest example of an extractor is the property getter as in the following example for class `TestClass`

`"Attribute 1" definedBy { _.stringAttribute }`

#### Lenses 

Lenses are a functional way of defining getters and setters for complex structures. The ValiData DSL allows you to use
two different implementation of the Lens concept:

- The Scalaz Lenses, see [The learning ScalaZ section on them](http://eed3si9n.com/learning-scalaz/Lens.html)
- The Monocle Lenses, see [The Monocle GitHub Project](https://github.com/julien-truffaut/Monocle)


To use ScalaZ lenses you simply have to import `uk.co.pragmasoft.validate.lenses.ScalazLenses._` 

```scala
  import uk.co.pragmasoft.validate._
  import scalaz._
  import uk.co.pragmasoft.validate.lenses.ScalazLenses._

  case class TestClass(val attribute1: String, val attribute2: Int)

  val attr1Lens = Lens.lensu[TestClass, String]( (obj, attr1) => new TestClass(attr1, obj.attribute2),  obj => obj.attribute1 )
  val attr2Lens = Lens.lensu[TestClass, Int]( (obj, attr2) => new TestClass(obj.attribute1, attr2),  obj => obj.attribute2 )
  

  implicit val validator = new TypeValidator[TestClass]  {
    override def validations =
      ( "Attribute 1" definedBy attr1Lens must beNotEmpty ) and ( "Attribute2" definedBy attr2Lens must beNonNegative )

  }
```

The usage of the Monocle lenses is similar, just import `uk.co.pragmasoft.validate.lenses.MonocleLenses._`. 
You have to follow Monocle documentation on how to enable the Macro based Lenses if you want to execute the example below or use the other way to define Lenses for a type:

```scala

  import monocle.macros.Lenses
  import uk.co.pragmasoft.validate._
  import uk.co.pragmasoft.validate.lenses.MonocleLenses._
  import scala.language.higherKinds

  @Lenses("_")
  case class TestClass(val attribute1: String, val attribute2: Int, val attribute3: Option[String], val attribute4: List[Int] = List.empty)


  implicit object testClassValidator extends TypeValidator[TestClass] with BaseValidations {

    import TestClass._

    override def validations = requiresAll(
      "Attribute 1" definedBy _attribute1 must beNotEmpty,
      "Attribute 2" definedBy _attribute2 must beNonNegative,
      "Attribute 3" definedBy _attribute3 must { beDefined[String] and content(beValidAsRegex) },
      "Attribute 4" definedBy _attribute4 must { beEmptyIterable[List[Int]] or all( beNonNegative[Int] ) }
    )
  }
 
```

#### Composing validations

Validations are composed with the following operators:

- `validation1 and validation2` to require both validations to be true. This will validate both validations
- `validation1 or validation2` to require either validation1 or validation2 to pass. If validation1 is successful validation2 won't be called
- `requiresAll( validation1, validation2, ... )` to require all validation to pass. This is simply a more compact and efficient way to combine all the validations with `and`
- `all ( validation )` allows to apply a validation on type `T` on an object of type `K <: Iterable[T]`
- `content ( validation )` allows to apply a validation on type `T` on an object of type `Option[T]`

## Including ValiData in Your Project

Add conjars resolver and dependency (only available for 2.11 at the moment): 

```scala
resolvers ++= "Conjars Repo" at "http://conjars.org/repo"

libraryDependencies += "uk.co.pragmasoft" %% "validata" % "0.1"
```


## License

Copyright 2015 PragmaSoft Ltd.

Licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
