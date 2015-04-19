package uk.co.pragmasoft.validate


import scalaz.NonEmptyList
import scala.reflect._


trait TypeValidationSupport extends ValidationPrimitives {

  type PropertyExtractor[OwningEntityType, PropertyType] = OwningEntityType => PropertyType

  case class Property[OwningEntityType, PropertyType](propertyDescription: String,  extractor: PropertyExtractor[OwningEntityType, PropertyType]) {
    private def combineValidationMessage(validation: ValidationResult[PropertyType]): ValidationResult[PropertyType] = {
      validation leftMap { assertionDescriptions: NonEmptyList[String] =>
        assertionDescriptions map { assertionDescription =>  s"$propertyDescription must $assertionDescription" }
      }
    }

    def must( assertion: DataValidationFunction[PropertyType], assertions: DataValidationFunction[PropertyType]* ): DataValidation[OwningEntityType] = {
      val validateProperty = requiresAll( (assertion :: assertions.toList):_* ).self andThen combineValidationMessage

      (entityValue: OwningEntityType) => {
        val propertyValue = extractor(entityValue)

        validateProperty(propertyValue) map { _ => entityValue }
      }
    }
  }

  case class UnaccessibleNamedProperty[Type](description: String) {
    def definedBy[PropertyType](extractor: Type => PropertyType) = Property[Type, PropertyType](description, extractor)
    def apply[PropertyType](extractor: Type => PropertyType) = definedBy[PropertyType](extractor)
  }

  case class ReadOnlyPropertyPropertyType(description: String)

  def typeProperty[EntityType](description: String) = UnaccessibleNamedProperty[EntityType](description)
  
  def entity[EntityType : ClassTag] = {
    val entityClass = classTag[EntityType].runtimeClass

    Property[EntityType, EntityType](
      propertyDescription = s"Entity ${entityClass.getSimpleName}",
      extractor = { entity: EntityType => entity }
    )
  }
}

object TypeValidationSupport extends TypeValidationSupport