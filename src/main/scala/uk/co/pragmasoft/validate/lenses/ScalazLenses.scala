package uk.co.pragmasoft.validate.lenses

import _root_.scalaz._

object ScalazLenses {
  implicit class ScalazLensExtractor[EntityType, PropertyType](lense: Lens[EntityType, PropertyType]) extends Function1[EntityType, PropertyType] {
    override def apply(v1: EntityType): PropertyType = lense.get(v1)
  }
}
