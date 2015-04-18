package uk.co.pragmasoft.validate

import scala.reflect._

trait TypeValidationDSL[Type] extends TypeValidationSupport {

  protected final def it(implicit classTag: ClassTag[Type]) = entity[Type]

  protected def property(description: String) = typeProperty[Type](description)
}

