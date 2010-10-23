import sbt.ModuleID
import scala.collection.mutable.HashMap

object IdeaDocUrl {
  val docUrls = HashMap[String, String]()

  implicit def moduleIdExtras(moduleId: ModuleID) = new {
    def ideaDocUrl(url: String): ModuleID = {
      docUrls += ((moduleId.name + "-" + moduleId.revision) -> url)
      moduleId
    }
  }
}