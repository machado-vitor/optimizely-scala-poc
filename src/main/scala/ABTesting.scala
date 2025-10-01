import com.fasterxml.jackson.databind.ObjectMapper
import com.optimizely.ab.{Optimizely, OptimizelyFactory, OptimizelyUserContext}
import com.optimizely.ab.notification.DecisionNotification
import com.optimizely.ab.optimizelydecision.OptimizelyDecision
import com.optimizely.ab.config.parser.JsonParseException

import scala.io.StdIn
import scala.util.Random

object ABTesting {
  @main def main(): Unit = {
    val optimizelyClient: Optimizely = OptimizelyFactory.newDefaultInstance("")// key goes here

    try {
      if (!optimizelyClient.isValid) {
        println("Optimizely client invalid. Verify in Settings>Environments that you used the primary environment's SDK key")
        return
      }

      /* --------------------------------
       OPTIONAL: Add a notification listener so you can integrate with third-party analytics platforms
       --------------------------------
      */
      // To enable, set enableDecisionListener = true
      val enableDecisionListener = true
      val maybeNotificationId: Option[Int] =
        if (enableDecisionListener) {
          val mapper = new ObjectMapper()
          val id = optimizelyClient.getNotificationCenter.addNotificationHandler(
            classOf[DecisionNotification],
            (decisionNotification: DecisionNotification) => {
              if ("flag" == decisionNotification.getType) {
                try {
                  val serializedJsonInfo = mapper.writeValueAsString(decisionNotification.getDecisionInfo)
                  println(s"Feature flag access related information: $serializedJsonInfo")
                  // Send data to analytics provider here
                } catch {
                  case ex: Exception =>
                    System.err.println(s"Failed to serialize decision info: ${ex.getMessage}")
                }
              }
            }
          )
          Some(id)
        } else None

      /* --------------------------------
       * to get rapid demo experiment results, generate random users. Each user is deterministically hashed into a variation.
       * --------------------------------
       */
      var hasOnFlags = false
      val rnd = new Random()

      (0 until 5).foreach { _ =>
        val userId = (rnd.nextInt(9999 - 1000) + 1000).toString

        /* --------------------------------
           Bucket user into a flag variation and mock experiment results
           --------------------------------
        */
        val user: OptimizelyUserContext = optimizelyClient.createUserContext(userId)
        val decision: OptimizelyDecision = user.decide("product_sort_ab")

        if (decision.getVariationKey == null) {
          println(s"\n\ndecision error: ${decision.getReasons}")
        }

        var sortMethod: String | Null = null
        try {
          sortMethod = decision.getVariables.getValue("sort_method_ab", classOf[String])
        } catch {
          case e: JsonParseException => e.printStackTrace()
        }

        if (decision.getEnabled) {
          hasOnFlags = true
        }

        println(
          s"\n\nFlag ${if (decision.getEnabled) "on" else "off"}. " +
            s"User number ${user.getUserId} saw flag variation: ${decision.getVariationKey} " +
            s"and got products sorted by: ${Option(sortMethod).getOrElse("<unset>")} config variable as part of flag rule: ${decision.getRuleKey}"
        )

        // mock tracking a user event so you can see some experiment reports
        mockPurchase(user)
      }

      if (!hasOnFlags) {
        val projectId = Option(optimizelyClient.getProjectConfig).map(_.getProjectId).getOrElse("<unknown>")
        println(
          s"\n\nFlag was off for everyone. Some reasons could include:" +
            s"\n1. Your sample size of visitors was too small. Rerun, or increase the iterations in the FOR loop" +
            s"\n2. By default you have 2 keys for 2 project environments (dev/prod). Verify in Settings>Environments that you used the right key for the environment where your flag is toggled to ON." +
            s"\n\nCheck your key at  https://app.optimizely.com/v2/projects/${projectId}settings/implementation"
        )
      } else {
        val projectId = Option(optimizelyClient.getProjectConfig).map(_.getProjectId).getOrElse("<unknown>")
        println("\n\nDone with your mocked A/B test.")
        println(s"Check out your report at  https://app.optimizely.com/v2/projects/$projectId/reports")
        println("Be sure to select the environment that corresponds to your SDK key")
      }

      // If we added a notification listener, you could remove it here if desired
      // maybeNotificationId.foreach(id => optimizelyClient.getNotificationCenter.removeNotificationHandler(id))

    } finally {
      // close before exit to flush out queued events
      optimizelyClient.close()
    }
  }

  private def mockPurchase(user: OptimizelyUserContext): Unit = {
    print("Pretend that user made a purchase? y/n ")
    val answer = Option(StdIn.readLine()).getOrElse("").trim.toLowerCase
    if (answer == "y") {
      // track a user event you defined in the Optimizely app
      user.trackEvent("purchase")
      println(s"Optimizely recorded a purchase in experiment results for user ${user.getUserId}")
    } else {
      println(s"Optimizely didn't record a purchase in experiment results for user ${user.getUserId}")
    }
  }
}

