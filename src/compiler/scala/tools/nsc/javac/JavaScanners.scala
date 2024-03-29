/* NSC -- new Scala compiler
 * Copyright 2005-2009 LAMP/EPFL
 * @author  Martin Odersky
 */

package scala.tools.nsc
package javac

import scala.tools.nsc.util._
import SourceFile.{LF, FF, CR, SU}
import JavaTokens._
import scala.annotation.switch

// Todo merge these better with Scanners
trait JavaScanners {
  val global : Global
  import global._
  abstract class AbstractJavaTokenData {
    def token: Int
    type ScanPosition
    val NoPos: ScanPosition
    def pos: ScanPosition
    def name: Name
  }

  /** A class for representing a token's data. */
  trait JavaTokenData extends AbstractJavaTokenData {
    type ScanPosition = Int

    val NoPos: Int = -1
    /** the next token */
    var token: Int = EMPTY
    /** the token's position */
    var pos: Int = 0

    /** the first character position after the previous token */
    var lastPos: Int = 0

    /** the name of an identifier or token */
    var name: Name = null

    /** the base of a number */
    var base: Int = 0

    def copyFrom(td: JavaTokenData) = {
      this.token = td.token
      this.pos = td.pos
      this.lastPos = td.lastPos
      this.name = td.name
      this.base = td.base
    }
  }

  /** ...
   */
  abstract class AbstractJavaScanner extends AbstractJavaTokenData {
    implicit def p2g(pos: Position): ScanPosition
    implicit def g2p(pos: ScanPosition): Position
    def warning(pos: ScanPosition, msg: String): Unit
    def error  (pos: ScanPosition, msg: String): Unit
    def incompleteInputError(pos: ScanPosition, msg: String): Unit
    def deprecationWarning(pos: ScanPosition, msg: String): Unit
    /** the last error position
     */
    var errpos: ScanPosition 
    var lastPos: ScanPosition
    def skipToken: ScanPosition
    def nextToken: Unit
    def next: AbstractJavaTokenData
    def intVal(negated: Boolean): Long
    def floatVal(negated: Boolean): Double
    def intVal: Long = intVal(false)
    def floatVal: Double = floatVal(false)
    //def token2string(token : Int) : String = configuration.token2string(token)
    /** return recent scala doc, if any */
    def flushDoc: String
    def currentPos: Position
  }

  object JavaScannerConfiguration {
//  Keywords -----------------------------------------------------------------
    /** Keyword array; maps from name indices to tokens */
    private var key: Array[Byte] = _
    private var maxKey = 0
    private var tokenName = new Array[Name](128)

    {
      var tokenCount = 0

      // Enter keywords

      def enterKeyword(s: String, tokenId: Int) {
        val n = newTermName(s)
        while (tokenId >= tokenName.length) {
          val newTokName = new Array[Name](tokenName.length * 2)
          Array.copy(tokenName, 0, newTokName, 0, newTokName.length)
          tokenName = newTokName
        }
        tokenName(tokenId) = n
        if (n.start > maxKey) maxKey = n.start
        if (tokenId >= tokenCount) tokenCount = tokenId + 1
      }

      enterKeyword("abstract", ABSTRACT)
      enterKeyword("assert", ASSERT)
      enterKeyword("boolean", BOOLEAN)
      enterKeyword("break", BREAK)
      enterKeyword("byte", BYTE)
      enterKeyword("case", CASE)
      enterKeyword("catch", CATCH)
      enterKeyword("char", CHAR)
      enterKeyword("class", CLASS)
      enterKeyword("const", CONST)
      enterKeyword("continue", CONTINUE)
      enterKeyword("default", DEFAULT)
      enterKeyword("do", DO)
      enterKeyword("double", DOUBLE)
      enterKeyword("else", ELSE)
      enterKeyword("enum", ENUM)
      enterKeyword("extends", EXTENDS)
      enterKeyword("final", FINAL)
      enterKeyword("finally", FINALLY)
      enterKeyword("float", FLOAT)
      enterKeyword("for", FOR)
      enterKeyword("if", IF)
      enterKeyword("goto", GOTO)
      enterKeyword("implements", IMPLEMENTS)
      enterKeyword("import", IMPORT)
      enterKeyword("instanceof", INSTANCEOF)
      enterKeyword("int", INT)
      enterKeyword("interface", INTERFACE)
      enterKeyword("long", LONG)
      enterKeyword("native", NATIVE)
      enterKeyword("new", NEW)
      enterKeyword("package", PACKAGE)
      enterKeyword("private", PRIVATE)
      enterKeyword("protected", PROTECTED)
      enterKeyword("public", PUBLIC)
      enterKeyword("return", RETURN)
      enterKeyword("short", SHORT)
      enterKeyword("static", STATIC)
      enterKeyword("strictfp", STRICTFP)
      enterKeyword("super", SUPER)
      enterKeyword("switch", SWITCH)
      enterKeyword("synchronized", SYNCHRONIZED)
      enterKeyword("this", THIS)
      enterKeyword("throw", THROW)
      enterKeyword("throws", THROWS)
      enterKeyword("transient", TRANSIENT)
      enterKeyword("try", TRY)
      enterKeyword("void", VOID)
      enterKeyword("volatile", VOLATILE)
      enterKeyword("while", WHILE)

      // Build keyword array
      key = new Array[Byte](maxKey + 1)
      for (i <- 0 to maxKey)
        key(i) = IDENTIFIER
      for (j <- 0 until tokenCount)
        if (tokenName(j) ne null)
          key(tokenName(j).start) = j.toByte

    }

//Token representation -----------------------------------------------------

