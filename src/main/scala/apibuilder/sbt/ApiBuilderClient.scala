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
        .traverse(apiBuilderRequests) { case ApiBuilderRequest(path, matchers, target) =>
          val Valid = new CodeValidator(log, matchers)
          client
            .run {
              val request = Gigahorse.url(s"$baseURL/$path").addHeaders("Authorization" -> basicAuth)
              log.debug(s"sending $request")
              request
            }
            .collect { case Valid(lastModified, codeFiles) =>
              codeFiles.map { cf =>
                ApiBuilderResponse(lastModified, target, cf.maybeDir.fold(cf.name)(_.resolve(cf.name)), cf.contents)
              }
            }
        }
        .map(_.flatten)
    }
}
