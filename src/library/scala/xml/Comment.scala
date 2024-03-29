/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2009, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

// $Id$


package scala.xml
import collection.mutable.StringBuilder

/** The class <code>Comment</code> implements an XML node for comments.
 *
 * @author Burak Emir
 * @param text the text contained in this node, may not contain "--"
 */
case class Comment(commentText: String) extends SpecialNode
{  
  def label = "#REM"
  override def text = ""
  final override def doCollectNamespaces = false
  final override def doTransform         = false

  if (commentText contains "--")
    throw new IllegalArgumentException("text contains \"--\"")

  /** Appends &quot;<!-- text -->&quot; to this string buffer.
   */
  override def buildString(sb: StringBuilder) =
    sb append ("<!--" + commentText + "-->")
}
