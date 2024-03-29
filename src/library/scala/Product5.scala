/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2009, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

// $Id$

// generated by genprod on Wed Nov 04 18:46:21 CET 2009  

package scala


object Product5 {
  def unapply[T1, T2, T3, T4, T5](x: Product5[T1, T2, T3, T4, T5]): Option[Product5[T1, T2, T3, T4, T5]] = 
    Some(x)
}

/** Product5 is a cartesian product of 5 components.
 *  
 *  @since 2.3
 */
trait Product5[+T1, +T2, +T3, +T4, +T5] extends Product {
  /**
   *  The arity of this product.
   *  @return 5
   */
  override def productArity = 5

  /**
   *  Returns the n-th projection of this product if 0&lt;=n&lt;arity,
   *  otherwise null.
   *
   *  @param n number of the projection to be returned 
   *  @return  same as _(n+1)
   *  @throws  IndexOutOfBoundsException
   */
  @throws(classOf[IndexOutOfBoundsException])
  override def productElement(n: Int) = n match { 
    case 0 => _1
    case 1 => _2
    case 2 => _3
    case 3 => _4
    case 4 => _5
    case _ => throw new IndexOutOfBoundsException(n.toString())
 }  

  /** projection of this product */
  def _1: T1

  /** projection of this product */
  def _2: T2

  /** projection of this product */
  def _3: T3

  /** projection of this product */
  def _4: T4

  /** projection of this product */
  def _5: T5



}
