/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2008, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

// $Id: Iterable.scala 15188 2008-05-24 15:01:02Z stepancheg $

package scalax.collection.mutable

import generic._

trait OrderedIterable[A] extends Iterable[A] with collection.OrderedIterable[A] with OrderedIterableTemplate[OrderedIterable, A]

/* Factory object for `OrderedIterable` class */
object OrderedIterable extends IterableFactory[OrderedIterable] {
  /** The empty iterable */
  def apply[A](args: A*): OrderedIterable[A] = ArrayBuffer.apply(args: _*) // !!! swicth to Array?
}

