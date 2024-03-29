/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2009, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.collection
package interfaces

import generic._
import mutable.Buffer
import scala.reflect.ClassManifest
import annotation.unchecked.uncheckedVariance

/**
 * @since 2.8
 */
trait IterableMethods[+A, +This <: IterableLike[A, This] with Iterable[A]] extends TraversableMethods[A, This]
{
  // abstract
  def iterator: Iterator[A]

  // concrete
  def dropRight(n: Int): Iterable[A]
  def sameElements[B >: A](that: Iterable[B]): Boolean
  def sortWith(lt: (A, A) => Boolean)(implicit m: ClassManifest[A @uncheckedVariance]): This
  def takeRight(n: Int): Iterable[A]
  def zipAll[B, A1 >: A, That](that: Iterable[B], e1: A1, e2: B)(implicit bf: CanBuildFrom[This, (A1, B), That]): That
  def zipWithIndex[A1 >: A, That](implicit bf: CanBuildFrom[This, (A1, Int), That]): That
  def zip[A1 >: A, B, That](that: Iterable[B])(implicit bf: CanBuildFrom[This, (A1, B), That]): That
  
  override def view: IterableView[A, This]
  override def view(from: Int, until: Int): IterableView[A, This]
}