  /** Convert name to token */
  def name2token(name: Name): Int =
    if (name.start <= maxKey) key(name.start) else IDENTIFIER

  /** Returns the string representation of given token. */
  def token2string(token: Int): String = token match {
    case IDENTIFIER =>
      "identifier"/* + \""+name+"\""*/
    case CHARLIT =>
      "character literal"
    case INTLIT =>
      "integer literal"
    case LONGLIT =>
      "long literal"
    case FLOATLIT =>
      "float literal"
    case DOUBLELIT =>
      "double literal"
    case STRINGLIT =>
      "string literal"
    case COMMA => "`,'"
    case SEMI => "`;'"
    case DOT => "`.'"
    case AT => "`@'"
    case COLON => "`:'"
    case ASSIGN => "`='"
    case EQEQ => "`=='"
    case BANGEQ => "`!='"
    case LT => "`<'"
    case GT => "`>'"
    case LTEQ => "`<='"
    case GTEQ => "`>='"
    case BANG => "`!'"
    case QMARK => "`?'"
    case AMP => "`&'"
    case BAR => "`|'"
    case PLUS => "`+'"
    case MINUS => "`-'"
    case ASTERISK => "`*'"
    case SLASH => "`/'"
    case PERCENT => "`%'"
    case HAT => "`^'"
    case LTLT => "`<<'"
    case GTGT => "`>>'"
    case GTGTGT => "`>>>'"
    case AMPAMP => "`&&'"
    case BARBAR => "`||'"
    case PLUSPLUS => "`++'"
    case MINUSMINUS => "`--'"
    case TILDE => "`~'"
    case DOTDOTDOT => "`...'"
    case AMPEQ => "`&='"
    case BAREQ => "`|='"
    case PLUSEQ => "`+='"
    case MINUSEQ => "`-='"
    case ASTERISKEQ => "`*='"
    case SLASHEQ => "`/='"
    case PERCENTEQ => "`%='"
    case HATEQ => "`^='"
    case LTLTEQ => "`<<='"
    case GTGTEQ => "`>>='"
    case GTGTGTEQ => "`>>>='"
    case LPAREN => "`('"
    case RPAREN => "`)'"
    case LBRACE => "`{'"
    case RBRACE => "`}'"
    case LBRACKET => "`['"
    case RBRACKET => "`]'"
    case EOF => "eof"
    case ERROR => "something"
    case _ =>
      try {
        "`" + tokenName(token) + "'"
      } catch {
        case _: ArrayIndexOutOfBoundsException =>
          "`<" + token + ">'"
        case _: NullPointerException =>
          "`<(" + token + ")>'"
      }
    }
  }
  
  /** A scanner for Java.
   *
   *  @author     Martin Odersky
   */
  abstract class JavaScanner extends AbstractJavaScanner with JavaTokenData with Cloneable {
    override def intVal = super.intVal// todo: needed?
    override def floatVal = super.floatVal
    override var errpos: Int = NoPos
    def currentPos: Position = g2p(pos - 1)

    var in: JavaCharArrayReader = _

    def dup: JavaScanner = {
      val dup = clone().asInstanceOf[JavaScanner]
      dup.in = in.dup
      dup
    }

