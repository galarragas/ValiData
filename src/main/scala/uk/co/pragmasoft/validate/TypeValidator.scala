package uk.co.pragmasoft.validate

trait TypeValidator[EntityType] extends Function1[EntityType, ValidationResult[EntityType]] with TypeValidationDSL[EntityType] {
  def validations: DataValidationFunction[EntityType]

  override def apply(entityType: EntityType): ValidationResult[EntityType] = validations(entityType)
}
