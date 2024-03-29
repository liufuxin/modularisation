/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2005-2009, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

// $Id$

package scala.actors

/** The <code>SchedulerAdapter</code> trait is used to adapt
 *  the behavior of the standard <code>Scheduler</code> object.
 *
 *  Providing an implementation for the
 *  <code>execute(f: => Unit)</code> method is sufficient to
 *  obtain a concrete <code>IScheduler</code> implementation.
 *
 *  @author Philipp Haller
 */
trait SchedulerAdapter extends IScheduler {

  /** Submits a <code>Runnable</code> for execution.
   *
   *  @param  task  the task to be executed
   */
  def execute(task: Runnable): Unit =
    execute { task.run() }

  /** Shuts down the scheduler.
   */
  def shutdown(): Unit =
    Scheduler.shutdown()

  /** When the scheduler is active, it can execute tasks.
   */ 
  def isActive: Boolean =
    Scheduler.isActive

  /** Registers a newly created actor with this scheduler.
   *
   *  @param  a  the actor to be registered
   */
  def newActor(a: Reactor) =
    Scheduler.newActor(a)

  /** Unregisters an actor from this scheduler, because it
   *  has terminated.
   * 
   *  @param  a  the actor to be unregistered
   */
  def terminated(a: Reactor) =
    Scheduler.terminated(a)

  /** Registers a closure to be executed when the specified
   *  actor terminates.
   * 
   *  @param  a  the actor
   *  @param  f  the closure to be registered
   */
  def onTerminate(a: Reactor)(f: => Unit) =
    Scheduler.onTerminate(a)(f)

  def managedBlock(blocker: scala.concurrent.ManagedBlocker) {
    blocker.block()
  }
}
