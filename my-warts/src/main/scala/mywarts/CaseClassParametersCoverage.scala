package mywarts

import org.wartremover.{WartTraverser, WartUniverse}

object CaseClassParametersCoverage extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    import scala.reflect.NameTransformer

    val targetCaseClassName: TermName = NameTransformer.encode("Test")
    val notImplemented: Symbol = typeOf[Predef.type].member(targetCaseClassName)
    require(notImplemented != NoSymbol)
    new Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case rt: RefTree if rt.symbol == notImplemented =>
            error(u)(tree.pos, "There was something left unimplemented")
          case _ =>
        }
        super.traverse(tree)
      }
    }
  }
}
