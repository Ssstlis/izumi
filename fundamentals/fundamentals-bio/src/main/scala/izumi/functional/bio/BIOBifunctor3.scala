package izumi.functional.bio

trait BIOBifunctor3[F[-_, +_, +_]] extends BIOFunctor3[F] {
  def bimap[R, E, A, E2, A2](r: F[R, E, A])(f: E => E2, g: A => A2): F[R, E2, A2]
  def leftMap[R, E, A, E2](r: F[R, E, A])(f: E => E2): F[R, E2, A] = bimap(r)(f, identity)

  @inline final def widenError[R, E, A, E1](r: F[R, E, A])(implicit @deprecated("unused", "") ev: E <:< E1): F[R, E1, A] = r.asInstanceOf[F[R, E1, A]]
  @inline final def widenBoth[R, E, A, E1, A1](r: F[R, E, A])(implicit @deprecated("unused", "") ev: E <:< E1, @deprecated("unused", "") ev2: A <:< A1): F[R, E1, A1] =
    r.asInstanceOf[F[R, E1, A1]]
}