    /** character buffer for literals
     */  
    val cbuf = new StringBuilder()

    /** append Unicode character to "lit" buffer
    */
    protected def putChar(c: Char) { cbuf.append(c) }

    /** Clear buffer and set name */
    private def setName() {
      name = newTermName(cbuf.toString())
      cbuf.setLength(0)
    }

    /** buffer for the documentation comment
     */
    var docBuffer: StringBuilder = null

    def flushDoc = {
      val ret = if (docBuffer != null) docBuffer.toString else null
      docBuffer = null
      ret
    }
  
    /** add the given character to the documentation buffer 
     */
    protected def putDocChar(c: Char) {
      if (docBuffer ne null) docBuffer.append(c)
    }

    private class JavaTokenData0 extends JavaTokenData

    /** we need one token lookahead
     */
    val next : JavaTokenData = new JavaTokenData0
    val prev : JavaTokenData = new JavaTokenData0

// Get next token ------------------------------------------------------------

    /** read next token and return last position
     */
    def skipToken: Int = {
      val p = pos; nextToken
      p - 1
    }
    
    def nextToken {
      if (next.token == EMPTY) {
        //print("[")
        val t = fetchToken()
        //print(this)
        //print("]")
        t
      } else {
        this copyFrom next
        next.token = EMPTY
      }
    }

    def lookaheadToken: Int = {
      prev copyFrom this
      nextToken
      val t = token
      next copyFrom this
      this copyFrom prev
      t
    }

    private def afterLineEnd() = (
      lastPos < in.lineStartPos && 
      (in.lineStartPos <= pos ||
       lastPos < in.lastLineStartPos && in.lastLineStartPos <= pos)
    )

