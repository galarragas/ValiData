package uk.co.pragmasoft.validate.lenses

import scalaz._

object ScalazLenses {
  implicit class ScalazLensExtractor[EntityType, PropertyType](val lense: Lens[EntityType, PropertyType]) extends Function1[EntityType, PropertyType] {
    override def apply(v1: EntityType): PropertyType = lense.get(v1)
  }
}
