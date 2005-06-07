/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003, LAMP/EPFL                  **
**  __\ \/ /__/ __ |/ /__/ __ |                                         **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
** $Id$
\*                                                                      */

package scala.collection.mutable;


/** <code>History[A, B]</code> objects may subscribe to events of
 *  type <code>A</code> published by an object of type <code>B</code>.
 *  The history subscriber object records all published events
 *  up to maximum number of <code>maxHistory</code> events.
 *
 *  @author  Matthias Zenger
 *  @version 1.0, 08/07/2003
 */
[serializable]
class History[A, B] extends AnyRef with Subscriber[A, B] with Iterable[Pair[B, A]] {

    protected val log: Queue[Pair[B, A]] = new Queue[Pair[B, A]];
    
    val maxHistory: Int = 1000;
    
    def notify(pub: B, event: A): Unit = {
        if (log.length >= maxHistory) {
            val old = log.dequeue;
        }
        log.enqueue(Pair(pub, event));
    }
    
    def elements: Iterator[Pair[B, A]] = log.elements;
    
    def events: Iterator[A] = log.elements.map { case Pair(_, e) => e }
    
    def size: Int = log.length;
    
    def clear: Unit = log.clear;
}