    /** read next token
     */
    private def fetchToken() {
      if (token == EOF) return
      lastPos = in.cpos - 1 // Position.encode(in.cline, in.ccol)
      //var index = bp
      while (true) {
        in.ch match {
          case ' ' | '\t' | CR | LF | FF =>
            in.next
          case _ =>
            pos = in.cpos // Position.encode(in.cline, in.ccol)
            (in.ch: @switch) match {
              case 'A' | 'B' | 'C' | 'D' | 'E' |
                   'F' | 'G' | 'H' | 'I' | 'J' |
                   'K' | 'L' | 'M' | 'N' | 'O' |
                   'P' | 'Q' | 'R' | 'S' | 'T' |
                   'U' | 'V' | 'W' | 'X' | 'Y' |
                   'Z' | '$' | '_' |
                   'a' | 'b' | 'c' | 'd' | 'e' |
                   'f' | 'g' | 'h' | 'i' | 'j' |
                   'k' | 'l' | 'm' | 'n' | 'o' |
                   'p' | 'q' | 'r' | 's' | 't' |
                   'u' | 'v' | 'w' | 'x' | 'y' |
                   'z' =>
                putChar(in.ch)
                in.next
                getIdentRest
                return

              case '0' =>
                putChar(in.ch)
                in.next
                if (in.ch == 'x' || in.ch == 'X') {
                  in.next
                  base = 16
                } else {
                  base = 8
                }
                getNumber
                return   

              case '1' | '2' | '3' | '4' |
                   '5' | '6' | '7' | '8' | '9' =>
                base = 10
                getNumber
                return

              case '\"' => 
                in.next
                while (in.ch != '\"' && (in.isUnicode || in.ch != CR && in.ch != LF && in.ch != SU)) {
                  getlitch()
                }
                if (in.ch == '\"') {
                  token = STRINGLIT
                  setName()
                  in.next
                } else {
                  syntaxError("unclosed string literal")
                }
                return

              case '\'' =>
                in.next
                getlitch()
                if (in.ch == '\'') {
                  in.next
                  token = CHARLIT
                  setName()
                } else {
                  syntaxError("unclosed character literal")
                }
                return

              case '=' =>
                token = ASSIGN
                in.next
                if (in.ch == '=') {
                  token = EQEQ
                  in.next
                } 
                return
              
              case '>' =>
                token = GT
                in.next
                if (in.ch == '=') {
                  token = GTEQ
                  in.next
                } else if (in.ch == '>') {
                  token = GTGT
                  in.next
                  if (in.ch == '=') {
                    token = GTGTEQ
                    in.next
                  } else if (in.ch == '>') {
                    token = GTGTGT
                    in.next
                    if (in.ch == '=') {
                      token = GTGTGTEQ
                      in.next
                    }
                  }
                }
                return
              
              case '<' =>
                token = LT
                in.next
                if (in.ch == '=') {
                  token = LTEQ
                  in.next
                } else if (in.ch == '<') {
                  token = LTLT
                  in.next
                  if (in.ch == '=') {
                    token = LTLTEQ
                    in.next
                  }
                }
                return

              case '!' =>
                token = BANG
                in.next
                if (in.ch == '=') {
                  token = BANGEQ
                  in.next
                } 
                return
                
              case '~' =>
                token = TILDE
                in.next
                return

              case '?' =>
                token = QMARK
                in.next
                return
                
              case ':' =>
                token = COLON
                in.next
                return

              case '@' =>
                token = AT
                in.next
                return

              case '&' =>
                token = AMP
                in.next
                if (in.ch == '&') {
                  token = AMPAMP
                  in.next
                } else if (in.ch == '=') {
                  token = AMPEQ
                  in.next
                } 
                return

              case '|' =>
                token = BAR
                in.next
                if (in.ch == '|') {
                  token = BARBAR
                  in.next
                } else if (in.ch == '=') {
                  token = BAREQ
                  in.next
                } 
                return

              case '+' =>
                token = PLUS
                in.next
                if (in.ch == '+') {
                  token = PLUSPLUS
                  in.next
                } else if (in.ch == '=') {
                  token = PLUSEQ
                  in.next
                } 
                return

              case '-' =>
                token = MINUS
                in.next
                if (in.ch == '-') {
                  token = MINUSMINUS
                  in.next
                } else if (in.ch == '=') {
                  token = MINUSEQ
                  in.next
                } 
                return

              case '*' =>
                token = ASTERISK
                in.next
                if (in.ch == '=') {
                  token = ASTERISKEQ
                  in.next
                } 
                return

              case '/' =>
                in.next
                if (!skipComment()) {
                  token = SLASH
                  in.next
                  if (in.ch == '=') {
                    token = SLASHEQ
                    in.next
                  } 
                  return
                }
              
              case '^' =>
                token = HAT
                in.next
                if (in.ch == '=') {
                  token = HATEQ
                  in.next
                } 
                return
              
              case '%' =>
                token = PERCENT
                in.next
                if (in.ch == '=') {
                  token = PERCENTEQ
                  in.next
                } 
                return

              case '.' =>
                token = DOT
                in.next
                if ('0' <= in.ch && in.ch <= '9') {
                  putChar('.'); getFraction
                } else if (in.ch == '.') {
                  in.next
                  if (in.ch == '.') {
                    in.next
                    token = DOTDOTDOT
                  } else syntaxError("`.' character expected")
                }
                return

              case ';' => 
                token = SEMI
                in.next
                return

              case ',' =>
                token = COMMA
                in.next
                return

              case '(' =>   
                token = LPAREN
                in.next
                return

              case '{' =>
                token = LBRACE
                in.next
                return

              case ')' =>
                token = RPAREN
                in.next
                return

              case '}' =>
                token = RBRACE
                in.next
                return

              case '[' =>
                token = LBRACKET
                in.next
                return

              case ']' =>
                token = RBRACKET
                in.next
                return

              case SU =>
                if (!in.hasNext) token = EOF
                else {
                  syntaxError("illegal character")
                  in.next
                }
                return

              case _ =>
                if (Character.isUnicodeIdentifierStart(in.ch)) {
                  putChar(in.ch)
                  in.next
                  getIdentRest
                } else {
                  syntaxError("illegal character: "+in.ch.toInt)
                  in.next
                }
                return
            }
        }
      }
    }

    private def skipComment(): Boolean = {
      if (in.ch == '/') {
        do {
          in.next
        } while ((in.ch != CR) && (in.ch != LF) && (in.ch != SU))
        true
      } else if (in.ch == '*') {
        docBuffer = null
        in.next
        val scalaDoc = ("/**", "*/")
        if (in.ch == '*' && onlyPresentation)
          docBuffer = new StringBuilder(scalaDoc._1)
        do {
          do {
            if (in.ch != '*' && in.ch != SU) {
              in.next; putDocChar(in.ch)
            }
          } while (in.ch != '*' && in.ch != SU)
          while (in.ch == '*') {
            in.next; putDocChar(in.ch)
          }
        } while (in.ch != '/' && in.ch != SU)
        if (in.ch == '/') in.next
        else incompleteInputError("unclosed comment")
        true
      } else {
        false
      }
    }

// Identifiers ---------------------------------------------------------------

