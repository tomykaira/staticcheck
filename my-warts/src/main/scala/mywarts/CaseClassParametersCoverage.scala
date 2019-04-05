package mywarts

import org.wartremover.{WartTraverser, WartUniverse}

import scala.collection.mutable

object CaseClassParametersCoverage extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    case class ApplyInfo(clsType: Type, args: Seq[Tree], applyTree: Tree, stats: Seq[Tree]) {
      val resolvedArgs = args.map {
        case x@Ident(i) =>
          stats.flatMap { s =>
            s match {
              case ValDef(_, name, _, rhs) if name == i => Some(rhs)
              case _ => None
            }
          }.headOption.getOrElse(x)
        case x => x
      }

      def reportProblems(): Unit = {
        val params = clsType.member(TermName("apply")).asMethod.paramLists.head
        if (params.length != resolvedArgs.length) { throw new RuntimeException("arg length mismatch") }
        val classifications = params.zip(resolvedArgs).zipWithIndex.map { case ((param, arg), nth) =>
          val usesDefaultArgument = arg match {
            case Select(tpt, TermName(name)) =>
              tpt.tpe == clsType && name == "apply$default$" + (nth + 1).toString
            case _ => false
          }
          val isErrorValDef = param.name.toString == "error"
          (isErrorValDef, usesDefaultArgument, param, arg)
        }
        val (errors, others) = classifications.partition(_._1)
        if (errors.head._2) {
          // all non error fields must be set
          others.filter { _._2 }.foreach { case (_, _, valDef, arg) =>
            error(u)(arg.pos, s"${valDef.name} must be explicitly specified")
          }
        } else  {
          // all non error fields must be default
          others.filter { !_._2 }.foreach { case (_, _, valDef, arg) =>
            error(u)(arg.pos, s"${valDef.name} must be not set")
          }
        }
      }
    }

    object ApplyApply {
      def unapply(tree: Tree): Option[ApplyInfo] = {
        tree match {
          case a@Apply(Select(tpt, TermName("apply")), args) =>
            Some(ApplyInfo(tpt.tpe, args, a, Seq()))
          case _ => None
        }
      }
    }

    object BlockApplyApply {
      def unapply(tree: Tree): Option[ApplyInfo] = {
        tree match {
          case Block(stats, a@Apply(Select(tpt, TermName("apply")), args)) =>
            Some(ApplyInfo(tpt.tpe, args, a, stats))
          case _ => None
        }
      }
    }

    new Traverser {
      val applyInfos = mutable.ArrayBuffer[ApplyInfo]()

      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case BlockApplyApply(applyInfo) if !checkedApplyTrees.contains(applyInfo.applyTree) =>
            applyInfos += applyInfo
            applyInfo.reportProblems()
            super.traverse(tree)
          case ApplyApply(applyInfo) if !checkedApplyTrees.contains(applyInfo.applyTree) =>
            applyInfos += applyInfo
            applyInfo.reportProblems()
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }

      def checkedApplyTrees: Seq[u.universe.Tree] = applyInfos.map(_.applyTree)
    }
  }
}
