/*                     __                                               *\ 
**     ________ ___   / /  ___     Scala API                            ** 
**    / __/ __// _ | / /  / _ |    (c) 2005-2009, LAMP/EPFL             ** 
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               ** 
** /____/\___/_/ |_/____/_/ | |                                         ** 
**                          |/                                          ** 
\*                                                                      */ 
 
package scala.actors 

import scheduler.DaemonScheduler
 
/** 
 * Base trait for actors with daemon semantics.
 * Unlike a regular <code>Actor</code>, an active <code>DaemonActor</code> will 
 * not prevent an application terminating, much like a daemon thread. 
 *
 * @author Erik Engbrecht 
 */ 
trait DaemonActor extends Actor { 
  override def scheduler: IScheduler = DaemonScheduler
}