    def isIdentStart(c: Char): Boolean = (
      ('A' <= c && c <= 'Z') ||
      ('a' <= c && c <= 'a') ||
      (c == '_') || (c == '$') ||
      Character.isUnicodeIdentifierStart(c)
    )

    def isIdentPart(c: Char) = (
      isIdentStart(c) || 
      ('0' <= c && c <= '9') ||
      Character.isUnicodeIdentifierPart(c)
    )

    def isSpecial(c: Char) = {
      val chtp = Character.getType(c)
      chtp == Character.MATH_SYMBOL.toInt || chtp == Character.OTHER_SYMBOL.toInt
    }

    private def getIdentRest {
      while (true) {
        (in.ch: @switch) match {
          case 'A' | 'B' | 'C' | 'D' | 'E' |
               'F' | 'G' | 'H' | 'I' | 'J' |
               'K' | 'L' | 'M' | 'N' | 'O' |
               'P' | 'Q' | 'R' | 'S' | 'T' |
               'U' | 'V' | 'W' | 'X' | 'Y' |
               'Z' | '$' |
               'a' | 'b' | 'c' | 'd' | 'e' |
               'f' | 'g' | 'h' | 'i' | 'j' |
               'k' | 'l' | 'm' | 'n' | 'o' |
               'p' | 'q' | 'r' | 's' | 't' |
               'u' | 'v' | 'w' | 'x' | 'y' |
               'z' |
               '0' | '1' | '2' | '3' | '4' |
               '5' | '6' | '7' | '8' | '9' =>
            putChar(in.ch)
            in.next
            
          case '_' =>
            putChar(in.ch)
            in.next
            getIdentRest
            return
          case SU =>
            setName()
            token = JavaScannerConfiguration.name2token(name)
            return
          case _ =>
            if (Character.isUnicodeIdentifierPart(in.ch)) {
              putChar(in.ch)
              in.next
            } else {
              setName()
              token = JavaScannerConfiguration.name2token(name)
              return
            }
        }
      }
    }

// Literals -----------------------------------------------------------------

    /** read next character in character or string literal:
    */
    protected def getlitch() =
      if (in.ch == '\\') {
        in.next
        if ('0' <= in.ch && in.ch <= '7') {
          val leadch: Char = in.ch
          var oct: Int = in.digit2int(in.ch, 8)
          in.next
          if ('0' <= in.ch && in.ch <= '7') {
            oct = oct * 8 + in.digit2int(in.ch, 8)
            in.next
            if (leadch <= '3' && '0' <= in.ch && in.ch <= '7') {
              oct = oct * 8 + in.digit2int(in.ch, 8)
              in.next
            }
          }
          putChar(oct.asInstanceOf[Char])
        } else {
          in.ch match {
            case 'b'  => putChar('\b')
            case 't'  => putChar('\t')
            case 'n'  => putChar('\n')
            case 'f'  => putChar('\f')
            case 'r'  => putChar('\r')
            case '\"' => putChar('\"')
            case '\'' => putChar('\'')
            case '\\' => putChar('\\')
            case _    =>
              syntaxError(in.cpos - 1, "invalid escape character")
              putChar(in.ch)
          }
          in.next
        }
      } else  {
        putChar(in.ch)
        in.next
      }

    /** read fractional part and exponent of floating point number
     *  if one is present.
     */
    protected def getFraction {
      token = DOUBLELIT
      while ('0' <= in.ch && in.ch <= '9') {
        putChar(in.ch)
        in.next
      }
      if (in.ch == 'e' || in.ch == 'E') {
        val lookahead = in.copy
        lookahead.next
        if (lookahead.ch == '+' || lookahead.ch == '-') {
          lookahead.next
        }
        if ('0' <= lookahead.ch && lookahead.ch <= '9') {
          putChar(in.ch)
          in.next
          if (in.ch == '+' || in.ch == '-') {
            putChar(in.ch)
            in.next
          }
          while ('0' <= in.ch && in.ch <= '9') {
            putChar(in.ch)
            in.next
          }
        }
        token = DOUBLELIT
      }
      if (in.ch == 'd' || in.ch == 'D') {
        putChar(in.ch)
        in.next
        token = DOUBLELIT
      } else if (in.ch == 'f' || in.ch == 'F') {
        putChar(in.ch)
        in.next
        token = FLOATLIT
      }
      setName()
    }

