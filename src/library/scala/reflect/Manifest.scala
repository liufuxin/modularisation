/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2007-2009, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

// $Id$


package scala.reflect

import scala.runtime._
import scala.collection.mutable._
import scala.collection.immutable.{List, Nil}

/** <p>
  *   A <code>Manifest[T]</code> is an opaque descriptor for type <code>T</code>.
  *   Currently, its only use is to give access to the erasure of the type as a
  *   <code>Class</code> instance.
  * </p>
  * <p>
  *   <b>BE AWARE</b>: The different type-relation operators are all forwarded 
  *   to the erased type as an approximation of the final semantics where
  *   these operators should be on the unerased type.
  * </p>
  */
@serializable
trait Manifest[T] extends ClassManifest[T] {
  override def typeArguments: List[Manifest[_]] = List()

  override def arrayManifest: Manifest[Array[T]] = 
    Manifest.classType[Array[T]](arrayClass[T](erasure))
}

/** <ps>
  *   This object is used by the compiler and <b>should not be used in client
  *   code</b>. The object <code>Manifest</code> defines factory methods for
  *   manifests.
  * </p>
  * <p>
  *   <b>BE AWARE</b>: The factory for refinement types is missing and
  *   will be implemented in a later version of this class.
  * </p>
  */
object Manifest {

  val Byte = new (Manifest[Byte] @serializable) {
    def erasure = java.lang.Byte.TYPE
    override def toString = "Byte"
    override def newArray(len: Int): Array[Byte] = new Array[Byte](len)
    override def newWrappedArray(len: Int): WrappedArray[Byte] = new WrappedArray.ofByte(new Array[Byte](len))
    override def newArrayBuilder(): ArrayBuilder[Byte] = new ArrayBuilder.ofByte()
  }

  val Short = new (Manifest[Short] @serializable) {
    def erasure = java.lang.Short.TYPE
    override def toString = "Short"
    override def newArray(len: Int): Array[Short] = new Array[Short](len)
    override def newWrappedArray(len: Int): WrappedArray[Short] = new WrappedArray.ofShort(new Array[Short](len))
    override def newArrayBuilder(): ArrayBuilder[Short] = new ArrayBuilder.ofShort()
  }

  val Char = new (Manifest[Char] @serializable) {
    def erasure = java.lang.Character.TYPE
    override def toString = "Char"
    override def newArray(len: Int): Array[Char] = new Array[Char](len)
    override def newWrappedArray(len: Int): WrappedArray[Char] = new WrappedArray.ofChar(new Array[Char](len))
    override def newArrayBuilder(): ArrayBuilder[Char] = new ArrayBuilder.ofChar()
  }

  val Int = new (Manifest[Int] @serializable) {
    def erasure = java.lang.Integer.TYPE
    override def toString = "Int"
    override def newArray(len: Int): Array[Int] = new Array[Int](len)
    override def newWrappedArray(len: Int): WrappedArray[Int] = new WrappedArray.ofInt(new Array[Int](len))
    override def newArrayBuilder(): ArrayBuilder[Int] = new ArrayBuilder.ofInt()
  }

  val Long = new (Manifest[Long] @serializable) {
    def erasure = java.lang.Long.TYPE
    override def toString = "Long"
    override def newArray(len: Int): Array[Long] = new Array[Long](len)
    override def newWrappedArray(len: Int): WrappedArray[Long] = new WrappedArray.ofLong(new Array[Long](len))
    override def newArrayBuilder(): ArrayBuilder[Long] = new ArrayBuilder.ofLong()
  }

  val Float = new (Manifest[Float] @serializable) {
    def erasure = java.lang.Float.TYPE
    override def toString = "Float"
    override def newArray(len: Int): Array[Float] = new Array[Float](len)
    override def newWrappedArray(len: Int): WrappedArray[Float] = new WrappedArray.ofFloat(new Array[Float](len))
    override def newArrayBuilder(): ArrayBuilder[Float] = new ArrayBuilder.ofFloat()
  }

  val Double = new (Manifest[Double] @serializable) {
    def erasure = java.lang.Double.TYPE
    override def toString = "Double"
    override def newArray(len: Int): Array[Double] = new Array[Double](len)
    override def newWrappedArray(len: Int): WrappedArray[Double] = new WrappedArray.ofDouble(new Array[Double](len))
    override def newArrayBuilder(): ArrayBuilder[Double] = new ArrayBuilder.ofDouble()
  }

  val Boolean = new (Manifest[Boolean] @serializable) {
    def erasure = java.lang.Boolean.TYPE
    override def toString = "Boolean"
    override def newArray(len: Int): Array[Boolean] = new Array[Boolean](len)
    override def newWrappedArray(len: Int): WrappedArray[Boolean] = new WrappedArray.ofBoolean(new Array[Boolean](len))
    override def newArrayBuilder(): ArrayBuilder[Boolean] = new ArrayBuilder.ofBoolean()
  }

