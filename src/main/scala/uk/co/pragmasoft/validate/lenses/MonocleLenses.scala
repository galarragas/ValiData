package uk.co.pragmasoft.validate.lenses

import monocle.Lens

object MonocleLenses {

  implicit class MonocleLensExtractor[EntityType, PropertyType](lense: Lens[EntityType, PropertyType]) extends Function1[EntityType, PropertyType] {
    override def apply(v1: EntityType): PropertyType = lense.get(v1)
  }

}