    /** convert name to long value
     */
    def intVal(negated: Boolean): Long = {
      if (token == CHARLIT && !negated) {
        if (name.length > 0) name(0) else 0
      } else {
        var value: Long = 0
        val divider = if (base == 10) 1 else 2
        val limit: Long =
          if (token == LONGLIT) Long.MaxValue else Int.MaxValue
        var i = 0
        val len = name.length
        while (i < len) {
          val d = in.digit2int(name(i), base)
          if (d < 0) {
            syntaxError("malformed integer number")
            return 0
          }
          if (value < 0 ||
              limit / (base / divider) < value ||
              limit - (d / divider) < value * (base / divider) &&
              !(negated && limit == value * base - 1 + d)) {
                syntaxError("integer number too large")
                return 0
              }
          value = value * base + d
          i += 1
        }
        if (negated) -value else value
      }
    }


    /** convert name, base to double value
    */
    def floatVal(negated: Boolean): Double = {
      val limit: Double = 
        if (token == DOUBLELIT) Double.MaxValue else Float.MaxValue
      try {
        val value: Double = java.lang.Double.valueOf(name.toString()).doubleValue()
        if (value > limit)
          syntaxError("floating point number too large")
        if (negated) -value else value
      } catch {
        case _: NumberFormatException => 
          syntaxError("malformed floating point number")
          0.0
      }
    }
    /** read a number into name and set base
    */
    protected def getNumber {
      while (in.digit2int(in.ch, if (base < 10) 10 else base) >= 0) {
        putChar(in.ch)
        in.next
      }
      token = INTLIT
      if (base <= 10 && in.ch == '.') {
        val lookahead = in.copy
        lookahead.next
        lookahead.ch match {
          case '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | 
               '8' | '9' | 'd' | 'D' | 'e' | 'E' | 'f' | 'F' =>
            putChar(in.ch)
            in.next
            return getFraction
          case _ =>
            if (!isIdentStart(lookahead.ch)) {
              putChar(in.ch)
              in.next
              return getFraction
            }
        }
      } 
      if (base <= 10 && 
          (in.ch == 'e' || in.ch == 'E' ||
           in.ch == 'f' || in.ch == 'F' ||
           in.ch == 'd' || in.ch == 'D')) {
        return getFraction
      }
      setName()
      if (in.ch == 'l' || in.ch == 'L') {
        in.next
        token = LONGLIT
      }
    }

// Errors -----------------------------------------------------------------

    /** generate an error at the given position
    */
    def syntaxError(pos: Int, msg: String) {
      error(pos, msg)
      token = ERROR
      errpos = pos
    }

    /** generate an error at the current token position
    */
    def syntaxError(msg: String) { syntaxError(pos, msg) }

    /** signal an error where the input ended in the middle of a token */
    def incompleteInputError(msg: String) {
      incompleteInputError(pos, msg)
      token = EOF
      errpos = pos
    }

    override def toString() = token match {
      case IDENTIFIER =>
        "id(" + name + ")"
      case CHARLIT =>
        "char(" + intVal + ")"
      case INTLIT =>
        "int(" + intVal + ")"
      case LONGLIT =>
        "long(" + intVal + ")"
      case FLOATLIT =>
        "float(" + floatVal + ")"
      case DOUBLELIT =>
        "double(" + floatVal + ")"
      case STRINGLIT =>
        "string(" + name + ")"
      case SEMI =>
        ";"
      case COMMA =>
        ","
      case _ =>
        JavaScannerConfiguration.token2string(token)
    }

    /** INIT: read lookahead character and token. 
     */
    def init {
      in.next
      nextToken
    }
  }

  /** ...
   */   
  class JavaUnitScanner(unit: CompilationUnit) extends JavaScanner {
    in = new JavaCharArrayReader(unit.source.asInstanceOf[BatchSourceFile].content, !settings.nouescape.value, syntaxError)
    init
    def warning(pos: Int, msg: String) = unit.warning(pos, msg)
    def error  (pos: Int, msg: String) = unit.  error(pos, msg)
    def incompleteInputError(pos: Int, msg: String) = unit.incompleteInputError(pos, msg)
    def deprecationWarning(pos: Int, msg: String) = unit.deprecationWarning(pos, msg)
    implicit def p2g(pos: Position): Int = if (pos.isDefined) pos.point else -1
    implicit def g2p(pos: Int): Position = new OffsetPosition(unit.source, pos)
  }
}
