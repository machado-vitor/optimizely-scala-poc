import com.optimizely.ab.{Optimizely, OptimizelyFactory, OptimizelyUserContext}
import com.optimizely.ab.optimizelydecision.OptimizelyDecision

import scala.util.Random
import scala.jdk.CollectionConverters._

object DecideAll {
  @main def decideAllDemo(): Unit = {
    val optimizelyClient: Optimizely = OptimizelyFactory.newDefaultInstance("") // key goes here

    try {
      if (!optimizelyClient.isValid) {
        println("Optimizely client invalid. Verify in Settings>Environments that you used the primary environment's SDK key")
        return
      }

      val rnd = new Random()

      (0 until 5).foreach { _ =>
        val userId = (rnd.nextInt(9999 - 1000) + 1000).toString
        val user: OptimizelyUserContext = optimizelyClient.createUserContext(userId)

        // decideAll returns decisions for all flags available in the current datafile
        val decisionsMap = user.decideAll()

        if (decisionsMap == null || decisionsMap.isEmpty) {
          println(s"\n\nNo decisions returned for user ${user.getUserId}. Ensure your project has flags and your SDK key is correct.")
        } else {
          println(s"\n\nDecisions for user ${user.getUserId} (total ${decisionsMap.size()} flags):")
          decisionsMap.asScala.foreach { case (flagKey, decision: OptimizelyDecision) =>
            val enabled = decision.getEnabled
            val variationKey = Option(decision.getVariationKey).getOrElse("<none>")
            val ruleKey = Option(decision.getRuleKey).getOrElse("<none>")
            val reasons = Option(decision.getReasons).map(_.toString).getOrElse("")
            val variablesStr = Option(decision.getVariables).map(_.toString).getOrElse("{}")

            println(
              s"- Flag '$flagKey': ${if (enabled) "on" else "off"}, variation=$variationKey, rule=$ruleKey, variables=$variablesStr"
            )
            if (!enabled && reasons.nonEmpty) println(s"  reasons: $reasons")
          }
        }
      }
    } finally {
      // close before exit to flush out queued events
      optimizelyClient.close()
    }
  }
}