  val Unit = new (Manifest[Unit] @serializable) {
    def erasure = java.lang.Void.TYPE
    override def toString = "Unit"
    override def newArray(len: Int): Array[Unit] = new Array[Unit](len)
    override def newWrappedArray(len: Int): WrappedArray[Unit] = new WrappedArray.ofUnit(new Array[Unit](len))
    override def newArrayBuilder(): ArrayBuilder[Unit] = new ArrayBuilder.ofUnit()
  }

  val Any: Manifest[Any] = new ClassTypeManifest[Any](None, classOf[java.lang.Object], List()) {
    override def toString = "Any"
    // todo: re-implement <:<
  }

  val Object: Manifest[Object] = new ClassTypeManifest[Object](None, classOf[java.lang.Object], List()) {
    override def toString = "Object"
    // todo: re-implement <:<
  }

  val AnyVal: Manifest[AnyVal] = new ClassTypeManifest[AnyVal](None, classOf[java.lang.Object], List()) {
    override def toString = "AnyVal"
    // todo: re-implement <:<
  }

  val Null: Manifest[Null] = new ClassTypeManifest[Null](None, classOf[java.lang.Object], List()) {
    override def toString = "Null"
    // todo: re-implement <:<
  }

  val Nothing: Manifest[Nothing] = new ClassTypeManifest[Nothing](None, classOf[java.lang.Object], List()) {
    override def toString = "Nothing"
    // todo: re-implement <:<
  }

  /** Manifest for the singleton type `value.type'. */
  def singleType[T](value: Any): Manifest[T] =
    new (Manifest[T] @serializable) {
      lazy val erasure =
        value match {
          case anyRefValue: AnyRef => anyRefValue.getClass
          case anyValue => error("There is no singleton type for AnyVal values")
        }
      override lazy val toString = value.toString + ".type"
    }

  /** Manifest for the class type `clazz[args]', where `clazz' is
    * a top-level or static class.
    * @note This no-prefix, no-arguments case is separate because we
    *       it's called from ScalaRunTime.boxArray itself. If we
    *       pass varargs as arrays into this, we get an infinitely recursive call
    *       to boxArray. (Besides, having a separate case is more efficient)
    */
  def classType[T](clazz: Predef.Class[_]): Manifest[T] =
    new ClassTypeManifest[T](None, clazz, Nil)

  /** Manifest for the class type `clazz', where `clazz' is
    * a top-level or static class and args are its type arguments. */
  def classType[T](clazz: Predef.Class[T], arg1: Manifest[_], args: Manifest[_]*): Manifest[T] =
    new ClassTypeManifest[T](None, clazz, arg1 :: args.toList)

  /** Manifest for the class type `clazz[args]', where `clazz' is
    * a class with non-package prefix type `prefix` and type arguments `args`.
    */
  def classType[T](prefix: Manifest[_], clazz: Predef.Class[_], args: Manifest[_]*): Manifest[T] =
    new ClassTypeManifest[T](Some(prefix), clazz, args.toList)

  /** Manifest for the class type `clazz[args]', where `clazz' is
    * a top-level or static class. */
  @serializable
  private class ClassTypeManifest[T](prefix: Option[Manifest[_]], 
                                     val erasure: Predef.Class[_], 
                                     override val typeArguments: List[Manifest[_]]) extends Manifest[T] {
    override def toString = 
      (if (prefix.isEmpty) "" else prefix.get.toString+"#") +
      (if (erasure.isArray) "Array" else erasure.getName) +
      argString
   }

  def arrayType[T](arg: Manifest[_]): Manifest[Array[T]] = 
    arg.asInstanceOf[Manifest[T]].arrayManifest

  /** Manifest for the abstract type `prefix # name'. `upperBound' is not
    * strictly necessary as it could be obtained by reflection. It was
    * added so that erasure can be calculated without reflection. */
  def abstractType[T](prefix: Manifest[_], name: String, upperBound: Manifest[_], args: Manifest[_]*): Manifest[T] =
    new (Manifest[T] @serializable) {
      def erasure = upperBound.erasure
      override val typeArguments = args.toList
      override def toString = prefix.toString+"#"+name+argString
    }

  /** Manifest for the intersection type `parents_0 with ... with parents_n'. */
  def intersectionType[T](parents: Manifest[_]*): Manifest[T] =
    new (Manifest[T] @serializable) {
      def erasure = parents.head.erasure
      override def toString = parents.mkString(" with ")
    }
}
