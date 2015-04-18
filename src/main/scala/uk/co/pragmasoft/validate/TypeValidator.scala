package uk.co.pragmasoft.validate

trait TypeValidator[EntityType] extends Function1[EntityType, ValidationResult] with TypeValidationDSL[EntityType] {
  def validations: DataValidation[EntityType]

  override def apply(entityType: EntityType): ValidationResult = validations(entityType)
}
