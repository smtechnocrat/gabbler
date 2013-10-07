/*
 * Copyright 2013 Heiko Seeberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package name.heikoseeberger.gabbler

import akka.actor.{ ActorLogging, ActorRef, Props }
import akka.io.IO
import scala.concurrent.duration.FiniteDuration
import spray.can.Http
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.routing.{ HttpServiceActor, Route }
import spray.routing.authentication.BasicAuth

object GabblerService {

  object Message extends DefaultJsonProtocol {
    implicit val format = jsonFormat2(apply)
  }

  case class Message(username: String, text: String)

  def props(hostname: String, port: Int, timeout: FiniteDuration): Props =
    Props(new GabblerService(hostname, port, timeout))
}

class GabblerService(hostname: String, port: Int, timeout: FiniteDuration) extends HttpServiceActor
    with ActorLogging with SprayJsonSupport {

  import GabblerService._
  import context.dispatcher

  IO(Http)(context.system) ! Http.Bind(self, hostname, port)

  override def receive: Receive =
    runRoute(apiRoute ~ staticRoute)

  def apiRoute: Route =
    // format: OFF
    authenticate(BasicAuth(UsernameEqualsPasswordAuthenticator, "Gabbler"))(user =>
      path("api" / "messages")(
        get(
          produce(instanceOf[Seq[Message]]){ completer => _ =>
            log.debug("User '{}' is asking for messages ...", user.username)
            gabblerFor(user.username) ! completer
          }
        ) ~
        post(
          entity(as[Message]) { message =>
            complete {
              log.debug("User '{}' has posted '{}'", user.username, message.text)
              val m = message.copy(username = user.username)
              context.children foreach (_ ! m)
              StatusCodes.NoContent
            }
          }
        )
      )
    ) // format: ON

  def staticRoute: Route =
    path("")(getFromResource("web/index.html")) ~ getFromResourceDirectory("web")

  def gabblerFor(username: String): ActorRef =
    context.child(username) getOrElse context.actorOf(Gabbler.props(timeout), username)
}
