package uk.co.pragmasoft.validate


import scalaz.NonEmptyList
import scala.reflect._


trait TypeValidationSupport extends ValidationPrimitives {

  type PropertyExtractor[OwningEntityType, PropertyType] = OwningEntityType => PropertyType

  case class Property[OwningEntityType, PropertyType](propertyDescription: String, extractor: PropertyExtractor[OwningEntityType, PropertyType]) {
    private def combineValidationMessage(validation: ValidationResult): ValidationResult = {
      validation leftMap { assertionDescriptions: NonEmptyList[String] =>
        assertionDescriptions map { assertionDescription =>  s"$propertyDescription must $assertionDescription" }
      }
    }

    def must( assertion: DataValidation[PropertyType], assertions: DataValidation[PropertyType]* ): DataValidation[OwningEntityType] = {
      extractor andThen requiresAll( (assertion :: assertions.toList):_* ).self andThen combineValidationMessage
    }
  }

  case class PartiallyDefinedTypeProperty[Type](description: String) {
    def extractedWith[PropertyType](extractor: Type => PropertyType) = Property[Type, PropertyType](description, extractor)
    def apply[PropertyType](extractor: Type => PropertyType) = extractedWith[PropertyType](extractor)
  }

  def typeProperty[EntityType](description: String) = PartiallyDefinedTypeProperty[EntityType](description)
  
  def entity[EntityType : ClassTag] = {
    val entityClass = classTag[EntityType].runtimeClass

    Property[EntityType, EntityType](propertyDescription = s"Entity ${entityClass.getSimpleName}", { entity: EntityType => entity } )
  }
}

object TypeValidationSupport extends TypeValidationSupport