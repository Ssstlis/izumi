package izumi.fundamentals.reflection.macrortti

import izumi.fundamentals.reflection.macrortti.LightTypeTagRef.{AbstractReference, Lambda}

sealed trait LightTypeTagRef {
  def combine(o: Seq[LightTypeTagRef]): AbstractReference = {
    applyParameters {
      l =>
        l.input.zip(o).map {
          case (p, v: AbstractReference) =>
            p.name -> v
        }.toMap
    }
  }

  def combineNonPos(o: Seq[Option[LightTypeTagRef]]): AbstractReference = {
    applyParameters {
      l =>
        l.input.zip(o).flatMap {
          case (p, v) =>
            v match {
              case Some(value: AbstractReference) =>
                Seq(p.name -> value)
              case None =>
                Seq.empty
            }
        }.toMap
    }
  }

  def combine(o: Map[String, LightTypeTagRef]): AbstractReference = {
    val parameters = o.map {
      case (p, v: AbstractReference) =>
        p -> v
    }

    applyParameters(_ => parameters)
  }

  private def applyParameters(p: Lambda => Map[String, AbstractReference]): AbstractReference = {
    this match {
      case l: Lambda =>
        val parameters = p(l)
        if (l.input.size < parameters.size) {
          throw new IllegalArgumentException(s"$this expects no more than ${l.input.size} parameters: ${l.input} but got $parameters")
        }
        val expected = l.input.map(_.name).toSet
        val unknownKeys = parameters.keySet.diff(expected)
        if (unknownKeys.nonEmpty) {
          throw new IllegalArgumentException(s"$this takes parameters: $expected but got unexpected ones: $unknownKeys")
        }


        val applied = RuntimeAPI.applyLambda(l, parameters)
        applied
      case _ =>
        throw new IllegalArgumentException(s"$this is not a type lambda, it cannot be parameterized")
    }
  }
}


object LightTypeTagRef {


  import LTTRenderables.Short._


  sealed trait AbstractReference extends LightTypeTagRef

  case class Lambda(input: List[LambdaParameter], output: AbstractReference) extends AbstractReference {
    override def toString: String = this.render()
  }

  case class LambdaParameter(name: String) {
    override def toString: String = this.render()
  }

  sealed trait AppliedReference extends AbstractReference

  sealed trait AppliedNamedReference extends AppliedReference {
    def asName: NameReference
  }

  case class IntersectionReference(refs: Set[AppliedNamedReference]) extends AppliedReference {
    override def toString: String = this.render()
  }

  case class NameReference(ref: String, boundaries: Boundaries, prefix: Option[AppliedReference]) extends AppliedNamedReference {

    override def asName: NameReference = this

    override def toString: String = this.render()
  }

  object NameReference {
    def apply(ref: String, boundaries: Boundaries = Boundaries.Empty, prefix: Option[AppliedReference] = None): NameReference = new NameReference(ref, boundaries, prefix)
  }

  case class FullReference(ref: String, parameters: List[TypeParam],  prefix: Option[AppliedReference]) extends AppliedNamedReference {

    override def asName: NameReference = NameReference(ref, prefix = prefix)

    override def toString: String = this.render()
  }

  object FullReference {
    def apply(ref: String, parameters: List[TypeParam], prefix: Option[AppliedReference] = None): FullReference = new FullReference(ref, parameters, prefix)
  }

  case class TypeParam(ref: AbstractReference, variance: Variance) {
    override def toString: String = this.render()
  }

  sealed trait RefinementDecl

  object RefinementDecl {

    case class Signature(name: String, input: List[AppliedReference], output: AppliedReference) extends RefinementDecl

    case class TypeMember(name: String, ref: AbstractReference) extends RefinementDecl

  }

  case class Refinement(reference: AppliedReference, decls: Set[RefinementDecl]) extends AppliedReference {
    override def toString: String = this.render()
  }

  sealed trait Variance {
    override def toString: String = this.render()
  }

  object Variance {

    case object Invariant extends Variance

    case object Contravariant extends Variance

    case object Covariant extends Variance

  }

  sealed trait Boundaries {
    override def toString: String = this.render()
  }

  object Boundaries {

    case class Defined(bottom: AbstractReference, top: AbstractReference) extends Boundaries

    case object Empty extends Boundaries

  }

}