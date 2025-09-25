import com.optimizely.ab.Optimizely
import com.optimizely.ab.OptimizelyFactory
import com.optimizely.ab.OptimizelyUserContext
import com.optimizely.ab.config.parser.JsonParseException
import com.optimizely.ab.optimizelydecision.OptimizelyDecision

import java.util.Random

@main def run(): Unit = {
  // this Optimizely initialization is synchronous. for other methods see the Java SDK reference
  val optimizelyClient: Optimizely = OptimizelyFactory.newDefaultInstance("")// key goes here/

  try {
    if (optimizelyClient.isValid) {
      // To get rapid demo results, generate random users. Each user always sees the same variation unless you reconfigure the flag rule.
      val rnd = new Random()

      var hasOnFlags = false

      (0 until 10).foreach { _ =>
        val userId = (rnd.nextInt(9999 - 1000) + 1000).toString

        // Create hardcoded user & bucket user into a flag variation
        val user: OptimizelyUserContext = optimizelyClient.createUserContext(userId)
        // "product_sort" corresponds to a flag key in your Optimizely project
        val decision: OptimizelyDecision = user.decide("product_sort")

        // did decision fail with a critical error?
        if (decision.getVariationKey == null) {
          println(s"\n\ndecision error: ${decision.getReasons}")
        }

        // get a dynamic configuration variable
        // "sort_method" corresponds to a variable key in your Optimizely project
        var sortMethod: String | Null = null
        try {
          sortMethod = decision.getVariables.getValue("sort_method", classOf[String])
        } catch {
          case e: JsonParseException => e.printStackTrace()
        }

        if (decision.getEnabled) {
          // Keep count how many visitors had the flag enabled
          hasOnFlags = true
        }

        // Mock what the users sees with print statements (in production, use flag variables to implement feature configuration)
        println(
          s"\n\nFlag ${if (decision.getEnabled) "on" else "off"}. User number ${user.getUserId} saw flag variation: ${decision.getVariationKey} and got products sorted by: ${Option(sortMethod).getOrElse("<unset>")} config variable as part of flag rule: ${decision.getRuleKey}"
        )
      }

      if (!hasOnFlags) {
        val projectId = Option(optimizelyClient.getProjectConfig).map(_.getProjectId).getOrElse("<unknown>")
        println(
          s"\n\nFlag was off for everyone. Some reasons could include:" +
            s"\n1. Your sample size of visitors was too small. Rerun, or increase the iterations in the FOR loop" +
            s"\n2. By default you have 2 keys for 2 project environments (dev/prod). Verify in Settings>Environments that you used the right key for the environment where your flag is toggled to ON." +
            s"\n\nCheck your key at  https://app.optimizely.com/v2/projects/${projectId}settings/implementation"
        )
      }
    } else {
      println("Optimizely client invalid. Verify in Settings>Environments that you used the primary environment's SDK key")
    }
  } finally {
    // close before exit to flush out queued events
    optimizelyClient.close()
  }
}

