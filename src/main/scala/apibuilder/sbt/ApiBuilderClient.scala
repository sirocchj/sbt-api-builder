package apibuilder.sbt

import java.net.URL

import gigahorse.support.okhttp.Gigahorse
import sbt.util.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class ApiBuilderClient(log: Logger, baseURL: URL, basicAuth: String) {
  def retrieveAll(apiBuilderRequests: Seq[ApiBuilderRequest]): Future[Seq[ApiBuilderResponse]] =
    Gigahorse.withHttp(Gigahorse.config) { client =>
      Future
        .traverse(apiBuilderRequests) {
          case ApiBuilderRequest(path, matchers) =>
            val Valid = new CodeValidator(log, matchers)
            client
              .run {
                Gigahorse.url(s"$baseURL/$path").addHeaders("Authentication" -> basicAuth)
              }
              .collect {
                case Valid(lastModified, codeFiles) =>
                  codeFiles.map(cf => ApiBuilderResponse(lastModified, cf.dir.resolve(cf.name), cf.contents))
              }
        }
        .map(_.flatten)
    }
}
